package com.baosight.xinsight.ots.rest.model.table.response;

import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/23
 * @description 表信息的列表
 */
public class TableInfoListBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @JsonProperty(value="total_count")
    private Integer totalCount;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode;

    @JsonProperty(value="table_info_list")
    private List<TableInfoBody> tableInfoList = new ArrayList<>();

    public TableInfoListBody() {
    }

    public TableInfoListBody(Long errcode) {
        this.errcode = errcode;
    }

    public TableInfoListBody(Integer totalCount, Long errcode, List<TableInfoBody> tableInfoList) {
        this.totalCount = totalCount;
        this.errcode = errcode;
        this.tableInfoList = tableInfoList;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }

    public List<TableInfoBody> getTableInfoList() {
        return tableInfoList;
    }

    public void setTableInfoList(List<TableInfoBody> tableInfoList) {
        this.tableInfoList = tableInfoList;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public void addTable(TableInfoBody tableInfoBody) {
        this.tableInfoList.add(tableInfoBody);

    }
}
