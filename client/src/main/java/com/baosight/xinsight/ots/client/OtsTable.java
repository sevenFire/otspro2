package com.baosight.xinsight.ots.client;

import com.baosight.xinsight.ots.OtsConfiguration;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.metacfg.Configurator;
import com.baosight.xinsight.ots.client.metacfg.Table;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class OtsTable {
    private static final Logger LOG = Logger.getLogger(OtsTable.class);

    private Long userId;
    private Long tenantId;
    private String tableName;
    private Table info = null;
    private OtsConfiguration conf = null;

    public OtsTable(Table info, Long tenantId, OtsConfiguration conf) {
        this.info = info;
        this.tenantId = tenantId;
        this.conf = conf;
        if (info != null) {
            this.userId = info.getUserId();
            this.tableName = info.getTableName();
        }
    }

    public OtsTable(Long userId, Long tenantId, String tableName, OtsConfiguration conf) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.conf = conf;
    }

    public Table getInfo() throws ConfigException, IOException {
        if (this.info != null) {
            return this.info;
        } else { //no safe mode
            Configurator configurator = new Configurator();

            try {
                return configurator.queryTable(getTableId(), getTableName());
            } catch (ConfigException e) {
                e.printStackTrace();
                throw e;
            } finally {
                configurator.release();
            }
        }
    }

    public Long getUserId() {
        return userId;
    }

    public long getTableId() throws IOException, ConfigException {
        return getInfo().getTableId();
    }

    public String getTableName() {
        return this.tableName;
    }

    public long getTenantId() {
        return this.tenantId;
    }

    public String getTenantIdAsString() throws IOException, ConfigException {
        return String.valueOf(getTenantId());
    }

    public String getTableDesc() throws IOException, ConfigException {
        return getInfo().getTableDesc();
    }

    public Date getCreateTime() throws ConfigException, IOException {
        return getInfo().getCreateTime();
    }

    public Date getModifyTime() throws ConfigException, IOException {
        return getInfo().getModifyTime();
    }

    public long getModifier() throws ConfigException, IOException {
        return getInfo().getModifier();
    }

    public long getCreator() throws ConfigException, IOException {
        return getInfo().getCreator();
    }

    public String getPrimaryKey() throws ConfigException, IOException {
        return getInfo().getPrimaryKey();
    }

    public String getTableColumns() throws ConfigException, IOException {
        return getInfo().getTableColumns();
    }

}
