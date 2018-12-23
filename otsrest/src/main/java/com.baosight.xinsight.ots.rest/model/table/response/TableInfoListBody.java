package com.baosight.xinsight.ots.rest.model.table.response;

import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author liyuhui
 * @date 2018/12/23
 * @description 表信息的列表
 */
public class TableInfoListBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @JsonProperty(value="count")
    private Integer count;

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

    public TableInfoListBody(Integer count, Long errcode, List<TableInfoBody> tableInfoList) {
        this.count = count;
        this.errcode = errcode;
        this.tableInfoList = tableInfoList;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
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
}
