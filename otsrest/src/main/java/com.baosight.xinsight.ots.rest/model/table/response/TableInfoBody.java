package com.baosight.xinsight.ots.rest.model.table.response;

import com.alibaba.fastjson.JSON;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.rest.model.table.operate.TableColumnsBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author liyuhui
 * @date 2018/12/14
 * @description 表的详细参数
 */
public class TableInfoBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="table_name")
    private String tableName;

    @JsonProperty(value="table_desc")
    private String tableDesc;

    //table_columns是一个数组，且每个元素又有多个属性
    @JsonProperty(value="table_columns")
    private List<TableColumnsBody> tableColumns = new ArrayList<TableColumnsBody>();

    @JsonProperty(value="primary_key")
    private List<String> primaryKey = new ArrayList<>();

    @JsonProperty(value="create_time")
    private String createTime;

    @JsonProperty(value="modify_time")
    private String modifyTime;

    @JsonProperty(value="creator")
    private Long creator;

    @JsonProperty(value="modifier")
    private Long modifier;



    public TableInfoBody() {
    }

    public TableInfoBody(String tableName,
                         String tableDesc,
                         List<TableColumnsBody> tableColumns,
                         List<String> primaryKey,
                         String createTime,
                         String modifyTime,
                         Long creator,
                         Long modifier) {
        this.tableName = tableName;
        this.tableDesc = tableDesc;
        this.tableColumns = tableColumns;
        this.primaryKey = primaryKey;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.creator = creator;
        this.modifier = modifier;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }

    public List<TableColumnsBody> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<TableColumnsBody> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Long getModifier() {
        return modifier;
    }

    public void setModifier(Long modifier) {
        this.modifier = modifier;
    }

    /**
     * 将实体中的参数存入其中
     * @param table
     */
    public void fromTable(Table table) {
        this.tableName = table.getTableName();
        this.tableDesc = table.getTableDesc();

        //需要特殊处理的参数
        this.tableColumns = JSON.parseArray(table.getTableColumns(),TableColumnsBody.class);
        this.primaryKey = JSON.parseArray(table.getPrimaryKey(),String.class);

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间格式化的格式
        this.createTime = sDateFormat.format(table.getCreateTime());
        this.modifyTime = sDateFormat.format(table.getModifyTime());
        this.creator = table.getCreator();
        this.modifier = table.getModifier();

    }

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
