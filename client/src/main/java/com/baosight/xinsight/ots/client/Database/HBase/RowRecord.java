package com.baosight.xinsight.ots.client.Database.HBase;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class RowRecord implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private byte[] rowkey = null;
	
	private List<RowCell> cellList = new ArrayList<>();
	
	/**
	 * Default constructor
	 */
	public RowRecord() {}	
	
	public RowRecord(List<RowCell> cellList) {
		super();
		this.cellList = cellList;
	}
	
	public RowRecord(Result rec) {
		this.rowkey = rec.getRow();

		for (Cell cell : rec.rawCells()) {
			if (cell != null) {
				byte[] cellname = CellUtil.cloneQualifier(cell);
				//if (0 != Bytes.compareTo(cellname, Bytes.toBytes(OtsConstants.DEFAULT_ROWKEY_NAME))) {
					RowCell cellRow = new RowCell(cellname, CellUtil.cloneValue(cell));
					this.addCell(cellRow);
				//}
			}
		}
	}

	public List<RowCell> getCellList() {
		return cellList;
	}

	public void setCellList(List<RowCell> cellList) {
		this.cellList = cellList;
	}

	public void addCell(RowCell c) {
		if (c != null) {
			cellList.add(c);
		}
	}

	public void clear() {
		cellList.clear();
	}

	public byte[] getRowkey() {
		return rowkey;
	}
	
	public void setRowkey(byte[] rowkey) {
		this.rowkey = rowkey;
	}
	
	public boolean hasRowkey() {
		if (rowkey != null) {
			return true;
		} 
		
//		for (RowCell cell : cellList) {
//			if (cell != null && 0 == Bytes.compareTo(cell.getName(), Bytes.toBytes(OtsConstants.DEFAULT_ROWKEY_NAME))) {
//				rowkey = cell.getValue();
//				return true;
//			}
//		}		
		
		return false;
	}

	/**
	 * 重写toString把rowkey返回
	 * @return
	 */
	@Override
	public String toString(){
		return new String(rowkey);
	}



}
