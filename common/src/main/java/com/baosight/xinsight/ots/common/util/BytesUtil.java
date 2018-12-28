package com.baosight.xinsight.ots.common.util;

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

}
