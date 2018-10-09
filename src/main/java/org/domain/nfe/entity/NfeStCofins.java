package org.domain.nfe.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_st_cofins database table.
 * 
 */
@Entity
@Table(name="nfe_st_cofins")
public class NfeStCofins implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public NfeStCofins() {
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