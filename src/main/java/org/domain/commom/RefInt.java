package org.domain.commom;

public class RefInt {
	
	public int value;
	
	public RefInt() {
		this.value = 0;
	}

	public String getString() {
		Integer _value = new Integer(this.value);
		return _value.toString();
	}
}
