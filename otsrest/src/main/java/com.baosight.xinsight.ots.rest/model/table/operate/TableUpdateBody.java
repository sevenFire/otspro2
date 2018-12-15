package com.baosight.xinsight.ots.rest.model.table.operate;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * @author liyuhui
 * @date 2018/12/14
 * @description 修改时的请求参数
 */
public class TableUpdateBody implements Serializable {

    @JsonProperty(value="table_name")
    private String tableName;

    @JsonProperty(value="table_desc")
    private String tableDesc;

    public TableUpdateBody() {
    }

    public TableUpdateBody(String tableName, String tableDesc) {
        this.tableName = tableName;
        this.tableDesc = tableDesc;
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


    /**
     * 将json串转换成实体类
     * @param in
     * @return
     * @throws OtsException
     */
    @JsonIgnore
    public static TableUpdateBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, TableUpdateBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to TableUpdateBody failed.");
        }
    }

    /**
     * 将请求体中的参数放入table的实体类中
     * @return
     */
    public Table toTable() {
        Table table = new Table();
        table.setTableName(tableName);
        table.setTableDesc(tableDesc);

        return table;
    }
}
