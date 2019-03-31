package org.domain.crud.entity;

import java.io.Serializable;

public class CrudGroupUserPK implements Serializable {
    public CrudGroupUserPK() {
	}

	public CrudGroupUserPK(Integer crudGroup, String crudUser) {
		this.crudGroup = crudGroup;
		this.crudUser = crudUser;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer crudGroup;
    private String crudUser;

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CrudGroupUserPK)) return false;
        CrudGroupUserPK pk = (CrudGroupUserPK) obj;
        return pk.crudGroup == this.crudGroup && pk.crudUser.equals(this.crudUser);
    }

	public String getCrudUser() {
		return crudUser;
	}

	public void setCrudUser(String crudUser) {
		this.crudUser = crudUser;
	}

	public Integer getCrudGroup() {
		return crudGroup;
	}

	public void setCrudGroup(Integer crudGroup) {
		this.crudGroup = crudGroup;
	}

	public int hashCode() {
        return (this.crudGroup << 24) | this.crudUser.hashCode();
    }

}
