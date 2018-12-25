package com.lyh.sample.rest.model;



import com.lyh.sample.rest.util.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;


/**
 * @author liyuhui
 * @date 2018/12/12
 * @description 表的列信息
 */
public class TableColumnsBody implements Serializable{

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="col_name")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private String colName;

    @JsonProperty(value="col_type")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private String colType;

    public TableColumnsBody() {
    }

    public TableColumnsBody(String colName, String colType) {
        this.colName = colName;
        this.colType = colType;
    }

    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

}
