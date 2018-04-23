package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the ibge_uf database table.
 * 
 */
@Entity
@Table(name="ibge_uf")
@NamedQuery(name="IbgeUf.findAll", query="SELECT i FROM IbgeUf i")
public class IbgeUf implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String abr;
	private String ddd;
	private String name;
	private Integer country;

	public IbgeUf() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getAbr() {
		return this.abr;
	}

	public void setAbr(String abr) {
		this.abr = abr;
	}


	public String getDdd() {
		return this.ddd;
	}

	public void setDdd(String ddd) {
		this.ddd = ddd;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}





	public Integer getCountry() {
		return this.country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

}