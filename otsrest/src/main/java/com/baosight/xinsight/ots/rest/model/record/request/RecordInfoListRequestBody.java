package com.baosight.xinsight.ots.rest.model.record.request;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
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
 * @date 2018/12/27
 * @description 插入/修改记录的请求体
 */
public class RecordInfoListRequestBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="records")
    private List<JsonNode> records = new ArrayList<>();

    public RecordInfoListRequestBody() {
    }

    public RecordInfoListRequestBody(List<JsonNode> records) {
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
    public static RecordInfoListRequestBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, RecordInfoListRequestBody.class);
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
    }
}
