package com.baosight.xinsight.ots.rest.model.index.request;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.model.index.same.IndexColumnsBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/27
 * @description
 */
public class IndexCreateBody implements Serializable {
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="index_type")
    private String indexType;

    //table_columns是一个数组，且每个元素又有多个属性
    @JsonProperty(value="index_key")
    private List<IndexColumnsBody> indexKey = new ArrayList<>();

    public IndexCreateBody() {
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
    public static IndexCreateBody toClass(String in) throws OtsException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
            return JsonUtil.readJsonFromStream(byteArrayInputStream, IndexCreateBody.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to IndexCreateBody failed.");
        }
    }


    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public List<IndexColumnsBody> getIndexKey() {
        return indexKey;
    }

    public void setIndexKey(List<IndexColumnsBody> indexKey) {
        this.indexKey = indexKey;
    }
}
