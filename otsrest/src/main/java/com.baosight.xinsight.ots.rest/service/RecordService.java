package com.baosight.xinsight.ots.rest.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.Database.HBase.RecordQueryOption;
import com.baosight.xinsight.ots.client.Database.HBase.RecordResult;
import com.baosight.xinsight.ots.client.Database.HBase.RowCell;
import com.baosight.xinsight.ots.client.Database.HBase.RowRecord;
import com.baosight.xinsight.ots.client.OtsIndex;
import com.baosight.xinsight.ots.client.OtsTable;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.common.util.ColumnsUtil;
import com.baosight.xinsight.ots.common.util.PrimaryKeyUtil;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
import com.baosight.xinsight.ots.rest.model.record.request.RecordColumnsBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordInfoListRequestBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordListBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordQueryBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordQueryListBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoBody;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.TableConfigUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class RecordService {

    private static final Logger LOG = Logger.getLogger(RecordService.class);

    public static ErrorMode deleteAllRecords(PermissionCheckUserInfo userInfo, String tableName) {
        ErrorMode rmodel = new ErrorMode(0L);

        try {
            OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(), tableName);
//            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
//                PermissionUtil.GetInstance().otsPermissionHandler(userInfo, otsTable.getTableId(), PermissionUtil.PermissionOpesration.EDIT);
//            }
            if (otsTable == null) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, "table not exist!");
            }

            //清空表：删除RDB中小表，删除HBase中相关记录
            ConfigUtil.getInstance().getOtsAdmin().truncate(otsTable.getTenantId(),otsTable.getTableId());

            //todo lyh 删除索引
