package com.baosight.xinsight.ots.client.Database.HBase;

import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class HBaseRecordProvider {

    /**
     * 根据表名删除所有属于该小表的记录
     * @param admin
     * @param tableName
     * @param tableId
     */
    public static void deleteAllRecordByTableId(Admin admin, TableName tableName, Long tableId) throws OtsException {
        //todo lyh HBase批量删除数据

    }
}
