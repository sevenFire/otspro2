package com.baosight.xinsight.ots.rest.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ParamConstant;
import com.baosight.xinsight.ots.rest.constant.ParamErrorCode;
import com.baosight.xinsight.utils.BytesUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author liyuhui
 * @date 2018/12/18
 * @description
 * rowKey = part1 + part2 + part3
 * part1：tableId加密后取前四位
 * part2: tableId
 * part3: value1 + len1 + value2 + len2 + value3 + len3(3个主键的值，按顺序)
 *      不是字符串类型使用默认长度，比如int为4个字节，long为8个字节等。
 *      blob和string类型的，获取其长度。
 *      len就用int表示。
 */
public class PrimaryKeyUtil {
    //截取的加密长度
    final static int BYTES_BYTES_MD5_LENGTH = 4;
    //用来存长度信息的长度
    final static int BYTES_LENGTH_LENGTH = 4;
    //int
    final static int BYTES_LENGTH_INT_LENGTH = 4;
    final static int BYTES_LENGTH_LONG_LENGTH = 8;

    /**
     * 生成rowKey
     * @param tableId 表id
     * @param schema_primaryKey 被选择为主键的所有列
     * @param schema_tableColumns  列的类型和名字
     * @param record
     * @return
     */
    public static byte[] generateRowKey(long tableId,
                                        JSONArray schema_primaryKey,
                                        JSONArray schema_tableColumns,
                                        JSONObject record,
                                        Boolean prefix) throws OtsException {
        
        Map<String,String> tableColumnsMap = dealWithTableColumns(schema_tableColumns);

        //存有primaryKey的每列值的数组
        List<byte[]> primaryKeyValueAndLength = new ArrayList<>();
        int lenTotal = 0;
        for (int i=0; i<schema_primaryKey.size(); i++){
            if (!record.containsKey(schema_primaryKey.get(i))){
                if (!prefix){//要计算全部primaryKey的
                    throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
                }else{//只需要计算到这里
                    break;
                }
            }
            //get col_name
            String primaryKey_colName = (String) schema_primaryKey.get(i);
            //get col_type
            String type = tableColumnsMap.get(primaryKey_colName);

            byte[] valueByte = null;
            byte[] lenByte = null;
            //get value in byte[]
            if (type.equalsIgnoreCase("string")){
                String value = (String) record.get(primaryKey_colName);
                valueByte = BytesUtil.toBytes(value);
                lenByte = BytesUtil.toBytes(value.length());
            }else{
                //todo 其他类型
            }

            primaryKeyValueAndLength.add(valueByte);
            primaryKeyValueAndLength.add(lenByte);
            lenTotal += valueByte.length + lenByte.length;
        }

        if (prefix){//前缀查询的时候，要删除后面的Len
            byte[] lastLen = primaryKeyValueAndLength.get(primaryKeyValueAndLength.size()-1);
            primaryKeyValueAndLength.remove(lastLen);
            lenTotal -= lastLen.length;
        }
        return generateRowKey(tableId,lenTotal,primaryKeyValueAndLength);
    }

    /**
     * 根据列值和列长度组成rowkey
     * @param tableId
     * @param lenTotal
     * @param primaryKeyValueAndLength
     * @return
     */
    private static byte[] generateRowKey(long tableId,
                                         int lenTotal,
                                         List<byte[]> primaryKeyValueAndLength) {

        //tableId加密后的byte数组
        byte[] digest = md5(tableId);
        //tableId的byte数组
        byte[] byteTableId = BytesUtil.toBytes(tableId);

        byte[] primaryKey = new byte[lenTotal + BYTES_LENGTH_LONG_LENGTH + BYTES_BYTES_MD5_LENGTH];
        BytesUtil.putBytes(primaryKey, 0, digest, 0, BYTES_BYTES_MD5_LENGTH);
        BytesUtil.putBytes(primaryKey, BYTES_BYTES_MD5_LENGTH, byteTableId, 0, BYTES_LENGTH_LONG_LENGTH);

        int targetOffset = BYTES_BYTES_MD5_LENGTH + BYTES_LENGTH_LONG_LENGTH;
        for (int i=0; i<primaryKeyValueAndLength.size(); i++) {
            byte[] one = primaryKeyValueAndLength.get(i);
            BytesUtil.putBytes(primaryKey,targetOffset,one,0,one.length);
            targetOffset += one.length;
        }

        return primaryKey;
    }



    /**
     * 处理table_columns，抽取col_name和col_type的对应关系。
     * @param schema_tableColumns
     * @return
     */
    private static Map<String, String> dealWithTableColumns(JSONArray schema_tableColumns) {
        Map<String,String> tableColumnsMap = new HashMap<>();
        for (int i = 0; i < schema_tableColumns.size(); i++) {
            JSONObject column = (JSONObject) schema_tableColumns.get(i);
            //(age:int)
            tableColumnsMap.put(column.getString(ParamConstant.KEY_COL_NAME),column.getString(ParamConstant.KEY_COL_TYPE));
        }
        return tableColumnsMap;
    }

