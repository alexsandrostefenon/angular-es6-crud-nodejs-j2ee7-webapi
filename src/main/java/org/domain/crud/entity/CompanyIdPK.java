package org.domain.crud.entity;

import java.io.Serializable;

public class CompanyIdPK implements Serializable {
    public CompanyIdPK() {
	}

	public CompanyIdPK(Integer company, Integer id) {
		this.company = company;
		this.id = id;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer company;
    private Integer id;

    public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int hashCode() {
        return (company << 24) | id;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CompanyIdPK)) return false;
        CompanyIdPK pk = (CompanyIdPK) obj;
        return pk.id == this.id && pk.company == this.company;
    }

}
