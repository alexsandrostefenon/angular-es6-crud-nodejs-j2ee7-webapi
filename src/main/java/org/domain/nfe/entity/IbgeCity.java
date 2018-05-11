package org.domain.nfe.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the ibge_city database table.
 * 
 */
@Entity
@Table(name="ibge_city")
@NamedQuery(name="IbgeCity.findAll", query="SELECT i FROM IbgeCity i")
public class IbgeCity implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private Integer uf;

	public IbgeCity() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Integer getUf() {
		return this.uf;
	}

	public void setUf(Integer uf) {
		this.uf = uf;
	}

}