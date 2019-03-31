package org.domain.crud.entity;

import java.io.Serializable;

public class CrudGroupOwnerIdPK implements Serializable {
    public CrudGroupOwnerIdPK() {
	}

	public CrudGroupOwnerIdPK(Integer crudGroupOwner, Integer id) {
		this.crudGroupOwner = crudGroupOwner;
		this.id = id;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer crudGroupOwner;
    private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int hashCode() {
        return (crudGroupOwner << 24) | id;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CrudGroupOwnerIdPK)) return false;
        CrudGroupOwnerIdPK pk = (CrudGroupOwnerIdPK) obj;
        return pk.id == this.id && pk.crudGroupOwner == this.crudGroupOwner;
    }

	public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}

}
