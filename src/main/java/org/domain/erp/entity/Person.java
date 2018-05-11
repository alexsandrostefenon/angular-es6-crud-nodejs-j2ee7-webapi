package org.domain.erp.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.domain.crud.entity.CompanyIdPK;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "person")
public class Person implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5971740408816408928L;
	@Id private Integer company;
	@Id
	@SequenceGenerator(name="person_id_seq", sequenceName="person_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="person_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@Column(name = "name", unique = true, nullable = false, length = 100)
	private String name;
	@Column(name = "cnpj_cpf", unique = true, length = 18)
	private String cnpjCpf;
	@Column(name = "ie_rg", unique = true, length = 12)
	private String ieRg;
	@Column(name = "zip", length = 9, columnDefinition="bpchar(9)")
	private String zip;
	private Integer uf;
	private Integer city;
	@Column(name = "district", length = 64)
	private String district;
	@Column(name = "address", length = 100)
	private String address;
	@Column(name = "phone", length = 16)
	private String phone;
	@Column(name = "fax", length = 16)
	private String fax;
	@Column(name = "email", length = 100)
	private String email;
	@Column(name = "site", length = 100)
	private String site;
	@Column(name = "additional_data")
	private String additionalData;
	@Column(name = "credit", precision = 9, scale = 3)
	private BigDecimal credit;

	public Person() {
	}

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCnpjCpf() {
		return cnpjCpf;
	}

	public void setCnpjCpf(String cnpjCpf) {
		this.cnpjCpf = cnpjCpf;
	}

	public String getIeRg() {
		return ieRg;
	}

	public void setIeRg(String ieRg) {
		this.ieRg = ieRg;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public Integer getUf() {
		return uf;
	}

	public void setUf(Integer uf) {
		this.uf = uf;
	}

	public Integer getCity() {
		return city;
	}

	public void setCity(Integer city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	public BigDecimal getCredit() {
		return credit;
	}

	public void setCredit(BigDecimal credit) {
		this.credit = credit;
	}

}
