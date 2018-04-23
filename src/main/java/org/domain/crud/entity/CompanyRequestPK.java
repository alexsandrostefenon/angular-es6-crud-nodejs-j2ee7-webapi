package org.domain.crud.entity;

import java.io.Serializable;

public class CompanyRequestPK implements Serializable {
    public CompanyRequestPK() {
	}

	public CompanyRequestPK(Integer company, Integer request) {
		this.company = company;
		this.request = request;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5712180195559551807L;
	private Integer company;
    private Integer request;

    public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}


	public int hashCode() {
        return (company << 24) | request;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof CompanyRequestPK)) return false;
        CompanyRequestPK pk = (CompanyRequestPK) obj;
        return pk.request == this.request && pk.company == this.company;
    }

}
