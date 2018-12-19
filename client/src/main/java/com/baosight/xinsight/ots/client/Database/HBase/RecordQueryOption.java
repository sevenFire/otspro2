package com.baosight.xinsight.ots.client.Database.HBase;

import org.apache.hadoop.hbase.filter.Filter;

import java.util.List;


/**
 * 
 * @author huangming
 *
 */
public class RecordQueryOption {
	protected List<String> returnColumns;
	protected Long limit;
	protected Long offset;
	protected  Integer caching;
	private String cursor_mark;
	protected Boolean descending;
	protected Filter filter;
//	protected Map<String, byte[]> hbase_attributes;
	
	public RecordQueryOption(){
	}

	public RecordQueryOption(List<String>  columns, Long limit, Long offset, Boolean descending) {
		this(columns, limit, offset, null, descending, null);
	}

	public RecordQueryOption(List<String>  columns, String cursor_mark, Boolean descending) {
		this(columns, null, null, cursor_mark, descending, null);
	}

	public RecordQueryOption(List<String>  columns, Long limit, String cursor_mark, Boolean descending) {
		this(columns, limit, null, cursor_mark, descending, null);
	}
	
	public RecordQueryOption(List<String> returnColumns, Long limit, Long offset, String cursor_mark, Boolean descending,
                             Filter filter) {
		this.returnColumns = returnColumns;
		this.limit = limit;
		this.offset = offset;
		this.cursor_mark = cursor_mark;
		this.descending = descending;
		this.filter = filter;
//		this.hbase_attributes = hbase_attributes;
	}

	public boolean hasReturnColumns() {
		if (returnColumns == null || returnColumns.isEmpty()) {
			return false;
		}
				
		return true;
	}
	
	public boolean hasLimit() {
		return limit==null?false:true;
	}
	
	public boolean hasIterate() {
		return cursor_mark==null?false:true;
	}
	
	public boolean hasCaching() {
		return caching==null?false:true;
	}
	
	public boolean hasOffset() {
		return offset==null?false:true;
	}
	
	public boolean isValidPage() {
		if (hasIterate()) { //first
			return true;
		}
		
		if (hasLimit()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 是否降序排列,默认降序
	 * 
	 * @since 2015-01-12
	 */
	public boolean isDescending() {
		if (descending == null) {
			return false;
		}
		
		return descending.booleanValue();
	}
	
	public boolean onlyGetRowKey() {	
//		if (hasColumns()) {
//			if (columns.size() == 1 && columns.get(0).equals(OtsConstants.DEFAULT_ROWKEY_NAME)) {
//				return true;
//			}
//		}
		
		return false;
	}
	
	public List<String>  getReturnColumns() {
		return returnColumns;
	}

	public void setReturnColumns(List<String>  returnColumns) {
		this.returnColumns = returnColumns;
	}

	public Long getLimit() {
		return limit==null?0:limit.longValue();
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public Long getOffset() {
		return offset==null?0:offset.longValue();
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}
	
	public Boolean getDescending() {
		return descending;
	}

	public void setDescending(Boolean descending) {
		this.descending = descending;
	}

	public boolean getIsSort() {
		return descending==null?false:true;
	}
	
	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
//	public Map<String, byte[]> getHbase_attributes() {
//		return hbase_attributes;
//	}
//
//	public void setHbase_attributes(Map<String, byte[]> hbase_attributes) {
//		this.hbase_attributes = hbase_attributes;
//	}

	public String getCursor_mark() {
		return cursor_mark;
	}

	public void setCursor_mark(String cursor_mark) {
		this.cursor_mark = cursor_mark;
	}
	
	public Integer getCaching() {
		return caching;
	}

	public void setCaching(Integer caching) {
		this.caching = caching;
	}
}
