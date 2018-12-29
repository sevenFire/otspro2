package com.baosight.xinsight.ots.rest.model.index.same;

import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @author liyuhui
 * @date 2018/12/27
 * @description
 */
public class IndexColumnsBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="col_name")
    private String colName;

    @JsonProperty(value="col_type")
    private String colType;

    @JsonProperty(value="col_maxLen")
    private Integer colMaxLen;

    public IndexColumnsBody(String colName, String colType, Integer colMaxLen) {
        this.colName = colName;
        this.colType = colType;
        this.colMaxLen = colMaxLen;
    }

    public IndexColumnsBody() {
    }

    @JsonIgnore
    @Override
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

    public Integer getColMaxLen() {
        return colMaxLen;
    }

    public void setColMaxLen(Integer colMaxLen) {
        this.colMaxLen = colMaxLen;
    }
}
