package org.domain.erp.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the request_nfe database table.
 * 
 */
@Embeddable
public class RequestNfePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	@Column(name="crud_group_owner")
	private Integer crudGroupOwner;
	private Integer request;

	public RequestNfePK() {
	}

	@Column(insertable=false, updatable=false)
	public Integer getRequest() {
		return this.request;
	}
	public void setRequest(Integer request) {
		this.request = request;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RequestNfePK)) {
			return false;
		}
		RequestNfePK castOther = (RequestNfePK)other;
		return 
			this.crudGroupOwner.equals(castOther.crudGroupOwner)
			&& this.request.equals(castOther.request);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.crudGroupOwner.hashCode();
		hash = hash * prime + this.request.hashCode();
		
		return hash;
	}

	public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}
}