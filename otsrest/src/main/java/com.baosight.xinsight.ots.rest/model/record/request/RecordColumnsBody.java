package com.baosight.xinsight.ots.rest.model.record.request;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

/**
 * @author liyuhui
 * @date 2018/12/24
 * @description
 */
public class RecordColumnsBody implements Serializable{

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="col_name")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private String colName;

    @JsonProperty(value="col_start")
    @JsonSerialize
    private String colStart;

    @JsonProperty(value="col_end")
    @JsonSerialize
    private String colEnd;

    @JsonProperty(value="col_value")
    @JsonSerialize
    private String colValue;

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }


    @JsonIgnore
    public static RecordColumnsBody toClass(String in) throws OtsException {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(bais, RecordColumnsBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to RecordColumnsBody failed.");
        }
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getColStart() {
        return colStart;
    }

    public void setColStart(String colStart) {
        this.colStart = colStart;
    }

    public String getColEnd() {
        return colEnd;
    }

    public void setColEnd(String colEnd) {
        this.colEnd = colEnd;
    }

    public String getColValue() {
        return colValue;
    }

    public void setColValue(String colValue) {
        this.colValue = colValue;
    }
}
