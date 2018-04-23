package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_cfop database table.
 * 
 */
@Entity
@Table(name="nfe_cfop")
@NamedQuery(name="NfeCfop.findAll", query="SELECT n FROM NfeCfop n")
public class NfeCfop implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private Integer indComunica;
	private Integer indDevol;
	private Integer indNfe;
	private Integer indTransp;
	private String name;

	public NfeCfop() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	@Column(name="ind_comunica")
	public Integer getIndComunica() {
		return this.indComunica;
	}

	public void setIndComunica(Integer indComunica) {
		this.indComunica = indComunica;
	}


	@Column(name="ind_devol")
	public Integer getIndDevol() {
		return this.indDevol;
	}

	public void setIndDevol(Integer indDevol) {
		this.indDevol = indDevol;
	}


	@Column(name="ind_nfe")
	public Integer getIndNfe() {
		return this.indNfe;
	}

	public void setIndNfe(Integer indNfe) {
		this.indNfe = indNfe;
	}


	@Column(name="ind_transp")
	public Integer getIndTransp() {
		return this.indTransp;
	}

	public void setIndTransp(Integer indTransp) {
		this.indTransp = indTransp;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}