package org.domain.crud.entity;

import java.io.Serializable;

import javax.persistence.Column;

public class CrudGroupOwnerNamePK implements Serializable {
    public CrudGroupOwnerNamePK() {
	}

	public CrudGroupOwnerNamePK(Integer crudGroupOwner, String name) {
		this.crudGroupOwner = crudGroupOwner;
		this.name = name;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	@Column(name="crud_group_owner")
	private Integer crudGroupOwner;
    private String name;

    public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int hashCode() {
        return (this.crudGroupOwner << 24) | this.name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CrudGroupOwnerNamePK)) return false;
        CrudGroupOwnerNamePK pk = (CrudGroupOwnerNamePK) obj;
        return pk.crudGroupOwner == this.crudGroupOwner && pk.name.equals(this.name);
    }

}
