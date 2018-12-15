package com.baosight.xinsight.ots.client.Database.HBase;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.client.Admin;

import java.io.IOException;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description
 */
public class HBaseMetricsProvider {

    /**
     * 查看tenantId下是否有表存在
     */
    public static boolean isNamespaceExist(Admin admin, String tenantid) throws IOException {
        NamespaceDescriptor descriptor;
        try {
            descriptor = admin.getNamespaceDescriptor(tenantid);
        } catch (NamespaceNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        return descriptor==null?false:true;
    }
}
