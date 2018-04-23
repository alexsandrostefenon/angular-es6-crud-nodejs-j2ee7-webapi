package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the confaz_cest database table.
 * 
 */
@Embeddable
public class ConfazCestPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer ncm;

	public ConfazCestPK() {
	}

	public Integer getId() {
		return this.id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNcm() {
		return this.ncm;
	}
	public void setNcm(Integer ncm) {
		this.ncm = ncm;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ConfazCestPK)) {
			return false;
		}
		ConfazCestPK castOther = (ConfazCestPK)other;
		return 
			this.id.equals(castOther.id)
			&& this.ncm.equals(castOther.ncm);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.id.hashCode();
		hash = hash * prime + this.ncm.hashCode();
		
		return hash;
	}
}