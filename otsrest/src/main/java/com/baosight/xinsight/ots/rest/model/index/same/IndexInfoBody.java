package com.baosight.xinsight.ots.rest.model.index.same;

import com.baosight.xinsight.ots.client.OtsIndex;
import com.baosight.xinsight.ots.client.metacfg.Index;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/28
 * @description
 */
public class IndexInfoBody implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) //if null, will not show in the results
    private Long errcode = 0L;

    @JsonProperty(value="index_id")
    private Long indexId;

    @JsonProperty(value="index_name")
    private String indexName;

    @JsonProperty(value="index_type")
    private String indexType;

    @JsonProperty(value="index_key")//索引列
    private List<IndexColumnsBody> indexKey = new ArrayList<>();

    @JsonProperty(value="create_time")
    private String createTime;

    @JsonProperty(value="modify_time")
    private String modifyTime;

    @JsonProperty(value="creator")
    private Long creator;

    @JsonProperty(value="modifier")
    private Long modifier;

    public IndexInfoBody(String indexName) {
        this.indexName = indexName;
    }

    public IndexInfoBody(String indexName, String indexType) {
        this.indexName = indexName;
        this.indexType = indexType;
    }

    public IndexInfoBody() {
    }

    public void addColumn(String colName, String colType, Integer realColMaxLen) {
        IndexColumnsBody indexColumnsBody = new IndexColumnsBody(colName,colType,realColMaxLen);
        indexKey.add(indexColumnsBody);
    }

    @JsonIgnore
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
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

    public void setIndexKey(List<IndexColumnsBody> indexColumns) {
        this.indexKey = indexColumns;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Long getModifier() {
        return modifier;
    }

    public void setModifier(Long modifier) {
        this.modifier = modifier;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * 将body中的参数写入实体类中
     * @return
     */
    public Index fromBodyToIndex() {
        Index index = new Index();
        index.setIndexName(indexName);
        index.setIndexKey(JsonUtil.toJsonString(indexKey));
        index.setIndexType(indexType);

        return index;
    }
}
