package org.domain.erp.entity;
// Generated 08/10/2015 20:06:22 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.domain.crud.entity.CrudGroupOwnerIdPK;

@IdClass(CrudGroupOwnerIdPK.class)
@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(columnNames = {"bank", "agency", "number"}))
public class Account implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5284750958346901988L;
	
	@Id
	@Column(name="crud_group_owner")
	private Integer crudGroupOwner;
	
	@Id
	@SequenceGenerator(name="account_id_seq", sequenceName="account_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="account_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	
	private String bank;
	private String agency;
	private String number;
	private String description;

	public Account() {
	}

	public Account(int id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "bank", length = 20)
	public String getBank() {
		return this.bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	@Column(name = "agency", length = 20)
	public String getAgency() {
		return this.agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	@Column(name = "number", length = 20)
	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}

}
