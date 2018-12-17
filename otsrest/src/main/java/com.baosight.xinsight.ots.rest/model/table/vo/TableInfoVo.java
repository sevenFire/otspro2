package com.baosight.xinsight.ots.rest.model.table.vo;

import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description 用于缓存中的数据模型
 */
public class TableInfoVo implements Serializable {

    @JsonProperty(value="table_id")
    private Long tableId;

    @JsonProperty(value="table_name")
    private String tableName;

    @JsonProperty(value="table_columns")
    private String tableColumns;

    @JsonProperty(value="primary_key")
    private String primaryKey;

    public TableInfoVo(String tableName) {
        this.tableName = tableName;
    }

    public TableInfoVo() {
    }

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(String tableColumns) {
        this.tableColumns = tableColumns;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public void fromTable(Table table) {
        this.tableColumns = table.getTableColumns();
        this.primaryKey = table.getPrimaryKey();
        this.tableId = table.getTableId();
        this.tableName = table.getTableName();
    }
}
