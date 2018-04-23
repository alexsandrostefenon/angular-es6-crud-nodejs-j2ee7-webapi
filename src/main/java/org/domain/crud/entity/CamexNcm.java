package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the camex_ncm database table.
 * 
 */
@Entity
@Table(name="camex_ncm")
@NamedQuery(name="CamexNcm.findAll", query="SELECT c FROM CamexNcm c")
public class CamexNcm implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private Integer tec;
	private String unit;

	public CamexNcm() {
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


	public Integer getTec() {
		return this.tec;
	}

	public void setTec(Integer tec) {
		this.tec = tec;
	}


	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

}