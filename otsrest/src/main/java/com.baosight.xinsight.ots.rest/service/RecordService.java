package com.baosight.xinsight.ots.rest.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.client.Database.HBase.RecordQueryOption;
import com.baosight.xinsight.ots.client.Database.HBase.RecordResult;
import com.baosight.xinsight.ots.client.Database.HBase.RowCell;
import com.baosight.xinsight.ots.client.Database.HBase.RowRecord;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.ParamConstant;
import com.baosight.xinsight.ots.rest.constant.ParamErrorCode;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
import com.baosight.xinsight.ots.rest.model.record.request.RecordInfoListRequestBody;
import com.baosight.xinsight.ots.rest.model.table.vo.TableInfoVo;
import com.baosight.xinsight.ots.rest.util.ColumnsUtil;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.PrimaryKeyUtil;
import com.baosight.xinsight.ots.rest.util.TableConfigUtil;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class RecordService {
    /**
     * 插入记录
     * 如果记录原本存在，覆盖记录
     * @param userInfo
     * @param tableName
     * @param postBody
     * @return
     * @throws OtsException
     */
    public static ErrorMode insertRecords(PermissionCheckUserInfo userInfo,
                                          String tableName,
                                          JSONObject postBody) throws OtsException{
        ErrorMode rMode = new ErrorMode(0L);

        //read from cache
        TableInfoVo info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info != null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.EDIT);
        }

        //generate records
        List<RowRecord> records = generateRowRecords(userInfo.getTenantId(),tableName,postBody);

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
     * @param putBody
     * @return
     */
    public static ErrorMode updateRecords(PermissionCheckUserInfo userInfo,
                                          String tableName,
                                          JSONObject putBody) throws OtsException {
        return insertRecords(userInfo,tableName,putBody);
    }

    /**
     * 通过主键查询记录
     * @param userInfo
     * @param tableName
     * @param getBody
     * @return
     */
    public static RecordInfoListRequestBody getRecordsByPrimaryKey(PermissionCheckUserInfo userInfo,
                                                                   String tableName,
                                                                   JSONObject getBody) throws OtsException {

        TableInfoVo info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info!=null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.READ);
        }

        //查询创建表时的表结构
        Table table = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(),tableName);
        JSONArray schema_primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray schema_tableColumns = JSONArray.parseArray(table.getTableColumns());

        //body中的参数存到query中
        JSONArray primaryKeyInput = (JSONArray) getBody.get(ParamConstant.KEY_PRIMARY_KEY);
        RecordQueryOption query = generateRecordQueryOptionByRequestBody(getBody);

        //拼接rowkey的前缀
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
        }else {//范围查询
            records = ConfigUtil.getInstance().getOtsAdmin()
                    .getRecords(table.getTenantId(), query,  rowKeyRange.get(0), rowKeyRange.get(1));
        }

//        return recordInfoListRequestBody;

        System.out.println("pause");
        return null;
    }

    /**
     * 根据主键批量查询
     * @param userInfo
     * @param tableName
     * @param getBody
     * @return
     */
    public static Object getRecordsByPrimaryKeys(PermissionCheckUserInfo userInfo,
                                                 String tableName,
                                                 JSONObject getBody) throws OtsException{
        //

        return null;


    }

    /**
     * 将请求体中的参数放入查询条件中
     * @param getBody
     * @return
     */
    private static RecordQueryOption generateRecordQueryOptionByRequestBody(JSONObject getBody) {

        List<String> returnColumns = (List<String>) getBody.get(ParamConstant.KEY_RETURN_COLUMNS);
        Long limit = getBody.getLong(ParamConstant.KEY_LIMIT);
        String cursorMark = getBody.getString(ParamConstant.KEY_CURSOR_MARK);
        Boolean descending = ParamConstant.DEFAULT_DESCENING;
////        todo lyh queryFrom是什么？
//        Integer queryFrom = RestConstants.QUERY_FROM_TYPE_REST;

        //todo lyh 校验参数
        //必填项校验
        //参数合法性校验

        RecordQueryOption query = new RecordQueryOption(returnColumns,limit,cursorMark,descending);
        return query;
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
                                                      JSONObject body) throws OtsException {
        JSONArray recordsJSONArray = body.getJSONArray(ParamConstant.KEY_RECORDS);
        if(recordsJSONArray == null){//必填项
            throw new OtsException(ParamErrorCode.PARAM_RECORDS_IS_NULL);
        }

        //查询创建表时的表结构
        Table table = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(tenantId,tableName);
        JSONArray schema_primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray schema_tableColumns = JSONArray.parseArray(table.getTableColumns());

        List<RowRecord> records = new ArrayList<>();
        for (int i = 0; i < recordsJSONArray.size(); i++) {
            //one row
            RowRecord rowRecord = new RowRecord();
            //one data
            JSONObject recordJSONObject = recordsJSONArray.getJSONObject(i);

            //set rowKey
            byte[] rowKey = PrimaryKeyUtil.generateRowKey(table.getTableId(), schema_primaryKey, schema_tableColumns, recordJSONObject,false);
            rowRecord.setRowkey(rowKey);

            //set cell
            String cellkey = TableConstants.HBASE_TABLE_CELL; //todo lyh key应该是什么？
            String cellvalue = ColumnsUtil.generateCellValue(schema_tableColumns, recordJSONObject);
            RowCell rowCell = new RowCell(Bytes.toBytes(cellkey), Bytes.toBytes(cellvalue));
            rowRecord.addCell(rowCell);

            //add to the list
            records.add(rowRecord);
        }

        return records;
    }
}
