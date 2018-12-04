package org.domain.nfe.entity;

import java.io.Serializable;
import javax.persistence.*;

import org.domain.nfe.entity.ConfazCestPK;

/**
 * The persistent class for the confaz_cest database table.
 * 
 */
@Entity
@Table(name="confaz_cest")
@NamedQuery(name="ConfazCest.findAll", query="SELECT c FROM ConfazCest c")
public class ConfazCest implements Serializable {
	private static final long serialVersionUID = 1L;
	private ConfazCestPK id;
	private String name;

	public ConfazCest() {
	}


	@EmbeddedId
	public ConfazCestPK getId() {
		return this.id;
	}

	public void setId(ConfazCestPK id) {
		this.id = id;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}