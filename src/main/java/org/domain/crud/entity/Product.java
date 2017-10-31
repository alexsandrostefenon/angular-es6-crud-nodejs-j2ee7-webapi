package org.domain.crud.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "product", uniqueConstraints = {
		@UniqueConstraint(columnNames = "barcode"),
		@UniqueConstraint(columnNames = {"name", "manufacturer", "model", "description"})})
public class Product {

	@Id
	@Column(name = "id", columnDefinition = "serial")
	private Integer id;
	@Column(name = "category", nullable = false)
	private Integer category;
	@Column(name = "barcode", unique = true, length = 13)
	private String barcode;
	@Column(name = "name", nullable = false, length = 100)
	private String name;
	@Column(name = "manufacturer", length = 64)
	private String manufacturer;
	@Column(name = "model", length = 255)
	private String model;
	@Column(name = "description")
	private String description;
	@Column(name = "additional_data")
	private String additionalData;
	@Column(name = "unit", length = 16)
	private String unit;
	@Column(name = "cl_fiscal", length = 16)
	private String clFiscal;
	@Column(name = "departament", length = 64)
	private String departament;
	@Column(name = "image_url")
	private String imageUrl;
	@Column(name = "weight", precision = 9, scale = 3)
	private BigDecimal weight;
	@Column(name = "tax_ipi", precision = 9, scale = 3)
	private BigDecimal taxIpi;
	@Column(name = "tax_icms", precision = 9, scale = 3)
	private BigDecimal taxIcms;
	@Column(name = "tax_iss", precision = 9, scale = 3)
	private BigDecimal taxIss;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getCategory() {
		return category;
	}
	public void setCategory(Integer category) {
		this.category = category;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAdditionalData() {
		return additionalData;
	}
	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getClFiscal() {
		return clFiscal;
	}
	public void setClFiscal(String clFiscal) {
		this.clFiscal = clFiscal;
	}
	public String getDepartament() {
		return departament;
	}
	public void setDepartament(String departament) {
		this.departament = departament;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	public BigDecimal getTaxIpi() {
		return taxIpi;
	}
	public void setTaxIpi(BigDecimal taxIpi) {
		this.taxIpi = taxIpi;
	}
	public BigDecimal getTaxIcms() {
		return taxIcms;
	}
	public void setTaxIcms(BigDecimal taxIcms) {
		this.taxIcms = taxIcms;
	}
	public BigDecimal getTaxIss() {
		return taxIss;
	}
	public void setTaxIss(BigDecimal taxIss) {
		this.taxIss = taxIss;
	}

}