//            // truncate the online index data
//            List<OtsIndex> listIndex = otsTable.getAllIndexes();
//            for (OtsIndex otsIndex : listIndex) {
//                if (TableConstants.OTS_INDEX_TYPE_HBASE_STRING == otsIndex.getIndexType()) {
//                    otsIndex.truncate();
//                }else{
//                    //todo lyh
//                    //es 暂不处理
//                }
//            }
        } catch (TableException e) {
            e.printStackTrace();
            LOG.error("Failed to truncate table for!\n" + e.getMessage());
            rmodel.setError_code(e.getErrorCode());
            rmodel.setErrinfo(e.getMessage());
        } catch (OtsException e) {
            e.printStackTrace();
            LOG.error("Failed to truncate table for!\n" + e.getMessage());
            rmodel.setError_code(e.getErrorCode());
            rmodel.setErrinfo(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            long errorcode = OtsErrorCode.EC_OTS_STORAGE_TABLE_UPDATE;
            LOG.error("Failed to truncate table!" + e.getMessage());
            rmodel.setError_code(errorcode);
            rmodel.setErrinfo(e.getMessage());
        }
        LOG.debug("RETURN:" + rmodel.toString());
        return rmodel;
    }

    /**
     * 插入记录
     * 如果记录原本存在，覆盖记录
     * @param userInfo
     * @param tableName
     * @param recordListBody
     * @return
     * @throws OtsException
     */
    public static ErrorMode insertRecords(PermissionCheckUserInfo userInfo,
                                          String tableName,
                                          RecordListBody recordListBody) throws OtsException, IOException, SQLException {
        ErrorMode rMode = new ErrorMode(0L);

        //read from cache
        TableInfoBody info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info != null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.EDIT);
        }

        //generate records
        List<RowRecord> records = generateRowRecords(userInfo.getTenantId(),tableName,recordListBody);

        //add records in the big table
        try {
            ConfigUtil.getInstance().getOtsAdmin().insertRecords(userInfo.getTenantId(), records);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(RestErrorCode.EC_OTS_INSERT_RECORDS, e.getMessage());
        }
        return rMode;
    }

    /**
     * 更新记录
     * 如果记录原本不存在，则插入
     * @param userInfo
     * @param tableName
     * @param recordListBody
     * @return
     */
    public static ErrorMode updateRecords(PermissionCheckUserInfo userInfo,
                                          String tableName,
                                          RecordListBody recordListBody) throws OtsException, IOException, SQLException {
        return insertRecords(userInfo,tableName,recordListBody);
    }

    /**
     * 根据主键批量查询
     * @param userInfo
     * @param tableName
     * @param getBody
     * @return
     */
    public static RecordListBody getRecordsByPrimaryKeys(PermissionCheckUserInfo userInfo,
                                                 String tableName,
                                                 RecordQueryListBody getBody) throws OtsException, IOException, SQLException {
        TableInfoBody info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info!=null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.READ);
        }

        OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(),tableName);
        if (otsTable == null){
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_DELETE,
                    String.format("table(table_name %s) in tenant(tenant_id:%d) is not exist!\n", tableName, userInfo.getTenantId()));
        }
        Table table = otsTable.getInfo();
        //查询创建表时的表结构：主键信息和列信息（列名和列类型）
        JSONArray schema_primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray schema_tableColumns = JSONArray.parseArray(table.getTableColumns());

        //body中的主键参数
        List<JSONObject> primaryKeyInputList = getBody.getPrimaryKeyList();

        //body中的其他查询条件
        RecordQueryOption query = new RecordQueryOption(getBody.getReturnColumns(),
                getBody.getLimit(),
                getBody.getCursorMark(),
                ParamConstant.DEFAULT_DESCENING);


        //拼接rowKey的前缀
        List<byte[]> rowKeyBatch = PrimaryKeyUtil.generateRowKeyBatch(table.getTableId(),
                                                                        schema_primaryKey,
                                                                        schema_tableColumns,
                                                                        primaryKeyInputList);

        //HBase批量查询
        RecordResult recordResult = ConfigUtil.getInstance().getOtsAdmin().getRecordsBatch(table.getTenantId(), rowKeyBatch, query);

        RecordListBody recordListBody = new RecordListBody();

        //对结果进行解析
        for (int i = 0; i < recordResult.size(); i++) {
            byte[] rowKey = recordResult.getRecordList().get(i).getRowkey();

            List<RowCell> rowCellList = recordResult.getRecordList().get(i).getCellList();
            if (rowCellList.size() != 1){
                throw new TableException(OtsErrorCode.EC_OTS_STORAGE_RECORD_QUERY,"查询的结果有误，列数不对。");
            }
            // md5(4) + tableId + value1 + （len1） + ...
            byte[] cellValue = rowCellList.get(0).getValue();

            //将每个列的值解析出来
            JSONObject record = ColumnsUtil.generateColumnValue(schema_primaryKey,schema_tableColumns,getBody.getReturnColumns(),cellValue,rowKey);
            recordListBody.addRecord(record);
        }

        recordListBody.setErrCode(0L);
        recordListBody.setTotalCount(recordListBody.getRecordList().size());

        return recordListBody;
    }


    /**
     * 通过主键（包含范围）查询记录
     * @param userInfo
     * @param tableName
     * @param getBody
     * @return
     */
    public static RecordInfoListRequestBody getRecordsByPrimaryKey(PermissionCheckUserInfo userInfo,
                                                                   String tableName,
                                                                   RecordQueryBody getBody) throws OtsException, IOException, SQLException {

        TableInfoBody info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info!=null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.READ);
        }

        OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(),tableName);
        if (otsTable == null){
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_DELETE,
                    String.format("table(table_name %s) in tenant(tenant_id:%d) is not exist!\n", tableName, userInfo.getTenantId()));
        }
        Table table = otsTable.getInfo();
        //查询创建表时的表结构：主键信息和列信息（列名和列类型）
        JSONArray schema_primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray schema_tableColumns = JSONArray.parseArray(table.getTableColumns());

        //body中的主键参数
        List<RecordColumnsBody> primaryKeyInputBody = getBody.getPrimaryKey();
        Map<String,JSONObject> primaryKeyInput = dealWithPrimaryKeyPrefix(primaryKeyInputBody);

        //body中的其他查询条件
        RecordQueryOption query = new RecordQueryOption(getBody.getReturnColumns(),
                                                        getBody.getLimit(),
                                                        getBody.getCursorMark(),
                                                        ParamConstant.DEFAULT_DESCENING);


        //拼接rowKey的前缀
        List<byte[]> rowKeyRange = PrimaryKeyUtil.generateRowKeyRange(table.getTableId(),
                                                                      schema_primaryKey,
                                                                      schema_tableColumns,
                                                                      primaryKeyInput);
        //query
