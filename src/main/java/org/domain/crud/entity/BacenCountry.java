package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the bacen_country database table.
 * 
 */
@Entity
@Table(name="bacen_country")
@NamedQuery(name="BacenCountry.findAll", query="SELECT b FROM BacenCountry b")
public class BacenCountry implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String abr;
	private String name;
	private String namePt;

	public BacenCountry() {
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


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Column(name="name_pt")
	public String getNamePt() {
		return this.namePt;
	}

	public void setNamePt(String namePt) {
		this.namePt = namePt;
	}



}