package com.baosight.xinsight.ots.rest.model.record.response;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class RecordInfoListResponseBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="err_code")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="records")
    private List<JsonNode> records = new ArrayList<>();

    @JsonProperty(value="range_key_next_cursor_mark")
    private int cursorMark;

    @JsonProperty(value="total_count")
    private int totalCount;

    public RecordInfoListResponseBody() {
    }

    public RecordInfoListResponseBody(List<JsonNode> records) {
        this.records = records;
    }

    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    /**
     * 将json串转换成实体body
     * @param in
     * @return
     * @throws OtsException
     */
    @JsonIgnore
    public static RecordInfoListResponseBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, RecordInfoListResponseBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordInfoListBody failed.");
        }
    }


    public List<JsonNode> getRecords() {
        return records;
    }

    public void setRecords(List<JsonNode> records) {
        this.records = records;
        this.errcode = 0L;
        this.totalCount = records.size();
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

    public void addRecord(JsonNode record) {
        this.records.add(record);
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }
}
