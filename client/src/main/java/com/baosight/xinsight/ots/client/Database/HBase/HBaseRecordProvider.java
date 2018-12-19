package com.baosight.xinsight.ots.client.Database.HBase;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.util.HBaseConnectionUtil;
import com.baosight.xinsight.ots.exception.OtsException;
import com.mchange.lang.ByteUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.DoNotRetryIOException;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.NotServingRegionException;
import org.apache.hadoop.hbase.RegionTooBusyException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public static void updateRecords(TableName tableName, List<RowRecord> records) throws IOException, TableException {
        insertRecords(tableName,records);
    }

    /**
     * 按范围查询表
     * @param query
     * @param startKey
     * @param endKey
     * @return
     */
    public static RecordResult getRecordsByRange(Table table,
                                                              RecordQueryOption query,
                                                              byte[] startKey,
                                                              byte[] endKey) throws DecoderException, IOException, TableException {
        Scan scan = new Scan();
        scan.addFamily(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME));


        byte[] realStartKey = startKey;
        if (!query.getCursor_mark().equals(OtsConstants.DEFAULT_QUERY_CURSOR_START)) {
            realStartKey = Hex.decodeHex(query.getCursor_mark().toCharArray());
            scan.setStartRow(realStartKey==null? HConstants.EMPTY_START_ROW:realStartKey);//important
        }

        if (query.hasCaching()) {
            scan.setCaching(query.getCaching());
        }

        if (query.isDescending()) {
            scan.setReversed(true);//important
            scan.setStartRow(endKey==null?HConstants.EMPTY_END_ROW:endKey);
            scan.setStopRow(realStartKey==null?HConstants.EMPTY_START_ROW:realStartKey);
        } else {
            scan.setStartRow(realStartKey==null?HConstants.EMPTY_START_ROW:realStartKey);
            scan.setStopRow(endKey==null?HConstants.EMPTY_END_ROW:endKey);
        }

        if (query.hasReturnColumns()) {
            List<String> returnColumns = query.getReturnColumns();
            boolean onlyRowKey = query.onlyGetRowKey();
            if (!onlyRowKey) {
                for (int i = 0; i < returnColumns.size(); i++) {
                    System.out.println(returnColumns.get(i).getClass());

                    byte[] col = returnColumns.get(i).getBytes();
                    scan.addColumn(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME), col);
                }
                scan.setBatch(returnColumns.size());
            }
        }

        if (query.hasIterate()) {
            return queryRecordsByCursorMark(table, query, scan);
        }else{
            //目前不支持limit和offset这种场景
            return null;
        }
    }

    /**
     * 以cursor mark的方式查询
     * @param table
     * @param query
     * @param scan
     * @return
     * @throws TableException
     * @throws IOException
     */
    private static RecordResult queryRecordsByCursorMark(Table table,
                                                         RecordQueryOption query,
                                                         Scan scan) throws TableException, IOException {

        long counter = 0;
        long matchCounter = 0;
        List<RowRecord> recordList = new ArrayList<>();
        ResultScanner rs = null;
        boolean onlyRowKey = query.onlyGetRowKey();
        byte[] next_rowKey = null;
        byte[] last_rowKey = null;

        try {
            rs = table.getScanner(scan);
            Result rec = rs.next();
            while (rec != null) {
                if (!Arrays.equals(last_rowKey, rec.getRow())) {
                    counter++;
                    last_rowKey = rec.getRow();
                }

                RowRecord recModel = readRow(onlyRowKey, rec);

                //limit
                if(counter <= query.getLimit()) {
                    RowRecord latestRecModel = recordList.size() > 0 ? recordList.get(recordList.size() -1):null;
                    if (latestRecModel != null && Arrays.equals(latestRecModel.getRowkey(), rec.getRow())) {
                        for (RowCell c : recModel.getCellList()) {
                            //if (c != null && !Arrays.equals(c.getName(), Bytes.toBytes(OtsConstants.DEFAULT_ROWKEY_NAME))) {
                            if (c != null) {
                                latestRecModel.addCell(c);
                            }
                        }
                    } else {
                        matchCounter++;
                        recordList.add(recModel);
                    }
                }

                // Check limit
                if(counter >= query.getLimit()) {
                    //calculate next row key
                    rec = rs.next();
                    if (rec != null)
                        next_rowKey = rec.getRow();

                    break;
                }

                rec = rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();

            if ( !(e instanceof DoNotRetryIOException)) {

                if (e.getCause() instanceof NotServingRegionException) {
                    throw new TableException(OtsErrorCode.EC_OTS_STORAGE_OPERATE_RECORD_NOSERVINGREGINON, "no serving region exception, you may retry the operation!");
                } else if (e.getCause() instanceof RegionTooBusyException) {
                    throw new TableException(OtsErrorCode.EC_OTS_STORAGE_OPERATE_RECORD_REGINONTOOBUSY, "region too busy exception, you may retry the operation!");
                }
            }
            throw new TableException(OtsErrorCode.EC_OTS_STORAGE_RECORD_QUERY, "query Record error!");

        } finally {
            if (rs != null) {
                rs.close();
            }
            table.close();
        }

        String strNextRowkey = OtsConstants.DEFAULT_QUERY_CURSOR_START;
        if (next_rowKey != null) {
            strNextRowkey = Hex.encodeHexString(next_rowKey);
        }
        return new RecordResult(matchCounter, strNextRowkey, recordList);
    }

    /**
     *
     * @param onlyRowKey
     * @param rec
     * @return
     */
    private static RowRecord readRow(boolean onlyRowKey, Result rec) {
        RowRecord recModel = new RowRecord();
        boolean rowKey = false;
        for(Cell cell: rec.rawCells()) {
            if (cell != null) {
                if (!rowKey) {
                    recModel.setRowkey(CellUtil.cloneRow(cell));
                    rowKey = true;
                }

                if (!onlyRowKey) {
                    RowCell cellModel = new RowCell();
                    cellModel.setName(CellUtil.cloneQualifier(cell));
                    cellModel.setValue(CellUtil.cloneValue(cell));
                    recModel.addCell(cellModel);
                }
            }
        }

        return recModel;
    }
}
