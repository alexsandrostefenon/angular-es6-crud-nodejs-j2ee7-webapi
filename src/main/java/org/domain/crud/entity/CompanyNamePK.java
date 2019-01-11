package org.domain.crud.entity;

import java.io.Serializable;

public class CompanyNamePK implements Serializable {
    public CompanyNamePK() {
	}

	public CompanyNamePK(Integer company, String name) {
		this.company = company;
		this.name = name;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer company;
    private String name;

    public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int hashCode() {
        return (this.company << 24) | this.name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CompanyNamePK)) return false;
        CompanyNamePK pk = (CompanyNamePK) obj;
        return pk.company == this.company && pk.name.equals(this.name);
    }

}
