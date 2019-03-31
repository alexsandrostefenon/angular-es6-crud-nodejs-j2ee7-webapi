package org.domain.erp.entity;

import java.io.Serializable;

public class CrudGroupOwnerRequestPK implements Serializable {
    public CrudGroupOwnerRequestPK() {
	}

	public CrudGroupOwnerRequestPK(Integer crudGroupOwner, Integer request) {
		this.crudGroupOwner = crudGroupOwner;
		this.request = request;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer crudGroupOwner;
    private Integer request;

	public int hashCode() {
        return (crudGroupOwner << 24) | request;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CrudGroupOwnerRequestPK)) return false;
        CrudGroupOwnerRequestPK pk = (CrudGroupOwnerRequestPK) obj;
        return pk.request == this.request && pk.crudGroupOwner == this.crudGroupOwner;
    }

	public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}

	public Integer getRequest() {
		return request;
	}

	public void setRequest(Integer request) {
		this.request = request;
	}

}