//        RecordInfoListRequestBody recordInfoListRequestBody = new RecordInfoListRequestBody();
//        List<RecordInfoBody> recordInfoBodyList = new ArrayList<>();
//        List<Map<String,String>> records;
//        recordInfoListRequestBody.setRecords(recordInfoBodyList);

        RecordResult records;
        if (rowKeyRange.size() == 1){//只有startKey
             records = ConfigUtil.getInstance().getOtsAdmin()
                     .getRecords(table.getTenantId(), query,  rowKeyRange.get(0),null);
        }else {//startKey和endKey都有
            records = ConfigUtil.getInstance().getOtsAdmin()
                    .getRecords(table.getTenantId(), query,  rowKeyRange.get(0), rowKeyRange.get(1));
        }

//        return recordInfoListRequestBody;


        //todo lyh查不到结果
        System.out.println("pause");
        return null;
    }

    /**
     * 校验参数并转换列格式
     * @param primaryKeyInputBody
     * @return
     */
    private static Map<String, JSONObject> dealWithPrimaryKeyPrefix(List<RecordColumnsBody> primaryKeyInputBody) throws OtsException {
        Map<String,JSONObject> primaryKeyInput = new HashMap<>();
        JSONObject recordStart = new JSONObject();
        JSONObject recordEnd = new JSONObject();

        //判断是否是范围查询
        Boolean rangePrefix = false;

        for (int i = 0; i < primaryKeyInputBody.size(); i++) {
            RecordColumnsBody recordColumnsBody = primaryKeyInputBody.get(i);

            if (i == primaryKeyInputBody.size()-1 ){//长度为1时i=0有特殊情况，长度不为1时i=2是有特殊情况
                if (recordColumnsBody.getColValue() != null){
                    recordStart.put(recordColumnsBody.getColName(),recordColumnsBody.getColValue());
                    recordEnd.put(recordColumnsBody.getColName(),recordColumnsBody.getColValue());
                }else if(recordColumnsBody.getColStart() !=null && recordColumnsBody.getColEnd() != null){
                    rangePrefix = true;
                    recordStart.put(recordColumnsBody.getColName(),recordColumnsBody.getColStart());
                    recordEnd.put(recordColumnsBody.getColName(),recordColumnsBody.getColEnd());
                }
            }else if (recordColumnsBody.getColValue() == null){
                throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
            }else {
                recordStart.put(recordColumnsBody.getColName(),recordColumnsBody.getColValue());
                recordEnd.put(recordColumnsBody.getColName(),recordColumnsBody.getColValue());
            }
        }

        primaryKeyInput.put(ParamConstant.RECORD_START,recordStart);
        if (rangePrefix){
            primaryKeyInput.put(ParamConstant.RECORD_END,recordEnd);
        }

        return primaryKeyInput;
    }

    /**
     * 转换生成要插入/更新的记录格式
     * @param tenantId
     * @param tableName
     * @param body
     * @return
     */
    private static List<RowRecord> generateRowRecords(long tenantId,
                                                      String tableName,
                                                      RecordListBody body) throws OtsException, IOException, SQLException {
        List<JSONObject> recordList = body.getRecordList();
        if(recordList == null || recordList.size() == 0){//必填项
            throw new OtsException(ParamErrorCode.PARAM_RECORDS_IS_NULL);
        }

        //查询创建表时的表结构
        OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(tenantId,tableName);
        if (otsTable == null){
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_DELETE,
                    String.format("table(table_name %s) in tenant(tenant_id:%d) is not exist!\n", tableName, tenantId));
        }
        Table table = otsTable.getInfo();
        JSONArray schema_primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray schema_tableColumns = JSONArray.parseArray(table.getTableColumns());

        List<RowRecord> records = new ArrayList<>();
        for (int i = 0; i < recordList.size(); i++) {
            //one real row
            RowRecord rowRecord = new RowRecord();
            //one input data
            JSONObject record = recordList.get(i);

            //set rowKey
            byte[] rowKey = PrimaryKeyUtil.generateRowKey(table.getTableId(), schema_primaryKey, schema_tableColumns, record,false);
            rowRecord.setRowkey(rowKey);

            //set cell
            String cellKey = TableConstants.HBASE_TABLE_CELL; //todo lyh cell key应该是什么？
            //cellValue里不存主键
            byte[] cellValue = ColumnsUtil.generateCellValue(schema_primaryKey, schema_tableColumns, record);
            RowCell rowCell = new RowCell(Bytes.toBytes(cellKey), cellValue);
            rowRecord.addCell(rowCell);

            //add to the list
            records.add(rowRecord);
        }

        return records;
    }

}
