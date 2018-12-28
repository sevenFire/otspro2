package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.kafka.MessageHandlerFactory;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.OtsTable;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.model.table.operate.TableColumnsBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableUpdateBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoListBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableNameListBody;
import com.baosight.xinsight.ots.rest.permission.CachePermission;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.MessageBuilder;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.TableConfigUtil;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
            //将权限信息并发送到kafka，让其他节点更新缓存
            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.CacheInfo info = PermissionUtil.GetInstance().otsPermissionHandler(userInfo, -1, PermissionUtil.PermissionOpesration.OTSMANAGEW);

                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(), MessageBuilder.buildPermissionMessage(userInfo, RestConstants.OTS_MANAGE_PERMISSION_OPERATION,
                                info.isReadPermission(), info.isWritePermission(), info.isPermissionFlag(), info.getCurrentTime()));
                LOG.debug("messageHandlerFactory: message" + MessageBuilder.buildPermissionMessage(userInfo, RestConstants.OTS_MANAGE_PERMISSION_OPERATION,
                        info.isReadPermission(), info.isWritePermission(), info.isPermissionFlag(), info.getCurrentTime()));
            }

            //创建表（包含大表和小表），以及表存在性校验
            OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().createTable(userInfo.getUserId(), userInfo.getTenantId(), tableName,tableCreateBody.toTable());

            //添加到缓存中
            TableInfoBody info = fromTableToBody(otsTable);
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

    private static TableInfoBody fromTableToBody(OtsTable otsTable) {
        TableInfoBody tableInfoBody = new TableInfoBody();
        Table table = null;
        try {
            table = otsTable.getInfo();
        } catch (ConfigException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tableInfoBody.setTableId(table.getTableId());
        tableInfoBody.setTableName(table.getTableName());
        tableInfoBody.setTableDesc(table.getTableDesc());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {            
            List<TableColumnsBody> tableColumnsBodies = objectMapper.readValue(table.getTableColumns(), new TypeReference<List<TableColumnsBody>>() {});
            tableInfoBody.setTableColumns(tableColumnsBodies);
            
            List<String> primaryKeyBodies = objectMapper.readValue(table.getPrimaryKey(), new TypeReference<List<String>>() {});
            tableInfoBody.setPrimaryKey(primaryKeyBodies);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间格式化的格式
        tableInfoBody.setCreateTime(sDateFormat.format(table.getCreateTime()));
        tableInfoBody.setModifyTime(sDateFormat.format(table.getModifyTime()));
        tableInfoBody.setCreator(table.getCreator());
        tableInfoBody.setModifier(table.getModifier());

        return tableInfoBody;
    }


    public static void main(String[] args) {
        String columns = "[{\"col_name\":\"col1\"}]";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<TableColumnsBody> tableColumnsBodies = objectMapper.readValue(columns, new TypeReference<List<TableColumnsBody>>() {
            });
            System.out.println(tableColumnsBodies);
        }catch (IOException e) {
            e.printStackTrace();
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
            OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().updateTable(userInfo.getUserId(), userInfo.getTenantId(), tableName,tableUpdateBody.toTable());

            //更新缓存
            //添加到缓存中
            TableInfoBody info = fromTableToBody(otsTable);
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
            OtsTable table = ConfigUtil.getInstance().getOtsAdmin().deleteTable(userInfo.getTenantId(), tableName);

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
    public static TableInfoBody getTableInfo(PermissionCheckUserInfo userInfo,
                                             String tableName) throws OtsException, IOException {
        TableInfoBody tableInfoBody = new TableInfoBody();

        try {

            //获取表信息，带有权限筛选。 permission=true的才会被查出来
            OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getPermissionTable(userInfo.getTenantId(),tableName);
            if (otsTable == null) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                        String.format("tenant (id:%d) was not owned table:%s!", userInfo.getTenantId(), tableName));
            }

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                //判定并获取权限信息
                PermissionUtil.CacheInfo cacheInfo = PermissionUtil.GetInstance()
                        .otsPermissionHandler(userInfo, otsTable.getTableId(), PermissionUtil.PermissionOpesration.READ);
                //通过kafka将权限信息发送
                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(),MessageBuilder.buildPermissionMessage(userInfo, otsTable.getTableId(), cacheInfo.isReadPermission(), cacheInfo.isWritePermission(), cacheInfo.isPermissionFlag(), cacheInfo.getCurrentTime()));
            }

            //更新缓存
            tableInfoBody = fromTableToBody(otsTable);
            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableInfoBody);

        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

        //返回
        LOG.debug("RETURN:" + tableInfoBody.toString());
        return tableInfoBody;
    }

    /**
     * 获取所有表的详细信息
     * @param userInfo
     * @return
     */
    public static TableInfoListBody getAllTableInfoList(PermissionCheckUserInfo userInfo,
                                             long limit,
                                             long offset) throws IOException, ConfigException {
        TableInfoListBody tableInfoListBody = new TableInfoListBody();

        try {
            //权限筛选，获取筛选后的表Id列表
            List<Long> permittedIds = getPermittedIds(userInfo);
            if (permittedIds.size() == 0){
                return tableInfoListBody;
            }
            //获取筛选后的表
            List<OtsTable> otsTableList = ConfigUtil.getInstance().getOtsAdmin()
                    .getAllOtsTablesWithPermission(userInfo.getTenantId(),limit, offset,permittedIds);
            for (OtsTable otsTable : otsTableList) {
                TableInfoBody tableInfoBody = fromTableToBody(otsTable);
                tableInfoListBody.addTable(tableInfoBody);
                //update cache
                TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableInfoBody);
            }
        }catch (ConfigException e) {
            throw e;
        }
        tableInfoListBody.setErrcode(0L);
        tableInfoListBody.setTotalCount(tableInfoListBody.getTableInfoList().size());

        LOG.debug("RETURN:" + tableInfoListBody.toString());
        return tableInfoListBody;
    }



    /**
     * 获取表名的列表，不带查询条件，查询出全部。
     * @param userInfo
     * @param limit
     * @param offset
     * @return
     */
    public static TableNameListBody getAllTableNameList(PermissionCheckUserInfo userInfo, long limit, long offset) throws OtsException {
        return (TableNameListBody) getTableNameList(userInfo,null,limit,offset,true);
    }

    /**
     * 获取表名的列表，带tableName的查询条件
     * @param userInfo
     * @param name 模糊查询条件
     * @param limit
     * @param offset
     * @return
     */
    public static Object getTableNameList(PermissionCheckUserInfo userInfo,
                                          String name,
                                          long limit,
                                          long offset,
                                          Boolean Fuzzy) throws OtsException {
        TableNameListBody tableNameListBody = new TableNameListBody();

        try {

            List<Long> permittedIds = getPermittedIds(userInfo);
            if (permittedIds.size() == 0){
                return tableNameListBody;
            }

            //获取表
            List<OtsTable> otsTableList = ConfigUtil.getInstance().getOtsAdmin()
                    .getAllOtsTablesWithPermission(userInfo.getTenantId(), name, limit, offset, Fuzzy, permittedIds);
            if (CollectionUtils.isEmpty(otsTableList)) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                        String.format("tenant (id:%d) didn't own any tables with table_name ~'%s'!", userInfo.getTenantId(), name));
            }

            for (OtsTable otsTable :otsTableList){
                TableInfoBody tableInfoBody = fromTableToBody(otsTable);
                tableNameListBody.addTableName(otsTable.getTableName());

                TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableInfoBody);
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

        tableNameListBody.setErrcode(0L);
        tableNameListBody.setTotalCount(tableNameListBody.getTableNames().size());
        return tableNameListBody;
    }

    private static List<Long> getPermittedIds(PermissionCheckUserInfo userInfo) throws ConfigException {
        //todo lyh
//        List<Long> noGetPermissionList = null;
//        if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
//            // 过滤出id list
//            List<Long> permittedIds = ConfigUtil.getInstance().getOtsAdmin().getPermissionTableIds(userInfo.getTenantId());
//            // 再调用AasPermissionUtil接口获取ConfigUtil.getInstance().getAuthServerAddr()，noGetPermissionList
//            try {
//                noGetPermissionList = AasPermissionUtil.obtainNoGetPermissionInstanceList(ConfigUtil.getInstance().getAuthServerAddr(), userInfo, permittedIds);
//            } catch (Exception e) {
//                e.printStackTrace();
//                LOG.error(ExceptionUtils.getFullStackTrace(e));
//            }
//        }
//
//        return noGetPermissionList;

        List<Long> permittedIds = new ArrayList<>();
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
            // 过滤出id list
            permittedIds = ConfigUtil.getInstance().getOtsAdmin().getPermissionTableIds(userInfo.getTenantId());
        }

        return permittedIds;

        //null表示无权限限制，size=0表示没有满足权限要求的记录。

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
