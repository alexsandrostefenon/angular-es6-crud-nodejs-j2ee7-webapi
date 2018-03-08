package org.domain.crud.entity;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "request_product", uniqueConstraints = @UniqueConstraint(columnNames = {
		"company", "request", "product", "serial"}))
public class RequestProduct implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5749645992706339825L;
	@Id private Integer company;
	@Id
	@SequenceGenerator(name="request_product_id_seq", sequenceName="request_product_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="request_product_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@NotNull
	private Integer product;
	private Integer request;
	@Column(name = "serial", length = 64)
	private String serial;
	@Column(name = "quantity", nullable = false, precision = 9, scale = 3)
	private BigDecimal quantity;
	@Column(name = "weight", precision = 9, scale = 3)
	private BigDecimal weight;
	private Integer containers;
	@Column(name = "weight_final", precision = 9, scale = 3)
	private BigDecimal weightFinal;
	@Column(name = "space", precision = 9, scale = 3)
	private BigDecimal space;
	@Column(name = "value", nullable = false, precision = 9, scale = 3)
	private BigDecimal value;
	@Column(name = "value_item", nullable = false, precision = 9, scale = 3)
	private BigDecimal valueItem;
	@Column(name = "tax_ipi", precision = 9, scale = 3)
	private BigDecimal taxIpi = new BigDecimal(0.0);
	@Column(name = "tax_icms", precision = 9, scale = 3)
	private BigDecimal taxIcms = new BigDecimal(0.0);
	@Column(name = "tax_upper_lower", precision = 9, scale = 3)
	private BigDecimal taxUpperLower = new BigDecimal(1.0);

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
	public Integer getProduct() {
		return product;
	}
	public void setProduct(Integer product) {
		this.product = product;
	}
	public Integer getRequest() {
		return request;
	}
	public void setRequest(Integer request) {
		this.request = request;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
	public BigDecimal getQuantity() {
		return quantity;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
	public Integer getContainers() {
		return containers;
	}
	public void setContainers(Integer containers) {
		this.containers = containers;
	}
	public BigDecimal getWeightFinal() {
		return weightFinal;
	}
	public void setWeightFinal(BigDecimal weightFinal) {
		this.weightFinal = weightFinal;
	}
	public BigDecimal getSpace() {
		return space;
	}
	public void setSpace(BigDecimal space) {
		this.space = space;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public BigDecimal getValueItem() {
		return valueItem;
	}
	public void setValueItem(BigDecimal valueItem) {
		this.valueItem = valueItem;
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
	public BigDecimal getTaxUpperLower() {
		return taxUpperLower;
	}
	public void setTaxUpperLower(BigDecimal taxUpperLower) {
		this.taxUpperLower = taxUpperLower;
	}

}
