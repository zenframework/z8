package org.zenframework.z8.pde.source;

public class Transaction implements Comparable<Transaction> {

	public Transaction(int length, int offset, String what) {
		this.length = length;
		this.offset = offset;
		this.what = what;
	}

	public Transaction(int deleteLength, int deleteOffset) {
		this(deleteLength, deleteOffset, "");
	}

	public Transaction(int offset, String insert) {
		this(0, offset, insert);
	}

	private int length, offset, helperIndex;

	public void setHelperIndex(int helperIndex) {
		this.helperIndex = helperIndex;
	}

	private String what;

	@Override
	public int compareTo(Transaction arg0) {
		if(offset == arg0.offset)
			return helperIndex - arg0.helperIndex;
		return offset - arg0.offset;
	}

	public int getLength() {
		return length;
	}

	public int getOffset() {
		return offset;
	}

	public String getWhat() {
		return what;
	}

	public int getShift() {
		return what.length() - length;
	}

	public void shift(int len) {
		offset += len;
	}
}
