package com.baosight.xinsight.ots.rest.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.client.Database.HBase.RowCell;
import com.baosight.xinsight.ots.client.Database.HBase.RowRecord;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.ParamConstant;
import com.baosight.xinsight.ots.rest.constant.ParamErrorCode;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
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
    public static ErrorMode insertRecords(PermissionCheckUserInfo userInfo, String tableName, JSONObject postBody) throws OtsException{
        ErrorMode rMode = new ErrorMode(0L);

        //read from cache
        TableInfoVo info = TableConfigUtil.getTableConfig(userInfo.getUserId(), userInfo.getTenantId(), tableName);
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null && info != null) {
            PermissionUtil.GetInstance().otsPermissionHandler(userInfo, info.getTableId(), PermissionUtil.PermissionOpesration.EDIT);
        }

        JSONArray recordsJSONArray = postBody.getJSONArray(ParamConstant.KEY_RECORDS);
        if(recordsJSONArray == null){//必填项
            throw new OtsException(ParamErrorCode.PARAM_RECORDS_IS_NULL);
        }

        //查询创建表时的表结构
        Table table = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(userInfo.getTenantId(),tableName);
        JSONArray primaryKey = JSONArray.parseArray(table.getPrimaryKey());
        JSONArray tableColumns = JSONArray.parseArray(table.getTableColumns());

        List<RowRecord> records = new ArrayList<>();
        for (int i = 0; i < recordsJSONArray.size(); i++) {
            //one row
            RowRecord rec = new RowRecord();

            JSONObject record = recordsJSONArray.getJSONObject(i);

            //设置行键
            byte[] rowkey = PrimaryKeyUtil.generateRowkey(table.getTableId(), primaryKey,record);
            rec.setRowkey(rowkey);

            //设置cell
            String cellkey = "all"; //todo lyh key应该是什么？
            String cellvalue = ColumnsUtil.generateCellValue(tableColumns, record);
            RowCell c = new RowCell(Bytes.toBytes(cellkey), Bytes.toBytes(cellvalue));
            rec.addCell(c);

            //add to the list
            records.add(rec);
        }

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
     *
     * @param record
     */
    private static void validateRequiredFields(JSONObject record) throws OtsException {
        if (!record.containsKey(ParamConstant.KEY_PRIMARY_KEY)){
            throw new OtsException(ParamErrorCode.PARAM_PRIMARY_KEY_IS_NULL);
        }
    }

    private static void toTableFromMap(JSONObject record, Table table) {

    }
}
