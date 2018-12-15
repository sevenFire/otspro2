package com.baosight.xinsight.ots.client.Database.HBase;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.constants.TableConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.util.Bytes;

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
     * @param tenantId
     * @return
     */
    public static void createHBaseTable(Admin admin, String tenantId) throws TableException, IOException {
        //创建namespace
        createNamespaceIfNotExist(admin, tenantId);

        //建表
        HTableDescriptor htableDescriptor = new HTableDescriptor(TableName.valueOf(TableConstants.HBASE_TABLE_PREFIX + tenantId));

        //建列族
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME));
        hColumnDescriptor.setScope(OtsConstants.DEFAULT_REPLICATED_SCOPE);
        //todo lyh 如果不设置，HBase的默认compressionType是什么？
        //todo lyh一些其他需要加的参数
        htableDescriptor.addFamily(hColumnDescriptor);

        admin.createTable(htableDescriptor);
    }

    /**
     * 创建namespace
     * @param admin
     * @param tenantId
     * @throws IOException
     */
    private static void createNamespaceIfNotExist(Admin admin, String tenantId) throws IOException, TableException {
        if (StringUtils.isBlank(tenantId)){
            throw new TableException(OtsErrorCode.EC_OTS_STORAGE_INVALID_TENANT, "namespace: 1(ots) is invalid!");
        }

        NamespaceDescriptor descriptor = null;

        try {
            descriptor = admin.getNamespaceDescriptor(tenantId);
        } catch (NamespaceNotFoundException e) {
            if (descriptor == null) {
                admin.createNamespace(NamespaceDescriptor.create(tenantId).build());
            }
        }
    }


}
