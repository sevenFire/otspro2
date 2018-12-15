package com.baosight.xinsight.ots.rest.model.table.response;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/14
 * @description
 */
public class TableNameListBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="total_count")
    private int totalCount;

    @JsonProperty(value="table_names")
    private List<String> tableNames = new ArrayList<>();

    public TableNameListBody() {
    }

    public TableNameListBody(int totalCount, List<String> tableNames) {
        this.totalCount = totalCount;
        this.tableNames = tableNames;
    }

    /**
     * 将实体类的参数转入其中
     * @param tableNameList
     */
    public void fromTableNameList(List<String> tableNameList) {
        this.totalCount = tableNameList.size();
        this.tableNames = tableNameList;

    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }


}
