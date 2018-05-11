package org.domain.erp.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the product database table.
 * 
 */
@Entity
@NamedQuery(name="Product.findAll", query="SELECT p FROM Product p")
public class Product implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name="product_id_seq", sequenceName="product_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="product_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@Column(name="additional_data")
	private String additionalData;
	private Integer category;
	private String departament;
	private String description;
	@Column(name="image_url")
	private String imageUrl;
	private String model;
	private String name;
	private Integer ncm;
	private Integer orig;
	private BigDecimal weight;

	public Product() {
	}


	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getAdditionalData() {
		return this.additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}


	public Integer getCategory() {
		return this.category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}


	public String getDepartament() {
		return this.departament;
	}

	public void setDepartament(String departament) {
		this.departament = departament;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Integer getNcm() {
		return this.ncm;
	}

	public void setNcm(Integer ncm) {
		this.ncm = ncm;
	}


	public Integer getOrig() {
		return this.orig;
	}

	public void setOrig(Integer orig) {
		this.orig = orig;
	}


	public BigDecimal getWeight() {
		return this.weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

}