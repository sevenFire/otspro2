package com.baosight.xinsight.ots.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class ColumnsUtil {
    final static int BYTES_BYTES_MD5_LENGTH = 4;
    final static int BYTES_LENGTH_LONG_LENGTH = 4;

    public static final String EXIST_BYTE_ARRAY = "exist";
    public static final String CELL_VALUE_AND_LEN_ARRAY = "cell";
    public static final String CELL_LEN_TOTAL = "lenTotal";
    /**
     * 生成要插入HBase的cell中的值
     * 注意，cellValue里不存主键。
     * @param schema_tableColumns
     * @param record
     * @return
     */
    public static byte[] generateCellValue(JSONArray schema_primaryKey,
                                           JSONArray schema_tableColumns,
                                           JSONObject record) throws SQLException, OtsException {
        Map<String,Object> cellMap = new HashMap<>();

        //用来存储列值和列长度(string和blob需要)
        List<byte[]> columnsValueAndLenArray = new ArrayList<>();
        //用来表示每一列的值存在性
        byte[] exist = new byte[schema_tableColumns.size() - schema_primaryKey.size()];
        //用来存储total len，初始值为exist数组的长度
        Integer lenTotal = exist.length;

        for (int i = 0,j=0; i<schema_tableColumns.size() && j<exist.length; i++){

            JSONObject tableColumn = (JSONObject) schema_tableColumns.get(i);
            String colName = (String) tableColumn.get("col_name");
            String colType = (String) tableColumn.get("col_type");

            if(schema_primaryKey.contains(colName)){//跳过主键
                continue;
            }

            if (record.containsKey(colName)){
                exist[j] = 1;

                byte[] valueByte;
                byte[] lenByte;
                if (colType.equalsIgnoreCase("string") || colType.equalsIgnoreCase("blob")){
                    if (colType.equalsIgnoreCase("string")) {
                        String value = record.getString(colName);
                        valueByte = BytesUtil.toBytes(value);
                        lenByte = BytesUtil.toBytes(value.length());

                    }else{
                        Blob value = (Blob) record.get(colName);
                        valueByte = BytesUtil.toBytes(value);
                        lenByte = BytesUtil.toBytes(value.length());
                    }
                    columnsValueAndLenArray.add(valueByte);
                    columnsValueAndLenArray.add(lenByte);
                    lenTotal += valueByte.length + lenByte.length;

                }else {

                    valueByte = getValueByte(colType,colName,record);

                    columnsValueAndLenArray.add(valueByte);
                    lenTotal += valueByte.length;
                }
            }else{
                exist[j] = 0;
            }
            j++;
        }

        cellMap.put(EXIST_BYTE_ARRAY,exist);
        cellMap.put(CELL_VALUE_AND_LEN_ARRAY,columnsValueAndLenArray);
        cellMap.put(CELL_LEN_TOTAL,lenTotal);

        return generateCellValueByCellMap(cellMap);
    }

    private static byte[] generateCellValueByCellMap(Map<String, Object> cellMap) {
        int lenTotal = (int) cellMap.get(CELL_LEN_TOTAL);
        List<byte[]> columnsValueAndLenArray = (List<byte[]>) cellMap.get(CELL_VALUE_AND_LEN_ARRAY);
        byte[] exist = (byte[]) cellMap.get(EXIST_BYTE_ARRAY);

        byte[] cellValue = new byte[lenTotal];

        int targetOffset = 0;
        for (int i=0; i<columnsValueAndLenArray.size(); i++) {
            byte[] one = columnsValueAndLenArray.get(i);
            BytesUtil.putBytes(cellValue,targetOffset,one,0,one.length);
            targetOffset += one.length;
        }

        //标记为放在最后
        BytesUtil.putBytes(cellValue,targetOffset,exist,0,exist.length);
        return cellValue;
    }

    public static byte[] getValueByte(String colType, String colName, JSONObject record) throws OtsException {
        byte[] valueByte;

        if(colType.equalsIgnoreCase("int8")) {
            Byte value = (Byte) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("int16")) {
            Short value = (Short) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("int32")) {
            Integer value = (Integer) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("int64")) {
            Long value = (Long) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("float32")) {
            Double value = (Double) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("float64")) {
            Float value = (Float) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else if(colType.equalsIgnoreCase("bool")) {
            Boolean value = (Boolean) record.get(colName);
            valueByte = BytesUtil.toBytes(value);
        }else{
            //不支持的字段类型
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,"不支持的字段类型");
        }

        return valueByte;

    }

    /**
     * 将存储于HBase的打包cell值解析出真正的列值
     * @param returnColumns
     * @param cellValue
     * @return
     */
    public static JSONObject generateColumnValue(JSONArray schema_tableColumns,
                                                 List<String> returnColumns,
                                                 byte[] cellValue) {
        JSONObject record = new JSONObject();

        Map<String,String> tableColumnsMap = dealWithTableColumns(schema_tableColumns);


        //列总数
        int colSize = schema_tableColumns.size();
        int totalLen = cellValue.length;
        //copyOfRange方法中from是inclusive，to是exclusive。
        byte[] valueAndLength = Arrays.copyOfRange(cellValue, BYTES_BYTES_MD5_LENGTH + BYTES_LENGTH_LONG_LENGTH, totalLen-colSize);
        byte[] exist = Arrays.copyOfRange(cellValue, totalLen-colSize, totalLen);

        int targetOffset = 4;
        for (int i=exist.length-1; i>=0; i--){
            int existFlag = exist[i];
            if (existFlag == 1){

//                record.put()
            }
        }

        return null;
    }

    /**
     * 处理table_columns，抽取col_name和col_type的对应关系。
     * @param schema_tableColumns
     * @return
     */
    public static Map<String, String> dealWithTableColumns(JSONArray schema_tableColumns) {
        Map<String,String> tableColumnsMap = new HashMap<>();
        for (int i = 0; i < schema_tableColumns.size(); i++) {
            JSONObject column = (JSONObject) schema_tableColumns.get(i);
            //(age:int)
            tableColumnsMap.put(column.getString(ParamConstant.KEY_COL_NAME),column.getString(ParamConstant.KEY_COL_TYPE));
        }
        return tableColumnsMap;
    }

    public static void main(String[] args) throws OtsException {
        JSONArray schema_primaryKey = new JSONArray();
        schema_primaryKey.add("col1");
        schema_primaryKey.add("col2");
        schema_primaryKey.add("col3");

        JSONArray schema_tableColumns = new JSONArray();
        JSONObject one = new JSONObject();
        one.put("col_name","col1");
        one.put("col_type","int32");
        schema_tableColumns.add(one);
        JSONObject two = new JSONObject();
        two.put("col_name","col2");
        two.put("col_type","int32");
        schema_tableColumns.add(two);
        JSONObject three = new JSONObject();
        three.put("col_name","col3");
        three.put("col_type","string");
        schema_tableColumns.add(three);
        JSONObject four = new JSONObject();
        four.put("col_name","col4");
        four.put("col_type","int32");
        schema_tableColumns.add(four);

        JSONObject record = new JSONObject();
        record.put("col1",1);
        record.put("col2",2);
        record.put("col3","value0");
        record.put("col4",4);

        byte[] cellValue = new byte[0];
        try {
            cellValue = generateCellValue(schema_primaryKey,schema_tableColumns, record);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < cellValue.length; i++) {
            System.out.print(cellValue[i]+" ");
        }

    }


}
