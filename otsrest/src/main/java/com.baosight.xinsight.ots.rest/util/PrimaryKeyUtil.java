package com.baosight.xinsight.ots.rest.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.utils.BytesUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class PrimaryKeyUtil {

    final static int MD5_BYTES_LENGTH_SHORT = 4;

    /**
     * 生成rowkey
     * @param tableId
     * @param primaryKey
     * @param record
     * @return
     */
    public static byte[] generateRowkey(long tableId, JSONArray primaryKey, JSONObject record) {

        JSONArray primaryKeyValue = new JSONArray();
        for (int i=0; i<primaryKey.size(); i++){
            if (!record.containsKey(primaryKey.get(i))){
                //报错
            }
            primaryKeyValue.add(record.get(primaryKey.get(i)));
        }

        //tableId的byte数组
        byte[] byteTableId = BytesUtil.toBytes(tableId);

        byte[] digest = md5(tableId);
        byte[] tableIdLenByte = BytesUtil.toBytes(byteTableId.length);


       return BytesUtil.toBytes(primaryKeyValue.toString());
       //todo lyh 拼接规则暂不定


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

}
