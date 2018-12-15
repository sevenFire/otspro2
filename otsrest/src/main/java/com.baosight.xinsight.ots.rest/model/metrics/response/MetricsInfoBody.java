package com.baosight.xinsight.ots.rest.model.metrics.response;

import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

public class MetricsInfoBody implements Serializable {

	@JsonIgnore
	private static final long serialVersionUID = 1L;

	@JsonProperty(value="errcode")
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	private Long errcode;

	@JsonProperty("table_name")
	private String tableName;

	@JsonProperty("read_count")
	private Long readCount;

	@JsonProperty("write_count")
	private Long writeCount;

	@JsonProperty("disk_size")
	private Long diskSize;

	@JsonIgnore
	@JsonProperty("record_count")
	private Long recordCount;

	public MetricsInfoBody() {
	}

	public MetricsInfoBody(String tableName, Long readCount, Long writeCount, Long diskSize, Long recordCount) {
		this.tableName = tableName;
		this.readCount = readCount;
		this.writeCount = writeCount;
		this.diskSize = diskSize;
		this.recordCount = recordCount;
	}

	public MetricsInfoBody(String tableName) {
		super();
		this.tableName = tableName;
	}

	public MetricsInfoBody(String tableName, long errcode) {
		super();
		this.tableName = tableName;
		this.errcode = errcode;
	}

	@JsonIgnore
	@Override
    public String toString() {
		return JsonUtil.toJsonString(this);
    }
	
	@JsonIgnore
    public static MetricsInfoBody toClass(String in) throws OtsException {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes(OtsConstants.DEFAULT_ENCODING));
			return JsonUtil.readJsonFromStream(bais, MetricsInfoBody.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_JSON2OBJECT, "convert json input to MetricsModel failed.");
		}		
    }

	public Long getErrcode() {
		return errcode;
	}

	public void setErrcode(Long errcode) {
		this.errcode = errcode;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Long getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(Long writeCount) {
		this.writeCount = writeCount;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}

	public Long getReadCount() {
		return readCount;
	}

	public void setReadCount(Long readCount) {
		this.readCount = readCount;
	}

	public Long getDiskSize() {
		return diskSize;
	}

	public void setDiskSize(Long diskSize) {
		this.diskSize = diskSize;
	}
}
