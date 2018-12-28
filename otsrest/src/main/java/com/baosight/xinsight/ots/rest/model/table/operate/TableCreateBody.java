package com.baosight.xinsight.ots.rest.model.table.operate;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author liyuhui
 * @date 2018/12/12
 * @description 创建表的请求参数
 */
public class TableCreateBody implements Serializable{
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="table_desc")
    private String tableDesc;

    //table_columns是一个数组，且每个元素又有多个属性
    @JsonProperty(value="table_columns")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private List<TableColumnsBody> tableColumns = new ArrayList<>();

    @JsonProperty(value="primary_key")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private List<String> primaryKey = new ArrayList<>();

    public TableCreateBody() {
    }

    public TableCreateBody(String tableDesc, List<TableColumnsBody> tableColumns, List<String> primaryKey) {
        this.tableDesc = tableDesc;
        this.tableColumns = tableColumns;
        this.primaryKey = primaryKey;
    }

    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    /**
     * 将实体body中的参数放入table的实体类中
     * @return
     */
    public Table toTable() {
        Table table = new Table();
        table.setTableDesc(tableDesc);
        //注意，不能用toString，不然结果会是[col1, col2, col3]而非["col1","col2","col3"]，插入数据库会出错。
        table.setPrimaryKey(JsonUtil.toJsonString(primaryKey));
        table.setTableColumns(JsonUtil.toJsonString(tableColumns));

        return table;
    }
    /**
     * 将json串转换成实体body
     * @param in
     * @return
     * @throws OtsException
     */
    @JsonIgnore
    public static TableCreateBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, TableCreateBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to TableCreateBody failed.");
        }
    }

    public List<TableColumnsBody> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<TableColumnsBody> tableColumns) {
        this.tableColumns = tableColumns;
    }

    public String getTableDesc() {
        return tableDesc;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setTableDesc(String tableDesc) {
        this.tableDesc = tableDesc;
    }

}
