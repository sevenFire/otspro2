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
public class RecordListBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="records")
    private List<JsonNode> recordList = new ArrayList<>();

    public List<JsonNode> getRecordList() {
        return recordList;
    }

    public void setRecordList(List<JsonNode> recordList) {
        this.recordList = recordList;
    }

    @JsonIgnore
    public void addRecord(JsonNode record) {
        recordList.add(record);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }


    @JsonIgnore
    public static RecordListBody toClass(String in) throws OtsException {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(bais, RecordListBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordListBody failed.");
        }
    }
}
