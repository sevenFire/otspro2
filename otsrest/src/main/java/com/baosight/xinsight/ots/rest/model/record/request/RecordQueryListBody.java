package com.baosight.xinsight.ots.rest.model.record.request;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/25
 * @description
 */
public class RecordQueryListBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="primary_key_list")
    private List<JsonNode> primaryKeyList = new ArrayList<>();

    @JsonProperty(value="return_columns")
    private List<String> returnColumns = new ArrayList<>();

    @JsonProperty(value="limit")
    private Long limit;

    @JsonProperty(value="range_key_cursor_mark")
    private String cursorMark;

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    @JsonIgnore
    public static RecordQueryListBody toClass(String in) throws OtsException {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(bais, RecordQueryListBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordQueryListBody failed.");
        }
    }

    public List<JsonNode> getPrimaryKeyList() {
        return primaryKeyList;
    }

    public void setPrimaryKeyList(List<JsonNode> primaryKeyList) {
        this.primaryKeyList = primaryKeyList;
    }

    public List<String> getReturnColumns() {
        return returnColumns;
    }

    public void setReturnColumns(List<String> returnColumns) {
        this.returnColumns = returnColumns;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public String getCursorMark() {
        return cursorMark;
    }

    public void setCursorMark(String cursorMark) {
        this.cursorMark = cursorMark;
    }

}
