package com.baosight.xinsight.ots.common.util;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baosight.xinsight.ots.constants.ByteConstant;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class ColumnsUtil {
    public static final String EXIST_BYTE_ARRAY = "exist";
    public static final String CELL_VALUE_AND_LEN_ARRAY = "cell";
    public static final String CELL_LEN_TOTAL = "lenTotal";

    private static final String NORMAL_COLUMNS = "normal_columns";
    private static final String PRIMARY_COLUMNS = "primary_columns";
    private static final String COL_NAME = "col_name";
    private static final String COL_TYPE = "col_type";

    //=========================正向===========================================
    /**
     * 生成要插入HBase的cell中的值
     * 注意，cellValue里不存主键。
     * @param schema_primaryKey
     * @param schema_tableColumns
     * @param record
     * @return
     * @throws DecoderException 
     */
    public static byte[] generateCellValue(ArrayNode schema_primaryKey,
                                           ArrayNode schema_tableColumns,
                                           JsonNode record) throws SQLException, OtsException, DecoderException {
        Map<String,Object> cellMap = new HashMap<>();

        //ArrayNode中没有contains方法，所以转换了一下类型。
        List<String> schema_primaryKey_list = changeArrayNodeToList(schema_primaryKey);

        //用来存储列值和列长度(string和blob需要)
        List<byte[]> columnsValueAndLenArray = new ArrayList<>();
        //用来表示每一列的值存在性
        byte[] exist = new byte[schema_tableColumns.size() - schema_primaryKey.size()];
        //用来存储total len
        Integer lenTotal = exist.length;

        for (int i = 0, j=0; i<schema_tableColumns.size() && j<exist.length; i++){

            JsonNode tableColumn = schema_tableColumns.get(i);
            String colName = tableColumn.get("col_name").asText();
            String colType = tableColumn.get("col_type").asText();

            if(schema_primaryKey_list.contains(colName)){//跳过主键
                continue;
            }

            if (record.has(colName)){
                exist[j] = 1;

                byte[] valueByte;
                byte[] lenByte;
                if (colType.equalsIgnoreCase("string") || colType.equalsIgnoreCase("blob")){
                    if (colType.equalsIgnoreCase("string")) {
                        String value = record.get(colName).asText();
                        valueByte = BytesUtil.toBytes(value);
                        lenByte = BytesUtil.toBytes(value.length());

                    }else{
                    	String value = record.get(colName).asText();
                    	valueByte = Hex.decodeHex(value.toCharArray());
                        lenByte = BytesUtil.toBytes(valueByte.length);
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

    /**
     * 转换类型
     * @param schema_primaryKey
     * @return
     */
    private static List<String> changeArrayNodeToList(ArrayNode schema_primaryKey) {
        List<String> schema_primaryKey_list = new ArrayList<>();
        for (int i = 0; i < schema_primaryKey.size(); i++) {
            schema_primaryKey_list.add(schema_primaryKey.get(i).asText());
        }
        return schema_primaryKey_list;
    }

    /**
     * 生成真正存储于HBase的cellValue
     * @param cellMap
     * @return
     */
    private static byte[] generateCellValueByCellMap(Map<String, Object> cellMap) {
        int lenTotal = (int) cellMap.get(CELL_LEN_TOTAL);
        @SuppressWarnings("unchecked")
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

    /**
     * 根据数据类型将colValue转换为byte[]
     * @param colType
     * @param colName
     * @param record
     * @return
     * @throws OtsException
     */
    public static byte[] getValueByte(String colType, String colName, JsonNode record) throws OtsException {
        byte[] valueByte;
        
        String value = record.get(colName).asText(); 
        if(colType.equalsIgnoreCase("int8")) {
            valueByte = new byte[1];
        	valueByte[0] = (byte)Short.parseShort(value);			
        }else if(colType.equalsIgnoreCase("int16")) {
            valueByte = BytesUtil.toBytes(Short.parseShort(value));
        }else if(colType.equalsIgnoreCase("int32")) {
            valueByte = BytesUtil.toBytes(Integer.parseInt(value));
        }else if(colType.equalsIgnoreCase("int64")) {
            valueByte = BytesUtil.toBytes(Long.parseLong(value));
        }else if(colType.equalsIgnoreCase("float32")) {
            valueByte = BytesUtil.toBytes(Float.parseFloat(value));
        }else if(colType.equalsIgnoreCase("float64")) {
            valueByte = BytesUtil.toBytes(Double.parseDouble(value));
        }else if(colType.equalsIgnoreCase("bool")) {
            valueByte = BytesUtil.toBytes(Boolean.parseBoolean(value));
        }else{
            //不支持的字段类型
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,"不支持的字段类型");
        }

        return valueByte;

    }

    /**
     * 根据数据的类型，获取其所占的byte[]长度
     * @param colType
     * @return
     */
    /**
     * 根据数据的类型，获取其所占的byte[]长度
     * @param colType
     * @return
     */
    private static int getValueByteLen(String colType) throws OtsException {
        if (colType.equalsIgnoreCase("int8")){
            return ByteConstant.BYTES_LENGTH_INT8_LENGTH;
        }else if (colType.equalsIgnoreCase("int16")){
            return ByteConstant.BYTES_LENGTH_INT16_LENGTH;
        }else if (colType.equalsIgnoreCase("int32")){
            return ByteConstant.BYTES_LENGTH_INT32_LENGTH;
        }else if (colType.equalsIgnoreCase("int64")){
            return ByteConstant.BYTES_LENGTH_INT64_LENGTH;
        }else if (colType.equalsIgnoreCase("bool")){
            return ByteConstant.BYTES_LENGTH_BOOL_LENGTH;
        }else if (colType.equalsIgnoreCase("float32")){
            return ByteConstant.BYTES_LENGTH_FLOAT32_LENGTH;
        }else if (colType.equalsIgnoreCase("float64")){
            return ByteConstant.BYTES_LENGTH_FLOAT64_LENGTH;
        }else{
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,"不支持的字段类型");
        }

    }


    /**
     * 处理table_columns，抽取col_name和col_type的对应关系。
     * @param schema_tableColumns
     * @return
     */
    public static Map<String, String> dealWithTableColumns(ArrayNode schema_tableColumns) {
        Map<String,String> tableColumnsMap = new HashMap<>();
        for (int i = 0; i < schema_tableColumns.size(); i++) {
            JsonNode column = schema_tableColumns.get(i);
            //(age:int)
            tableColumnsMap.put(column.get(ParamConstant.KEY_COL_NAME).asText(),
                    column.get(ParamConstant.KEY_COL_TYPE).asText());
        }
        return tableColumnsMap;
    }


    //==========================反向=================================================
    /**
     * 将存储于HBase的打包cell值解析出真正的列值
     * 注意，打包cell中不存储主键值，所以主键值需要从rowKey中解析出来
     * @param schema_primaryKey
     * @param schema_tableColumns
     * @param returnColumns
     * @param cellValue 这里的cellValue是数据表的cell，构成为普通列的value1+(len1)+...+标记位
     * @param rowKey 这里的行键是数据表的行键，构成为md5+tableId+value1+(len1)+...
     * @return
     */
    public static Map<String,Object> generateColumnValue(ArrayNode schema_primaryKey,
                                               ArrayNode schema_tableColumns,
                                               List<String> returnColumns,
                                               byte[] cellValue,
                                               byte[] rowKey) throws OtsException {

        //处理列信息
        Map<String,List<Map<String,String>>> tableColumnValueMap = dealWithTableColumns(schema_primaryKey,schema_tableColumns);
        List<Map<String,String>> primaryKeyNameAndType = tableColumnValueMap.get(PRIMARY_COLUMNS);
        List<Map<String,String>> normalColumnsNameAndType = tableColumnValueMap.get(NORMAL_COLUMNS);

        //解析列信息
        Map<String,Object> primaryColumns = getPrimaryColumnsColValue(rowKey,primaryKeyNameAndType);
        Map<String,Object> normalColumns = getNormalColumnsColValue(cellValue,normalColumnsNameAndType);

        //处理returnColumns
        Map<String,Object> record = dealWithReturnColumns(primaryColumns,normalColumns,returnColumns);

        return record;

    }

    /**
     * 处理返回的列
     * @param primaryColumnsValue
     * @param normalColumnsValue
     * @param returnColumns
     * @return
     */
    private static Map<String,Object> dealWithReturnColumns(Map<String,Object> primaryColumnsValue,
                                                    Map<String,Object> normalColumnsValue,
                                                    List<String> returnColumns) {
        Map<String,Object> record = new HashMap<>();
        for (int i = 0; i < returnColumns.size(); i++) {
            String returnColName = returnColumns.get(i);
            if(normalColumnsValue.containsKey(returnColName)){
                record.put(returnColName,normalColumnsValue.get(returnColName));
            }else if(primaryColumnsValue.containsKey(returnColName)){
                record.put(returnColName,primaryColumnsValue.get(returnColName));
            }else{
                continue;
            }
        }
        return record;
    }

    /**
     * 从cellValue中解析出primary key的值。
     * 注意exist判定。
     * 注意getNormalColumnsColValue()方法和getPrimaryColumnsColValue()很相似，但这里不写到一起，因为
     * 后续也许会改变cellValue和rowKey的组成方式。二者不一定是保持一致的。
     * @param cellValue
     * @param normalColumnsColValue 普通列的列名和列类型对
     */
    private static Map<String,Object> getNormalColumnsColValue(byte[] cellValue, List<Map<String,String>> normalColumnsColValue) throws OtsException {
        //总长度
        int totalLen = cellValue.length;
        //标记位的长度
        int existSize = normalColumnsColValue.size();
        //标记位数组
        byte[] exist = Arrays.copyOfRange(cellValue,totalLen-existSize,totalLen);

        Map<String,Object> record = new HashMap<>();

        int srcOffset = cellValue.length - existSize;
        int tarOffset = cellValue.length - existSize;
        for (int i = normalColumnsColValue.size()-1; i >= 0 && srcOffset>=0 && tarOffset>=0; i--) {
            if (exist[i] == 0){
                continue;
            }
            Map<String, String> one = normalColumnsColValue.get(i);
            String colName = one.get(COL_NAME);
            String colType = one.get(COL_TYPE);


            if (colType.equalsIgnoreCase("string") || colType.equalsIgnoreCase("blob")){
                //取长度的字节数组
                srcOffset -= ByteConstant.BYTES_LENGTH_INT_LENGTH;
                byte[] lenByte = Arrays.copyOfRange(cellValue,srcOffset,tarOffset);
                int len = BytesUtil.byteArrayToInt(lenByte);

                //取value的字节数组
                tarOffset = srcOffset;
                srcOffset -= len;
                byte[] valueByte =  Arrays.copyOfRange(cellValue,srcOffset,tarOffset);

                if (colType.equalsIgnoreCase("string")) {
                    String colValue = BytesUtil.byteArrayToString(valueByte);
                    record.put(colName,colValue);
                }else{
                    Blob colValue = BytesUtil.byteArrayToBlob(valueByte);
                    record.put(colName,colValue);
                }
            }else{
                //取value的字节数组
                int valueByteLen = getValueByteLen(colType);
                srcOffset -= valueByteLen;
                byte[] valueByte = Arrays.copyOfRange(cellValue,srcOffset,tarOffset);
                Object colValue = getColValueByType(colType,valueByte);
                record.put(colName,colValue);
            }
            tarOffset = srcOffset;
        }
        return record;
    }


    /**
     * 从rowKey中解析出primary key的值。
     * @param rowKey
     * @param primaryKeyNameAndType 主键列的列名和列类型对
     */
    private static Map<String,Object> getPrimaryColumnsColValue(byte[] rowKey,
                                                        List<Map<String,String>> primaryKeyNameAndType) throws OtsException {
        Map<String,Object> record = new HashMap<>();

        int srcOffset = rowKey.length;
        int tarOffset = rowKey.length;

        int prefixLen = ByteConstant.BYTES_BYTES_MD5_LENGTH + ByteConstant.BYTES_LENGTH_LONG_LENGTH;
        for (int i = primaryKeyNameAndType.size()-1; i >= 0 && srcOffset>=prefixLen && tarOffset>=prefixLen; i--) {
            Map<String,String> one = primaryKeyNameAndType.get(i);
            String colName = one.get(COL_NAME);
            String colType = one.get(COL_TYPE);

            if (colType.equalsIgnoreCase("string") || colType.equalsIgnoreCase("blob")){
                //取长度的字节数组
                srcOffset -= ByteConstant.BYTES_LENGTH_INT_LENGTH;
                byte[] lenByte = Arrays.copyOfRange(rowKey,srcOffset,tarOffset);
                int len = BytesUtil.byteArrayToInt(lenByte);

                //取value的字节数组
                tarOffset = srcOffset;
                srcOffset -= len;
                byte[] valueByte =  Arrays.copyOfRange(rowKey,srcOffset,tarOffset);

                if (colType.equalsIgnoreCase("string")) {
                    String colValue = BytesUtil.byteArrayToString(valueByte);
                    record.put(colName,colValue);
                }else{
                    Blob colValue = BytesUtil.byteArrayToBlob(valueByte);
                    record.put(colName,colValue);
                }
            }else{
                //取value的字节数组
                int valueByteLen = getValueByteLen(colType);
                srcOffset -= valueByteLen;
                byte[] valueByte = Arrays.copyOfRange(rowKey,srcOffset,tarOffset);
                Object colValue = getColValueByType(colType,valueByte);
                record.put(colName,colValue);
            }
            tarOffset = srcOffset;
        }
        return record;
    }



    /**
     * 处理table_columns，抽取col_name和col_type的对应关系。
     * 并将其primaryKey和普通列区分开。
     * 注意，列要保持顺序
     * @param schema_primaryKey
     * @param schema_tableColumns
     * @return 主键和普通列分别的列名和列类型对应关系
     */
    private static Map<String, List<Map<String,String>>> dealWithTableColumns(ArrayNode schema_primaryKey,
                                                               ArrayNode schema_tableColumns) {

        List<String> schema_primaryKey_list = changeArrayNodeToList(schema_primaryKey);

        Map<String,List<Map<String,String>>> tableColumnsMap = new HashMap<>();
        List<Map<String,String>> normalColumns = new ArrayList<>();
        List<Map<String,String>> primaryColumns = new ArrayList<>();
        tableColumnsMap.put(NORMAL_COLUMNS,normalColumns);
        tableColumnsMap.put(PRIMARY_COLUMNS,primaryColumns);

        for (int i = 0; i < schema_tableColumns.size(); i++) {
            ObjectNode column = (ObjectNode) schema_tableColumns.get(i);
            String colName = column.get(ParamConstant.KEY_COL_NAME).asText();
            String colType = column.get(ParamConstant.KEY_COL_TYPE).asText();

            Map<String,String> one = new HashMap<>();
            one.put(COL_NAME,colName);
            one.put(COL_TYPE,colType);
            //比如(age:int)
            if (schema_primaryKey_list.contains(colName)){
                primaryColumns.add(one);
            }else{
                normalColumns.add(one);
            }

        }
        return tableColumnsMap;
    }


    /**
     * 根据数据类型将byte[]解析出原本的colValue
     * @param colType
     * @param valueByte
     * @return
     */
    private static Object getColValueByType(String colType, byte[] valueByte) throws OtsException {
        if(colType.equalsIgnoreCase("int8")) {
            return BytesUtil.byteArrayToByte(valueByte);
        }else if(colType.equalsIgnoreCase("int16")) {
            return BytesUtil.byteArrayToShort(valueByte);
        }else if(colType.equalsIgnoreCase("int32")) {
            return BytesUtil.byteArrayToInteger(valueByte);
        }else if(colType.equalsIgnoreCase("int64")) {
            return BytesUtil.byteArrayToLong(valueByte);
        }else if(colType.equalsIgnoreCase("float32")) {
            return BytesUtil.byteArrayToDouble(valueByte);
        }else if(colType.equalsIgnoreCase("float64")) {
            return BytesUtil.byteArrayToFloat(valueByte);
        }else if(colType.equalsIgnoreCase("bool")) {
            return BytesUtil.byteArrayToBoolean(valueByte);
        }else{
            //不支持的字段类型
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,"不支持的字段类型");
        }

    }




    //==========================测试=================================================




    public static void main(String[] args) throws OtsException, DecoderException {
    	
        ArrayNode schema_primaryKey = JsonNodeFactory.instance.arrayNode();
        schema_primaryKey.add("col1");
        schema_primaryKey.add("col2");
        schema_primaryKey.add("col3");

//        List<String> schema_primaryKey_list = changeArrayNodeToList(schema_primaryKey);

        ArrayNode schema_tableColumns = JsonNodeFactory.instance.arrayNode();
        
        ObjectNode one = schema_tableColumns.addObject();
        one.put("col_name","col1");
        one.put("col_type","int32");
        
        ObjectNode two = schema_tableColumns.addObject();
        two.put("col_name","col2");
        two.put("col_type","int32");

        ObjectNode three = schema_tableColumns.addObject();
        three.put("col_name","col3");
        three.put("col_type","string");

        ObjectNode record = JsonNodeFactory.instance.objectNode();
        record.put("col1",1);
        record.put("col2",2);
        record.put("col3","value0");

        //解析cellValue
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
