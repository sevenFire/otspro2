package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.kafka.MessageHandlerFactory;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableUpdateBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableNameListBody;
import com.baosight.xinsight.ots.rest.model.table.vo.TableInfoVo;
import com.baosight.xinsight.ots.rest.permission.CachePermission;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.MessageBuilder;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.TableConfigUtil;
import com.baosight.xinsight.utils.AasPermissionUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/13
 * @description
 */
public class TableService {
    private static final Logger LOG = Logger.getLogger(TableService.class);


    /**
     * create new table
     *
     * @return created table
     * @throws Exception
     */
    public static void createTable(PermissionCheckUserInfo userInfo, String tableName, TableCreateBody tableCreateBody) throws OtsException {
        try {
            //将权限信息并发送到kafka
            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.CacheInfo info = PermissionUtil.GetInstance().otsPermissionHandler(userInfo, -1, PermissionUtil.PermissionOpesration.OTSMANAGEW); //todo lyh managew?

                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(), MessageBuilder.buildPermissionMessage(userInfo, RestConstants.OTS_MANAGE_PERMISSION_OPERATION,
                                info.isReadPermission(), info.isWritePermission(), info.isPermissionFlag(), info.getCurrentTime()));

                LOG.debug("messageHandlerFactory: message" + MessageBuilder.buildPermissionMessage(userInfo, RestConstants.OTS_MANAGE_PERMISSION_OPERATION,
                        info.isReadPermission(), info.isWritePermission(), info.isPermissionFlag(), info.getCurrentTime()));
            }

            //创建表（包含大表和小表），以及表存在性校验
            Table table = ConfigUtil.getInstance().getOtsAdmin().createTable(userInfo.getUserId(), userInfo.getTenantId(), tableName,tableCreateBody.toTable());

