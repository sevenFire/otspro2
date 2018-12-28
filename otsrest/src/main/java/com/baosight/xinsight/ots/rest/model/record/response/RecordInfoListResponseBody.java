package com.baosight.xinsight.ots.rest.model.record.response;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.model.record.same.RecordInfoBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class RecordInfoListResponseBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="records")
    private List<RecordInfoBody> records;

    public RecordInfoListResponseBody() {
    }

    public RecordInfoListResponseBody(List<RecordInfoBody> records) {
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
    public static TableCreateBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, RecordInfoListResponseBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordInfoListBody failed.");
        }
    }


    public List<RecordInfoBody> getRecords() {
        return records;
    }

    public void setRecords(List<RecordInfoBody> records) {
        this.records = records;
        this.errcode = 0L;
    }
}
