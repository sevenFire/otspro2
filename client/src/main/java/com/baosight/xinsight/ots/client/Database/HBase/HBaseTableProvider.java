package com.baosight.xinsight.ots.client.Database.HBase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;

import java.io.IOException;

/**
 * @author liyuhui
 * @date 2018/12/13
 * @description 处理HBase表中的增删该查
 */
public class HBaseTableProvider {

    /**
     * 在HBase中根据表名判定表是否存在
     * @param admin
     * @param tableName
     * @return
     */
    public static boolean isHBaseTableExist(Admin admin, TableName tableName) throws IOException {
        return admin.tableExists(tableName);
    }

    /**
     * 在HBase中创建表
     * @param admin
     * @param s
     * @return
     */
    public static void createHBaseTable(Admin admin, String s) {

    }
}
