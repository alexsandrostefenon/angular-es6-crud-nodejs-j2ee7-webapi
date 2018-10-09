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
import javax.validation.constraints.NotNull;

import org.domain.crud.entity.CompanyIdPK;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "request_product")
public class RequestProduct implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5749645992706339825L;
	@Id private Integer company;
	private Integer request;
	@Id
	@SequenceGenerator(name="request_product_id_seq", sequenceName="request_product_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="request_product_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@NotNull
	private Integer product;
	@Column(name = "quantity", nullable = false, precision = 9, scale = 3)
	private BigDecimal quantity;
	@Column(name = "value", nullable = false, precision = 9, scale = 3)
	private BigDecimal value;
	@Column(name = "value_item", nullable = false, precision = 9, scale = 3)
	private BigDecimal valueItem;
	@Column(name = "value_desc", columnDefinition = "numeric(9,2)")//][Required][FilterUIHint("", "", "defaultValue", "0.0")]
	private BigDecimal valueDesc;
	@Column(name = "value_freight", columnDefinition = "numeric(9,2)")//][Required][FilterUIHint("", "", "defaultValue", "0.0")]
	private BigDecimal valueFreight;
	@Column(name = "cfop")//][ForeignKey("NfeCfop")]
	private Integer cfop;
	@Column(name = "tax")//][ForeignKey("NfeTaxGroup")]
	private Integer tax;
	@Column(name = "value_all_tax", columnDefinition = "numeric(9,2)")//][Required][FilterUIHint("", "", "defaultValue", "0.0")][Editable(false)]
	private BigDecimal valueAllTax;
	@Column(name = "serials", length = 64)
	private String serial;
	
	public Integer getCompany() {
		return company;
	}
	public void setCompany(Integer company) {
		this.company = company;
	}
	public Integer getRequest() {
		return request;
	}
	public void setRequest(Integer request) {
		this.request = request;
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
	public BigDecimal getQuantity() {
		return quantity;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
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
	public BigDecimal getValueDesc() {
		return valueDesc;
	}
	public void setValueDesc(BigDecimal valueDesc) {
		this.valueDesc = valueDesc;
	}
	public BigDecimal getValueFreight() {
		return valueFreight;
	}
	public void setValueFreight(BigDecimal valueFreight) {
		this.valueFreight = valueFreight;
	}
	public Integer getCfop() {
		return cfop;
	}
	public void setCfop(Integer cfop) {
		this.cfop = cfop;
	}
	public Integer getTax() {
		return tax;
	}
	public void setTax(Integer tax) {
		this.tax = tax;
	}
	public BigDecimal getValueAllTax() {
		return valueAllTax;
	}
	public void setValueAllTax(BigDecimal valueAllTax) {
		this.valueAllTax = valueAllTax;
	}
	public String getSerial() {
		return serial;
	}
	public void setSerial(String serial) {
		this.serial = serial;
	}
}
