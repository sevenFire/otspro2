package com.baosight.xinsight.ots.rest.model.index.response;

import com.baosight.xinsight.ots.rest.model.index.same.IndexInfoBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/28
 * @description
 */
public class IndexInfoListResponseBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="index_info_list")
    private List<IndexInfoBody> indexInfoList = new ArrayList<>();

    @JsonProperty(value="total_count")
    private int totalCount;

    @JsonProperty(value="cursor_mark")
    private int cursorMark;



    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }


    public List<IndexInfoBody> getIndexInfoList() {
        return indexInfoList;
    }

    public void setIndexInfoList(List<IndexInfoBody> indexInfoList) {
        this.indexInfoList = indexInfoList;
        this.totalCount = indexInfoList.size();
        this.errcode = 0L;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCursorMark() {
        return cursorMark;
    }

    public void setCursorMark(int cursorMark) {
        this.cursorMark = cursorMark;
    }
}
