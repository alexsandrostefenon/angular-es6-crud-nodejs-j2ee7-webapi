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
import javax.xml.bind.annotation.XmlRootElement;

import org.domain.crud.entity.CompanyIdPK;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "account", uniqueConstraints = @UniqueConstraint(columnNames = {"bank", "agency", "account"}))
@XmlRootElement
public class Account implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5284750958346901988L;
	
	@Id
	private Integer company;
	
	@Id
	@SequenceGenerator(name="account_id_seq", sequenceName="account_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="account_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	
	private String bank;
	private String agency;
	private String account;
	private String description;

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public Account() {
	}

	public Account(int id) {
		this.id = id;
	}

	public Account(int id, String bank, String agency, String account,
			String description) {
		this.id = id;
		this.bank = bank;
		this.agency = agency;
		this.account = account;
		this.description = description;
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

	@Column(name = "account", length = 20)
	public String getAccount() {
		return this.account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
