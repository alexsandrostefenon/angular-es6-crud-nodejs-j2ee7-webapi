package org.domain.badmt.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "service")
public class Service {

	@Id
	private Integer id;
	private Integer category;
	private String name;
	private String description;
	private String unit;
	@Column(name = "tax_iss", precision = 9, scale = 3)
	private BigDecimal taxIss;
	@Column(name = "additional_data")
	private String additionalData;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCategory() {
		return this.category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getTaxIss() {
		return this.taxIss;
	}

	public void setTaxIss(BigDecimal taxIss) {
		this.taxIss = taxIss;
	}

	public String getAdditionalData() {
		return this.additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

}
