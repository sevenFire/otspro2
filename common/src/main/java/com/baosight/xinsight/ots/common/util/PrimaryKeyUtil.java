package com.baosight.xinsight.ots.common.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.ots.constants.ByteConstant;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * 批量生成rowKey
     * @param tableId
     * @param schema_primaryKey
     * @param schema_tableColumns
     * @param primaryKeyInputList
     * @return
     */
    public static List<byte[]> generateRowKeyBatch(Long tableId,
                                                   JSONArray schema_primaryKey,
                                                   JSONArray schema_tableColumns,
                                                   List<JSONObject> primaryKeyInputList) throws OtsException, SQLException {
        List<byte[]> rowKeys = new ArrayList<>();
        for (int i = 0; i < primaryKeyInputList.size(); i++) {
            JSONObject record = primaryKeyInputList.get(i);
            byte[] rowKey = generateRowKey(tableId,schema_primaryKey,schema_tableColumns,record,false);
            rowKeys.add(rowKey);
        }

        return rowKeys;
    }

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
                                        Boolean prefix) throws OtsException, SQLException {
        
        Map<String,String> tableColumnsMap = ColumnsUtil.dealWithTableColumns(schema_tableColumns);

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
            if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("blob")){

                if (type.equalsIgnoreCase("string")) {
                    String value = (String) record.get(primaryKey_colName);
                    valueByte = BytesUtil.toBytes(value);
                    lenByte = BytesUtil.toBytes(value.length());
                }else{
                    Blob value = (Blob) record.get(primaryKey_colName);
                    valueByte = BytesUtil.toBytes(value);
                    lenByte = BytesUtil.toBytes(value.length());
                }

                primaryKeyValueAndLength.add(valueByte);
                primaryKeyValueAndLength.add(lenByte);
                lenTotal += valueByte.length + lenByte.length;

            }else{
                valueByte = ColumnsUtil.getValueByte(type,primaryKey_colName,record);

                primaryKeyValueAndLength.add(valueByte);
                lenTotal += valueByte.length;

            }

        }

        if (prefix){//前缀查询的时候，要删除后面的Len
            byte[] lastLen = primaryKeyValueAndLength.get(primaryKeyValueAndLength.size()-1);
            primaryKeyValueAndLength.remove(lastLen);
            lenTotal -= lastLen.length;
        }
        return generateRowKey(tableId,lenTotal,primaryKeyValueAndLength);
    }

    /**
     * 只有tableId的rowkey前缀
     * @param tableId
     * @return
     */
    public static byte[] generateRowKeyPrefixOnlyWithTableId(Long tableId) {
        //tableId加密后的byte数组
        byte[] digest = md5(tableId);
        //tableId的byte数组
        byte[] byteTableId = BytesUtil.toBytes(tableId);

        byte[] primaryKey = new byte[ByteConstant.BYTES_LENGTH_LONG_LENGTH + ByteConstant.BYTES_BYTES_MD5_LENGTH];
        BytesUtil.putBytes(primaryKey, 0, digest, 0, ByteConstant.BYTES_BYTES_MD5_LENGTH);
        BytesUtil.putBytes(primaryKey, ByteConstant.BYTES_BYTES_MD5_LENGTH, byteTableId, 0, ByteConstant.BYTES_LENGTH_LONG_LENGTH);

        return primaryKey;
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

        byte[] primaryKey = new byte[lenTotal + ByteConstant.BYTES_LENGTH_LONG_LENGTH + ByteConstant.BYTES_BYTES_MD5_LENGTH];
        BytesUtil.putBytes(primaryKey, 0, digest, 0, ByteConstant.BYTES_BYTES_MD5_LENGTH);
        BytesUtil.putBytes(primaryKey, ByteConstant.BYTES_BYTES_MD5_LENGTH, byteTableId, 0, ByteConstant.BYTES_LENGTH_LONG_LENGTH);

        int targetOffset = ByteConstant.BYTES_BYTES_MD5_LENGTH + ByteConstant.BYTES_LENGTH_LONG_LENGTH;
        for (int i=0; i<primaryKeyValueAndLength.size(); i++) {
            byte[] one = primaryKeyValueAndLength.get(i);
            BytesUtil.putBytes(primaryKey,targetOffset,one,0,one.length);
            targetOffset += one.length;
        }

        return primaryKey;
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
                                                Map<String,JSONObject> primaryKeyInput) throws OtsException, SQLException {
        List<byte[]> rowKeys = new ArrayList<>();

        if (primaryKeyInput==null || primaryKeyInput.size()==0 || primaryKeyInput.size() > 2){
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
        }

        JSONObject recordStart = primaryKeyInput.containsKey(ParamConstant.RECORD_START) ? primaryKeyInput.get(ParamConstant.RECORD_START): null;
        JSONObject recordEnd = primaryKeyInput.containsKey(ParamConstant.RECORD_END) ? primaryKeyInput.get(ParamConstant.RECORD_END): null;

        if (recordEnd == null){
            throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID);
        }
        rowKeys.add(generateRowKey(tableId, schema_primaryKey, schema_tableColumns, recordStart, true));
        if (recordEnd != null) {
            rowKeys.add(generateRowKey(tableId, schema_primaryKey, schema_tableColumns, recordEnd, true));
        }

        return rowKeys;
    }

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

        JSONObject record = new JSONObject();
        record.put("col1",1);
        record.put("col2",2);
        record.put("col3","value0");
        record.put("col4","value0");
        record.put("col5","value0");

        byte[] rowKey = new byte[0];
        try {
            rowKey = generateRowKey(tableId, schema_primaryKey, schema_tableColumns, record,false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < rowKey.length; i++) {
            System.out.print(rowKey[i]+" ");
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