    /**
     * 得到rowkey的取值范围
     * @param tableId
     * @param schema_primaryKey
     * @param schema_tableColumns
     * @param primaryKeyInput 实际输入的主键值
     *
     *
     */
    public static List<byte[]> generateRowKeyRange(long tableId,
                                                JSONArray schema_primaryKey,
                                                JSONArray schema_tableColumns,
                                                JSONArray primaryKeyInput) throws OtsException {
        if (primaryKeyInput==null || primaryKeyInput.size()==0){
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
        }
        List<byte[]> rowKeys = new ArrayList<>();

        JSONObject recordStart = new JSONObject();
        JSONObject recordEnd = new JSONObject();

        for (int i = primaryKeyInput.size()-1 ; i >= 0; i--) {
            JSONObject colOne = (JSONObject) primaryKeyInput.get(i);

            //先获取最后一位，只有这一位有两种可能
            if (i == primaryKeyInput.size() - 1) {
                if (colOne.containsKey(ParamConstant.KEY_COL_VALUE)) {
                    Object colValue = colOne.get(ParamConstant.KEY_COL_VALUE);
                    String colName = colOne.getString(ParamConstant.KEY_COL_NAME);

                    recordStart.put(colName, colValue);

                } else if (colOne.containsKey(ParamConstant.KEY_COL_START)
                        && colOne.containsKey(ParamConstant.KEY_COL_END)) {
                    Object colStart = colOne.get(ParamConstant.KEY_COL_START);
                    Object colEnd = colOne.get(ParamConstant.KEY_COL_END);
                    String colName = colOne.getString(ParamConstant.KEY_COL_NAME);

                    recordStart.put(colName, colStart);
                    recordEnd.put(colName, colEnd);

                } else {
                    throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
                }
            } else {
                if (!colOne.containsKey(ParamConstant.KEY_COL_VALUE)) {
                    throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
                }
                Object colValue = colOne.get(ParamConstant.KEY_COL_VALUE);
                String colName = colOne.getString(ParamConstant.KEY_COL_NAME);
                recordStart.put(colName, colValue);
                recordEnd.put(colName, colValue);
            }
        }

        rowKeys.add(generateRowKey(tableId, schema_primaryKey, schema_tableColumns, recordStart, true));
        if (recordEnd.size() == recordStart.size()) {
            rowKeys.add(generateRowKey(tableId, schema_primaryKey, schema_tableColumns, recordEnd, true));
        }
        return rowKeys;
    }


    /**
     * 生成rowkey
     * @param tableId
     * @param primaryKeyValue 按顺序存好的主键值
     * @param colRange 有范围的主键值
     *
     */
    public static byte[] generateRowKeyPrefix(long tableId, JSONArray primaryKeyValue, Object colRange) {
        //tableId加密后的前四位
        byte[] digest = Arrays.copyOfRange(md5(tableId),0,3);

        //tableId的byte数组
        byte[] byteTableId = BytesUtil.toBytes(tableId);

        JSONArray byteLenArray = new JSONArray();
        Integer primaryKeyByteLenTotal = 0;
        for (int i = 0; i < primaryKeyValue.size()-1; i++) {

            Object colOne = primaryKeyValue.get(i);
//            getLen(byteLenArray,colOne);
//            primaryKeyByteLenTotal+=byteLenArray.get(i);

        }

//        byte[] primaryKey = new byte[MD5_BYTES_LENGTH_SHORT + byteTableId.length + hashKey.length];
        return null;

    }

//    /**
//     * 获取字节长度
//     * @param byteLenArray
//     * @param col
//     */
//    private static int getLen(Object col) {
//        if (col instanceof String){
//            return BytesUtil.toBytes(((String) col).length());
//        }else if(col instanceof Integer){
//            return BytesUtil.toBytes(4);
//        }else if(col instanceof Long){
//            return BytesUtil.toBytes(8);
//        }else{
//            //todo lyh other situation
//            return null;
//    }

    /**
     * 加密
     * @param tableId
     * @return
     */
    private static byte[] md5(long tableId) {
        byte[] tableIdByte = BytesUtil.toBytes(tableId);

        MessageDigest md5Digest;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        md5Digest.update(tableIdByte);
        return md5Digest.digest();
    }



    public static void main(String[] args) throws OtsException {
        long tableId = 1;
        JSONArray schema_primaryKey = new JSONArray();
        schema_primaryKey.add("col1");
        schema_primaryKey.add("col2");
        schema_primaryKey.add("col3");

        JSONArray schema_tableColumns = new JSONArray();
        JSONObject one = new JSONObject();
        one.put("col_name","col1");
        one.put("col_type","string");
        schema_tableColumns.add(one);
        JSONObject two = new JSONObject();
        two.put("col_name","col2");
        two.put("col_type","string");
        schema_tableColumns.add(two);
        JSONObject three = new JSONObject();
        three.put("col_name","col3");
        three.put("col_type","string");
        schema_tableColumns.add(three);

        JSONObject record = new JSONObject();
        record.put("col1","value0");
        record.put("col2","value0");
        record.put("col3","value5");
        record.put("col4","value0");
        record.put("col5","value0");

        byte[] rowKey = generateRowKey(tableId, schema_primaryKey, schema_tableColumns, record,false);
        for (int i = 0; i < rowKey.length; i++) {
            System.out.println(rowKey[i]);
        }

//        int a = 0;
//        long b = 0l;
//        float c = 0;
//
//        System.out.println(BytesUtil.toBytes(a).length);
//        System.out.println(BytesUtil.toBytes(b).length);
//        System.out.println(BytesUtil.toBytes(c).length);

    }


}
