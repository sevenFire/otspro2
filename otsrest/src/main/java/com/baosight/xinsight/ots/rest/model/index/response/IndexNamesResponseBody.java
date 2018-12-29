package com.baosight.xinsight.ots.rest.model.index.response;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class IndexNamesResponseBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="total_count")
    private int totalCount;

    @JsonProperty(value="index_names")
    private List<String> indexNames = new ArrayList<>();

    public IndexNamesResponseBody() {
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    public void setIndexNames(List<String> indexNames) {
        this.indexNames = indexNames;
        this.errcode = 0L;
        this.totalCount = indexNames.size();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }


}
