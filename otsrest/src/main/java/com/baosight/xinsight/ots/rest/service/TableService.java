package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.kafka.MessageHandlerFactory;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.client.OTSTable;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.api.TableResource;
import com.baosight.xinsight.ots.rest.common.RestConstants;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.MessageBuilder;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.vo.table.operate.TableCreateBodyVo;

import org.apache.log4j.Logger;

import javax.ws.rs.Path;

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
    public static OTSTable createTable(PermissionCheckUserInfo userInfo, String tableName, TableCreateBodyVo createBodyModel) throws Exception {
        try {
            //存入userInfo到缓存
            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.CacheInfo info = PermissionUtil.GetInstance().otsPermissionHandler(userInfo, -1, PermissionUtil.PermissionOpesration.OTSMANAGEW);
                MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                        .sendData(userInfo.getTenantId(), MessageBuilder.buildPermissionMessage(userInfo, RestConstants.OTS_MANAGE_PERMISSION_OPERATION,
                                info.isReadPermission(), info.isWritePermission(), info.isPermissionFlag(), info.getCurrentTime()));
            }

            //创建表（包含大表和小表），以及表存在性校验
            OTSTable table = ConfigUtil.getInstance().getOtsAdmin()
                    .createTable(userInfo.getUserId(), userInfo.getTenantId(), tableName,createBodyModel.toTable());

            //add cache
            //todo lyh 存入cache的格式是什么？
//            TableInfoModel info = new TableInfoModel(table.getTableName());
//            info.setKeyType(table.getKeyType());
//            info.setHashKeyType(table.getHashKeyType());
//            info.setRangeKeyType(table.getRangeKeyType());
//            TableConfigUtil.addTableConfig(userInfo.getUserId(), userInfo.getTenantId(), info);

            //Kafka produces config message
            MessageHandlerFactory.getMessageProducer(CommonConstants.OTS_CONFIG_TOPIC)
                    .sendData(userInfo.getTenantId(), MessageBuilder.buildConfigMessage(0, userInfo.getTenantId(), userInfo.getUserId(), tableName));

            return table;
        } catch (TableException e) {
            throw e;
        } catch (ConfigException e) {
            throw e;
        } catch (OtsException e) {
            throw e;
        }
    }





}
