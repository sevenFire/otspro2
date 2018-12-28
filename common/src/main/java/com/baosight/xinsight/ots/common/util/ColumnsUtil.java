package com.baosight.xinsight.ots.common.util;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.BytesUtil;

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
    final static int BYTES_BYTES_MD5_LENGTH = 4;
    final static int BYTES_LENGTH_LONG_LENGTH = 4;

    public static final String EXIST_BYTE_ARRAY = "exist";
    public static final String CELL_VALUE_AND_LEN_ARRAY = "cell";
    public static final String CELL_LEN_TOTAL = "lenTotal";
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
            schema_primaryKey_list.add(String.valueOf(schema_primaryKey.get(i)));
        }
        return schema_primaryKey_list;
    }

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
     * 将存储于HBase的打包cell值解析出真正的列值
     * @param returnColumns
     * @param cellValue
     * @return
     */
    public static JsonNode generateColumnValue(ArrayNode schema_tableColumns,
                                                 List<String> returnColumns,
                                                 byte[] cellValue) {
    	//JsonNode record = new JsonNode();
        Map<String, String> tableColumnsMap = dealWithTableColumns(schema_tableColumns);

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

    public static void main(String[] args) throws OtsException, DecoderException {
    	
        ArrayNode schema_primaryKey = JsonNodeFactory.instance.arrayNode();
        schema_primaryKey.add("col1");
        schema_primaryKey.add("col2");
        schema_primaryKey.add("col3");

        List<String> schema_primaryKey_list = changeArrayNodeToList(schema_primaryKey);

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
