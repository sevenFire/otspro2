package com.baosight.xinsight.ots.client;

import com.baosight.xinsight.ots.OtsConfiguration;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.SecondaryIndexException;
import com.baosight.xinsight.ots.client.metacfg.Configurator;
import com.baosight.xinsight.ots.client.metacfg.Index;
import com.baosight.xinsight.ots.client.util.HBaseConnectionUtil;
import com.baosight.xinsight.ots.client.util.TableNameUtil;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexColumnListConvert;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexInfo;
import com.baosight.xinsight.ots.common.util.SecondaryIndexUtil;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/21
 * @description
 */
public class OtsIndex {

    private static final Logger LOG = Logger.getLogger(OtsIndex.class);

    private Long tableId;
    private Long tenantId;
    private Long userId;
    private String tableName;
    private String indexName;
    private Index info = null;
    private OtsConfiguration conf = null;

    public OtsIndex(Index info, OtsConfiguration conf) {
        this.info = info;
        this.conf = conf;
        if (info != null){
            this.userId = info.getUserId();
            this.tableName = info.getTableName();
            this.indexName = info.getIndexName();
            this.tableId = info.getTableId();
        }
    }


    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Index getInfo() {
        return info;
    }

    public void setInfo(Index info) {
        this.info = info;
    }

    public OtsConfiguration getConf() {
        return conf;
    }

    public void setConf(OtsConfiguration conf) {
        this.conf = conf;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getIndexType(){
        return getInfo().getIndexType();
    }
}
