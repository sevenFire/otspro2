package com.baosight.xinsight.ots.rest.model.record.same;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description
 */
public class RecordInfoBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="records")
    private List<Map<String,Object>> records;

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
            return JsonUtil.readJsonFromStream(byteArrayInputStream, RecordInfoBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordInfoBody failed.");
        }
    }

    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public List<Map<String, Object>> getRecords() {
        return records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }
}
