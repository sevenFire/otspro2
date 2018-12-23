package com.baosight.xinsight.ots.client.util;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.constants.TableConstants;

import org.apache.hadoop.hbase.TableName;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class TableNameUtil {
    /**
     * 1:ots_1_indexname_ots_idx
     * @param tableFullName 原表表名全程
     * @param indexName 索引名
     * @return
     */
    public static TableName generateHBaseIndexTableName(String tableFullName, String indexName) {
        return TableName.valueOf(tableFullName + OtsConstants.OTS_INDEX_TABLE_SPLIT + indexName + OtsConstants.OTS_INDEX_TABLE_SUFFIX);
    }


    /**
     * 根据租户id拼接生成HBase中真正的表名
     * 1:ots_${tenantId}
     * @param tenantId
     * @return
     */
    public static String generateHBaseTableName(Long tenantId) {
        return (TableConstants.HBASE_TABLE_PREFIX + String.valueOf(tenantId));
    }




}
