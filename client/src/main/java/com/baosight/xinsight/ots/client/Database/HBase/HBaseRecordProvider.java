package com.baosight.xinsight.ots.client.Database.HBase;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.util.HBaseConnectionUtil;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.RegionTooBusyException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 根据表名插入记录
     * @param tableName
     * @param records
     */
    public static void insertRecords(TableName tableName, List<RowRecord> records) throws TableException, IOException {
        //判定表是否存在
        Table table = HBaseConnectionUtil.getInstance().getTable(tableName);
        if (table == null){
            throw new TableException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, "table is not exist!");
        }

        int bufferSize = OtsConstants.DEFAULT_META_SCANNER_CACHING; //avoid running out of memory
        table.setWriteBufferSize(OtsConstants.DEFAULT_CLIENT_WRITE_BUFFER);

        List<Put> putList = new ArrayList<>();
        try {
            for(int i = 0; i < records.size(); i++) {
                RowRecord record = records.get(i);
                if (record.hasRowkey()) { //important
                    Put put = new Put(record.getRowkey());

                    List<RowCell> cellList = record.getCellList();
                    for (RowCell cell : cellList) {
                        put.addColumn(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME), cell.getName(), cell.getValue());
                    }
                    putList.add(put);
                }

                if(0 == i % bufferSize) //avoid running out of memory
                {
                    table.put(putList);
                    putList.clear();
                }
            }

            if(putList.size() > 0) {
                table.put(putList);
                putList.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();

            if ( !(e instanceof DoNotRetryIOException)) {

                if (e.getCause() instanceof NotServingRegionException) {
                    throw new TableException(OtsErrorCode.EC_OTS_STORAGE_OPERATE_RECORD_NOSERVINGREGINON, "no serving region exception, you may retry the operation!");
                } else if (e.getCause() instanceof RegionTooBusyException) {
                    throw new TableException(OtsErrorCode.EC_OTS_STORAGE_OPERATE_RECORD_REGINONTOOBUSY, "region too busy exception, you may retry the operation!");
                }
            }
            throw new TableException(OtsErrorCode.EC_OTS_STORAGE_RECORD_INSERT, "insert Record error!");

        } finally {
            table.close();
        }

    }
}
