package org.domain.nfe.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_st_ipi database table.
 * 
 */
@Entity
@Table(name="nfe_st_ipi")
@NamedQuery(name="NfeStIpi.findAll", query="SELECT n FROM NfeStIpi n")
public class NfeStIpi implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;

	public NfeStIpi() {
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