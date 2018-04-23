package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_st_icms_desoneracao database table.
 * 
 */
@Entity
@Table(name="nfe_st_icms_desoneracao")
@NamedQuery(name="NfeStIcmsDesoneracao.findAll", query="SELECT n FROM NfeStIcmsDesoneracao n")
public class NfeStIcmsDesoneracao implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public NfeStIcmsDesoneracao() {
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