package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_st_icms_modalidade_st database table.
 * 
 */
@Entity
@Table(name="nfe_st_icms_modalidade_st")
@NamedQuery(name="NfeStIcmsModalidadeSt.findAll", query="SELECT n FROM NfeStIcmsModalidadeSt n")
public class NfeStIcmsModalidadeSt implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public NfeStIcmsModalidadeSt() {
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