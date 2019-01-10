package com.baosight.xinsight.ots.rest.model.record.request;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class RecordAllQueryBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="return_columns")
    private List<String> returnColumns = new ArrayList<>();

    @JsonProperty(value="limit")
    private Long limit;

    @JsonProperty(value="cursor_mark")
    private String cursorMark;

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    @JsonIgnore
    public static RecordAllQueryBody toClass(String in) throws OtsException {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(bais, RecordAllQueryBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordAllQueryBody failed.");
        }
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

    public RecordAllQueryBody(String returnColumnsString, Long limit, String cursorMark) {
        if (!StringUtils.isBlank(returnColumnsString)){
            String[] returnColumns = returnColumnsString.split(",");
            this.returnColumns = Arrays.asList(returnColumns);
        }
        this.limit = limit;
        this.cursorMark = cursorMark;
    }


}
