package org.domain.nfe.entity;
// Generated 08/10/2015 20:06:22 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * TaxOpcode generated by hbm2java
 */
@Entity
@Table(name = "payment_type", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PaymentType implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1864551567293374642L;
	@Id
	private Integer id;
	private String name;
	private String description;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "name", unique = true, nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", length = 100)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
