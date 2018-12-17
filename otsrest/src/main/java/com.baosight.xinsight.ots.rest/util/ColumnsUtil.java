package com.baosight.xinsight.ots.rest.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.ots.rest.constant.ParamConstant;

import scala.collection.parallel.mutable.ParArray;

/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class ColumnsUtil {

    /**
     * 校验列
     * @param tableColumns
     * @param record
     * @return
     */
    public static String generateCellValue(JSONArray tableColumns, JSONObject record) {
        JSONArray tableColumnsValueArray = new JSONArray();

        for (int i = 0; i<tableColumns.size(); i++){

            JSONObject tableColumn = (JSONObject) tableColumns.get(i);
            String colName = (String) tableColumn.get(ParamConstant.KEY_COL_NAME);
            String colType = (String) tableColumn.get(ParamConstant.KEY_COL_TYPE);

            if (record.containsKey(colName)){
                //todo lyh 校验类型 col_type
                tableColumnsValueArray.add(record.get(colName));
            }
        }
        return tableColumnsValueArray.toJSONString();
    }
}
