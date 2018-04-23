package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the ibge_cnae database table.
 * 
 */
@Entity
@Table(name="ibge_cnae")
@NamedQuery(name="IbgeCnae.findAll", query="SELECT i FROM IbgeCnae i")
public class IbgeCnae implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public IbgeCnae() {
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

}