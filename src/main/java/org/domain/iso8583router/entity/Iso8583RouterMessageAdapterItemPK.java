package org.domain.iso8583router.entity;

import java.io.Serializable;

public class Iso8583RouterMessageAdapterItemPK implements Serializable {
	private static final long serialVersionUID = -3903151553731326294L;
	private String messageAdapter;
    private String rootPattern;
    private String tag;

    public Iso8583RouterMessageAdapterItemPK() {
	}

	public Iso8583RouterMessageAdapterItemPK(String messageAdapter, String rootPattern, String tag) {
		this.messageAdapter = messageAdapter;
		this.rootPattern = rootPattern;
		this.tag = tag;
	}

	public int hashCode() {
		String str = this.messageAdapter + this.rootPattern + this.tag;
        return str.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Iso8583RouterMessageAdapterItemPK)) return false;
        Iso8583RouterMessageAdapterItemPK pk = (Iso8583RouterMessageAdapterItemPK) obj;
        
        if (this.messageAdapter != null && this.messageAdapter.equals(pk.messageAdapter) == false) return false;
        if (this.rootPattern != null && this.rootPattern.equals(pk.rootPattern) == false) return false;
        if (this.tag != null && this.tag.equals(pk.tag) == false) return false;
        return true;
    }

	public String getMessageAdapter() {
		return messageAdapter;
	}

	public void setMessageAdapter(String messageAdapter) {
		this.messageAdapter = messageAdapter;
	}

	public String getRootPattern() {
		return rootPattern;
	}

	public void setRootPattern(String rootPattern) {
		this.rootPattern = rootPattern;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
