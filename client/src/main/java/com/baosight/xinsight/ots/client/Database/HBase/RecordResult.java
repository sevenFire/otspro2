package com.baosight.xinsight.ots.client.Database.HBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long count;
	
	private String next_rowKey;//base64
		
	private List<RowRecord> recordList = new ArrayList<>();
	
	/**
	 * Default constructor
	 */	
	public RecordResult() {
		super();
		this.count = 0L;
		this.next_rowKey = null;
		this.recordList.clear();
	}
	
	public RecordResult(Long count, List<RowRecord> recordList) {
		super();
		this.count = count;
		this.next_rowKey = null;
		this.recordList = recordList;
	}
	
	public RecordResult(Long count, String next_rowKey, List<RowRecord> recordList) {
		super();
		this.count = count;
		this.next_rowKey = next_rowKey;
		this.recordList = recordList;
	}

	public String getNext_rowKey() {
		return next_rowKey;
	}

	public void setNext_rowKey(String next_rowKey) {
		this.next_rowKey = next_rowKey;
	}

	public List<RowRecord> getRecordList() {
		return recordList;
	}

	public void setRecordList(List<RowRecord> recordList) {
		this.recordList = recordList;
	}

	public void add(RowRecord rowRecord) {
		recordList.add(rowRecord);
	}
	
	public int size() {
		return recordList.size();
	}
	
	public RowRecord getRowRecord(int index) {
		return recordList.get(index);
	}

	public void clear() {
		recordList.clear();
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
