package org.domain.badmt.entity;
// Generated 08/10/2015 20:06:22 by Hibernate Tools 4.3.1

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.domain.erp.entity.CompanyRequestPK;

@IdClass(CompanyRequestPK.class)
@Entity
@Table(name = "request_repair", uniqueConstraints = @UniqueConstraint(columnNames = "request"))
@XmlRootElement
public class RequestRepair implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3143007962610225361L;
	@Id private Integer company;
	private Integer product;
	private String serial;
	private String defect;
	@Column(name = "additional_data")
	private String additionalData;
	private BigDecimal value;
	private String borrowed;
	@Id private Integer request;


	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public RequestRepair() {
	}


	public Integer getProduct() {
		return this.product;
	}

	public void setProduct(Integer product) {
		this.product = product;
	}

	public Integer getRequest() {
		return this.request;
	}

	public void setRequest(Integer request) {
		this.request = request;
	}

	@Column(name = "serial", nullable = false, length = 64)
	public String getSerial() {
		return this.serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Column(name = "defect", nullable = false)
	public String getDefect() {
		return this.defect;
	}

	public void setDefect(String defect) {
		this.defect = defect;
	}

	public String getAdditionalData() {
		return this.additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}


	@Column(name = "value", precision = 9, scale = 3)
	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	@Column(name = "borrowed")
	public String getBorrowed() {
		return this.borrowed;
	}

	public void setBorrowed(String borrowed) {
		this.borrowed = borrowed;
	}

}
