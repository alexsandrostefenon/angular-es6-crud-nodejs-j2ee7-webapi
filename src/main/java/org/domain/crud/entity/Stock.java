package org.domain.crud.entity;
// Generated 08/10/2015 20:06:22 by Hibernate Tools 4.3.1

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "stock")
@XmlRootElement
public class Stock implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8377367856010010641L;
	@Id private Integer company;
	@Id private Integer id; // mesmo número do id do Product

	private BigDecimal countIn;
	private BigDecimal countOut;
	private BigDecimal reservedIn;
	private BigDecimal reservedOut;
	private BigDecimal estimedIn;
	private BigDecimal estimedOut;
	private BigDecimal stock;

	private String stockSerials;

	private BigDecimal sumValueIn;
	private BigDecimal sumValueOut;
	private BigDecimal sumValueStock;
	
	private BigDecimal estimedValue;
	// valores manuais
	private BigDecimal stockMinimal;
	private BigDecimal stockDefault;
	private BigDecimal marginSale;
	private BigDecimal marginWholesale;
	private BigDecimal value;
	private BigDecimal valueWholesale;

	public Stock() {
		this.countIn = new BigDecimal(0.0);
		this.countOut = new BigDecimal(0.0);
		this.reservedIn = new BigDecimal(0.0);
		this.reservedOut = new BigDecimal(0.0);
		this.estimedIn = new BigDecimal(0.0);
		this.estimedOut = new BigDecimal(0.0);
		
		this.stock = new BigDecimal(0.0);
		
		this.stockSerials = "";
		
		this.sumValueIn = new BigDecimal(0.0);
		this.sumValueOut = new BigDecimal(0.0);
		this.sumValueStock = new BigDecimal(0.0);
		
		this.estimedValue = new BigDecimal(0.0);
		// manual values
		this.stockMinimal = new BigDecimal(0.0);
		this.stockDefault = new BigDecimal(0.0);
		this.marginSale = new BigDecimal(0.6);
		this.marginWholesale = new BigDecimal(0.3);
		this.value = new BigDecimal(0.0);
		this.valueWholesale = new BigDecimal(0.0);
	}

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "count_in", precision = 9, scale = 3)
	public BigDecimal getCountIn() {
		return this.countIn;
	}

	public void setCountIn(BigDecimal countIn) {
		this.countIn = countIn;
	}

	@Column(name = "count_out", precision = 9, scale = 3)
	public BigDecimal getCountOut() {
		return this.countOut;
	}

	public void setCountOut(BigDecimal countOut) {
		this.countOut = countOut;
	}

	@Column(name = "stock_default", precision = 9, scale = 3)
	public BigDecimal getStockDefault() {
		return this.stockDefault;
	}

	public void setStockDefault(BigDecimal stockDefault) {
		this.stockDefault = stockDefault;
	}

	@Column(name = "stock_minimal", precision = 9, scale = 3)
	public BigDecimal getStockMinimal() {
		return this.stockMinimal;
	}

	public void setStockMinimal(BigDecimal stockMinimal) {
		this.stockMinimal = stockMinimal;
	}

	@Column(name = "stock", precision = 9, scale = 3)
	public BigDecimal getStock() {
		return this.stock;
	}

	public void setStock(BigDecimal stock) {
		this.stock = stock;
	}

	@Column(name = "stock_serials", length = 1024)
	public String getStockSerials() {
		return this.stockSerials;
	}

	public void setStockSerials(String stockSerials) {
		this.stockSerials = stockSerials;
	}

	@Column(name = "reserved_out", precision = 9, scale = 3)
	public BigDecimal getReservedOut() {
		return this.reservedOut;
	}

	public void setReservedOut(BigDecimal reservedOut) {
		this.reservedOut = reservedOut;
	}

	@Column(name = "reserved_in", precision = 9, scale = 3)
	public BigDecimal getReservedIn() {
		return this.reservedIn;
	}

	public void setReservedIn(BigDecimal reservedIn) {
		this.reservedIn = reservedIn;
	}

	@Column(name = "estimed_in", precision = 9, scale = 3)
	public BigDecimal getEstimedIn() {
		return this.estimedIn;
	}

	public void setEstimedIn(BigDecimal estimedIn) {
		this.estimedIn = estimedIn;
	}

	@Column(name = "estimed_out", precision = 9, scale = 3)
	public BigDecimal getEstimedOut() {
		return this.estimedOut;
	}

	public void setEstimedOut(BigDecimal estimedOut) {
		this.estimedOut = estimedOut;
	}

	@Column(name = "sum_value_in", precision = 9, scale = 3)
	public BigDecimal getSumValueIn() {
		return this.sumValueIn;
	}

	public void setSumValueIn(BigDecimal sumValueIn) {
		this.sumValueIn = sumValueIn;
	}

	@Column(name = "sum_value_out", precision = 9, scale = 3)
	public BigDecimal getSumValueOut() {
		return this.sumValueOut;
	}

	public void setSumValueOut(BigDecimal sumValueOut) {
		this.sumValueOut = sumValueOut;
	}

	@Column(name = "sum_value_stock", precision = 9, scale = 3)
	public BigDecimal getSumValueStock() {
		return this.sumValueStock;
	}

	public void setSumValueStock(BigDecimal sumValueStock) {
		this.sumValueStock = sumValueStock;
	}

	@Column(name = "margin_sale", precision = 9, scale = 3)
	public BigDecimal getMarginSale() {
		return this.marginSale;
	}

	public void setMarginSale(BigDecimal marginSale) {
		this.marginSale = marginSale;
	}

	@Column(name = "margin_wholesale", precision = 9, scale = 3)
	public BigDecimal getMarginWholesale() {
		return this.marginWholesale;
	}

	public void setMarginWholesale(BigDecimal marginWholesale) {
		this.marginWholesale = marginWholesale;
	}

	@Column(name = "estimed_value", precision = 9, scale = 3)
	public BigDecimal getEstimedValue() {
		return this.estimedValue;
	}

	public void setEstimedValue(BigDecimal estimedValue) {
		this.estimedValue = estimedValue;
	}

	@Column(name = "value", precision = 9, scale = 3)
	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	@Column(name = "value_wholesale", precision = 9, scale = 3)
	public BigDecimal getValueWholesale() {
		return this.valueWholesale;
	}

	public void setValueWholesale(BigDecimal valueWholesale) {
		this.valueWholesale = valueWholesale;
	}

}
