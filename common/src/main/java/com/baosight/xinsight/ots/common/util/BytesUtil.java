package com.baosight.xinsight.ots.common.util;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;


/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class BytesUtil extends com.baosight.xinsight.utils.BytesUtil{

    /**
     * blob转为byte[]
     * @param blob
     * @return
     */
    public static byte[] toBytes(Blob blob) {
        byte[] b = null;
        try {
            Long length = blob.length();
            b = blob.getBytes(1, length.intValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return b;
    }

    /**
     * byte[]转int
     * @param bytes
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    /**
     * byte数组转为string
     * @param bytes
     * @return
     */
    public static String byteArrayToString(byte[] bytes){
        String value = null;
        try {
            value = new String(bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * byte数组转为blob
     * @param valueByte
     * @return
     */
    public static Blob byteArrayToBlob(byte[] valueByte) {
       //todo lyh
        return null;

    }

    public static Byte byteArrayToByte(byte[] valueByte) {
        Byte value = Byte.valueOf(valueByte[valueByte.length-1]);
        return value;
    }

    public static Short byteArrayToShort(byte[] valueByte) {
        Short value = Short.valueOf(valueByte[valueByte.length-1]);
        return value;
    }

    public static Integer byteArrayToInteger(byte[] valueByte) {
        Integer value = Integer.valueOf(valueByte[valueByte.length-1]);
        return value;
    }

    public static Long byteArrayToLong(byte[] valueByte) {
        Long value = Long.valueOf(valueByte[valueByte.length-1]);
        return value;
    }

    public static Double byteArrayToDouble(byte[] valueByte) {
        return null;
        //todo lyh
    }

    public static Float byteArrayToFloat(byte[] valueByte) {
        return null;
        //todo lyh
    }

    public static Boolean byteArrayToBoolean(byte[] valueByte) {
        return null;
        //todo lyh
    }
}
