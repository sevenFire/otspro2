package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
import com.baosight.xinsight.ots.rest.model.metrics.response.MetricsInfoBody;
import com.baosight.xinsight.ots.rest.model.metrics.response.MetricsInfoListBody;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description
 */
public class MetricsService {
    private static final Logger LOG = Logger.getLogger(MetricsService.class);

    /**
     * 获取监控信息
     * @param tenantId
     * @param tableName
     * @return
     */
    public static MetricsInfoBody getMetricsInfoByTableName(long tenantId, String tableName){
        MetricsInfoBody metricsInfoBody = new MetricsInfoBody(tableName);

        //租户10001下的表a，存于redis中的key格式为：ots_metrics_10001:a
        String readCount = getMetricsValue(tenantId, tableName, RestConstants.METRICS_TABLE_READ_COUNT);
        String writeCount = getMetricsValue(tenantId, tableName, RestConstants.METRICS_TABLE_WRITE_COUNT);
        String diskSize = getMetricsValue(tenantId, tableName, RestConstants.METRICS_TABLE_DISK_SIZE);
        String recordCount = getMetricsValue(tenantId, tableName, RestConstants.METRICS_TABLE_RECORD_COUNT);

        metricsInfoBody.setReadCount(dealRedisValue(readCount));
        metricsInfoBody.setWriteCount(dealRedisValue(writeCount));
        metricsInfoBody.setRecordCount(dealRedisValue(recordCount));
        metricsInfoBody.setDiskSize(dealRedisValue(diskSize));

        return metricsInfoBody;
    }

    public static MetricsInfoListBody getMetricsInfoByNamespace(long tenantId) throws OtsException {
        MetricsInfoListBody metricsInfoListBody = new MetricsInfoListBody();

        //获取该租户下的所有表
        //todo lyh 这里是否有权限要求，能获取所有的表吗
        List<String> tableNameList;
        try {
            tableNameList = ConfigUtil.getInstance().getOtsAdmin().getTableNameListWithPermission(tenantId);
            if (CollectionUtils.isEmpty(tableNameList)){
                throw new OtsException(RestErrorCode.EC_OTS_REST_METRICS_NAMESPACE, String.format("there is no permitted table in the namespace (tenantId = '%d')", tenantId));
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw new OtsException(RestErrorCode.EC_OTS_REST_METRICS_NAMESPACE, String.format("Get namespace %d metrics info error!", tenantId));
        }

        for (String tableName : tableNameList){
            MetricsInfoBody metricsInfoBody = getMetricsInfoByTableName(tenantId,tableName);
            metricsInfoListBody.getMetricsInfoBodyList().add(metricsInfoBody);
        }

        LOG.debug("RETURN:" + tableNameList.toString());

        return metricsInfoListBody;
    }

    /**
     * 验证namespace是否存在
     */
    public static boolean isNamespaceExist(long tenantId) throws OtsException {
        try {
            return ConfigUtil.getInstance().getOtsAdmin().isNamespaceExist(tenantId);
        } catch (OtsException e) {
            throw e;
        }
    }


    /**
     * 为null时返回默认值0。
     * @param value
     * @return
     */
    private static Long dealRedisValue(String value) {
        return value != null ? Long.parseLong(value) : 0;
    }

    /**
     * 根据字段名拼接在redis中的值
     * @param tenantId
     * @param tableName
     * @param propertyName
     * @return
     */
    private static String getMetricsValue(long tenantId, String tableName, String propertyName) {
        //拼接完整字段
        String key =  RestConstants.DEFAULT_METRICS_PREFIX + String.valueOf(tenantId) + TableName.NAMESPACE_DELIM + tableName;
        return ConfigUtil.getInstance().getRedisUtil().getHSet(key, propertyName);
    }
}