            //添加到缓存中
            TableInfoVo info = new TableInfoVo(table.getTableName());
            info.setTableColumns(table.getTableColumns());
            info.setPrimaryKey(table.getPrimaryKey());
            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), info);

            //将表信息并发送到kafka
            MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                    .sendData(userInfo.getTenantId(), MessageBuilder.buildConfigMessage(0, userInfo.getTenantId(), userInfo.getUserId(), tableName));
            LOG.debug("messageHandlerFactory: message" + MessageBuilder.buildConfigMessage(0, userInfo.getTenantId(), userInfo.getUserId(), tableName));
        } catch (TableException e) {
            throw e;
        } catch (ConfigException e) {
            throw e;
        } catch (OtsException e) {
            throw e;
        }
    }

    /**
     * 修改表
     * @param userInfo
     * @param tableName
     * @param tableUpdateBody
     */
    public static void updateTable(PermissionCheckUserInfo userInfo, String tableName, TableUpdateBody tableUpdateBody) throws OtsException {
        try {
            //校验权限信息
            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.GetInstance().otsPermissionHandler(userInfo, userInfo.getUserId(), PermissionUtil.PermissionOpesration.READ);
            }

            //todo lyh 校验修改后的参数是否正确
            //1.table_name和table_desc至少要有一个。
            //2.二者都要符合字段规范，其中table_desc可以为空，table_name不可以。

            //修改表，包含表存在性校验
            Table table = ConfigUtil.getInstance().getOtsAdmin().updateTable(userInfo.getUserId(), userInfo.getTenantId(), tableName,tableUpdateBody.toTable());

            //更新缓存
            TableInfoVo info = new TableInfoVo(table.getTableName());
            info.setTableColumns(table.getTableColumns());
            info.setPrimaryKey(table.getPrimaryKey());
            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), info);

            //将表信息并发送到kafka
            MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                    .sendData(userInfo.getTenantId(), MessageBuilder.buildConfigMessage(0, userInfo.getTenantId(), userInfo.getUserId(), tableName));

        } catch (TableException e) {
            throw e;
        } catch (ConfigException e) {
            throw e;
        } catch (OtsException e) {
            throw e;
        }
    }

    /**
     * 删除表
     * @param userInfo
     * @param tableName
     */
    public static void deleteTable(PermissionCheckUserInfo userInfo, String tableName) throws OtsException, IOException {

        try {
            //删除表，包含表存在性校验、小表、HBase中对应的表记录
            Table table = ConfigUtil.getInstance().getOtsAdmin().deleteTable(userInfo.getTenantId(), tableName);

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.GetInstance().otsPermissionHandler(userInfo, -1, PermissionUtil.PermissionOpesration.OTSMANAGEW);
                // 删除缓存中此表的相关权限信息
                CachePermission.getInstance().batchRemove((new StringBuilder()).append(RestConstants.OTS_PERMISSION_CACHE_KEY)
                        .append(userInfo.getUserId()).append(CommonConstants.DEFAULT_SINGLE_UNLINE_SPLIT).append(table.getTableId()).toString());

                // kafka发送删除信息
                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(), MessageBuilder.buildPermissionRemoveMessage((new StringBuilder()).append(RestConstants.OTS_PERMISSION_CACHE_KEY)
                                .append(userInfo.getUserId()).append(CommonConstants.DEFAULT_SINGLE_UNLINE_SPLIT).append(table.getTableId()).toString()));
            }

            // 删除redis中的metric记录
            ConfigUtil.getInstance().getRedisUtil().delHSet(RestConstants.DEFAULT_METRICS_PREFIX + userInfo.getTenantId(), tableName);

            TableConfigUtil.deleteTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);

            MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                    .sendData(userInfo.getTenantId(), MessageBuilder.buildConfigMessage(1, userInfo.getTenantId(), userInfo.getUserId(), tableName));

            LOG.debug("deleteTable successful: uid=" + userInfo.getUserId() + ", namespace=" + userInfo.getUserId() + ", tablename=" + tableName);

        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * 获取单表的详细信息
     * @param userInfo
     * @param tableName
     * @return
     */
    public static TableInfoBody getTableInfo(PermissionCheckUserInfo userInfo, String tableName) throws OtsException {
        TableInfoBody tableInfoBody = new TableInfoBody();

        try {
            //获取表信息
            Table table = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(),tableName);
            if (table == null) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                        String.format("tenant (id:%d) was not owned table:%s!", userInfo.getTenantId(), tableName));
            }

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                //判定并获取权限信息
                PermissionUtil.CacheInfo cacheInfo = PermissionUtil.GetInstance()
                        .otsPermissionHandler(userInfo, table.getTableId(), PermissionUtil.PermissionOpesration.READ);
                //通过kafka将权限信息发送
                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(),MessageBuilder.buildPermissionMessage(userInfo, table.getTableId(), cacheInfo.isReadPermission(), cacheInfo.isWritePermission(), cacheInfo.isPermissionFlag(), cacheInfo.getCurrentTime()));
            }

            //更新缓存
            TableInfoVo tableInfoVo = new TableInfoVo();
            tableInfoVo.fromTable(table);
            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableInfoVo);

            //返回
            tableInfoBody.fromTable(table);

        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

        return tableInfoBody;
    }



    /**
     * 获取表名的列表，不带查询条件，查询出全部。
     * @param userInfo
     * @param limit
     * @param offset
     * @return
     */
    public static TableNameListBody getTableNameList(PermissionCheckUserInfo userInfo, long limit, long offset) throws OtsException {
        return (TableNameListBody) getTableNameList(userInfo,null,limit,offset,true);
    }

    /**
     * 获取表名的列表，带tableName的查询条件
     * @param userInfo
     * @param tableName
     * @param limit
     * @param offset
     * @return
     */
    public static Object getTableNameList(PermissionCheckUserInfo userInfo, String tableName, long limit, long offset, Boolean Fuzzy) throws OtsException {
        TableNameListBody tableNameListModel = new TableNameListBody();

        try {
//            List<Long> noGetPermissionList = null;
//            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
//                // 过滤出id list
//                List<Table> permittedTables = ConfigUtil.getInstance().getOtsAdmin().getPermissionTables(userInfo.getUserId(), userInfo.getTenantId());
//
//                List<Long> byPermittedObjects = new ArrayList<>();
//                for (Table table : permittedTables) {
//                    byPermittedObjects.add(table.getTableId());
//                }
//                // 再调用AasPermissionUtil接口获取ConfigUtil.getInstance().getAuthServerAddr()，noGetPermissionList
//                try {
//                    noGetPermissionList = AasPermissionUtil.obtainNoGetPermissionInstanceList(ConfigUtil.getInstance().getAuthServerAddr(), userInfo, byPermittedObjects);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LOG.error(ExceptionUtils.getFullStackTrace(e));
//                }
//            }

            //获取表
            List<String> tableNameList = ConfigUtil.getInstance().getOtsAdmin().getTableNameList(userInfo.getTenantId(), tableName, limit, offset, Fuzzy);
            if (CollectionUtils.isEmpty(tableNameList)) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                        String.format("tenant (id:%d) didn't own any tables with table_name ~'%s'!", userInfo.getTenantId(), tableName));
            }


//            long tableId = table.getTableId();
//            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
//                PermissionUtil.CacheInfo cacheInfo = PermissionUtil.GetInstance()
//                        .otsPermissionHandler(userInfo, tableId, PermissionUtil.PermissionOpesration.READ);
//
//                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
//                        .sendData(userInfo.getTenantId(),MessageBuilder.buildPermissionMessage(userInfo, table.getTableId(), cacheInfo.isReadPermission(), cacheInfo.isWritePermission(), cacheInfo.isPermissionFlag(), cacheInfo.getCurrentTime()));
//            }

            tableNameListModel.fromTableNameList(tableNameList);

            //todo lyh cache未处理
//            // update cache
//            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableAllInfoBody);

        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

        return tableNameListModel;
    }

    /**
     * 对offset参数的校验和转换
     * @param offset
     * @param offset
     */
    public static long dealWithOffset(String offset) {
        if (StringUtils.isBlank(offset) || Long.parseLong(offset) < 0){
            offset = String.valueOf(RestConstants.DEFAULT_QUERY_OFFSET);
        }

        return Long.parseLong(offset);
    }

    public static long dealWithLimit(String limit) {
        if (StringUtils.isBlank(limit) || Long.parseLong(limit) < 0){
            limit = String.valueOf(RestConstants.DEFAULT_QUERY_LIMIT);
        }

        if (limit != null && Integer.parseInt(limit) > RestConstants.DEFAULT_QUERY_MAX_LIMIT) {
            LOG.info("limit is too large, set to " + RestConstants.DEFAULT_QUERY_MAX_LIMIT);
            limit = String.valueOf(RestConstants.DEFAULT_QUERY_MAX_LIMIT);
        }

        return Long.parseLong(limit);
    }


}
