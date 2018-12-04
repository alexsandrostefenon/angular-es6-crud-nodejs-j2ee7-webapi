package org.domain.erp.entity;
// Generated 08/10/2015 20:06:22 by Hibernate Tools 4.3.1

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.domain.crud.entity.CompanyIdPK;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "stock")
public class Stock implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8377367856010010641L;
	@Id private Integer company;
	@Id private Integer id; // mesmo número do id do Product

	@Column(name = "count_in", precision = 9, scale = 3)
	private BigDecimal countIn;
	@Column(name = "count_out", precision = 9, scale = 3)
	private BigDecimal countOut;
	@Column(name = "reserved_in", precision = 9, scale = 3)
	private BigDecimal reservedIn;
	@Column(name = "reserved_out", precision = 9, scale = 3)
	private BigDecimal reservedOut;
	@Column(name = "estimed_in", precision = 9, scale = 3)
	private BigDecimal estimedIn;
	@Column(name = "estimed_out", precision = 9, scale = 3)
	private BigDecimal estimedOut;
	@Column(name = "stock_value", precision = 9, scale = 3)
	private BigDecimal stockValue;

	@Column(name = "stock_serials", length = 1024)
	private String stockSerials;

	@Column(name = "sum_value_in", precision = 9, scale = 3)
	private BigDecimal sumValueIn;
	@Column(name = "sum_value_out", precision = 9, scale = 3)
	private BigDecimal sumValueOut;
	@Column(name = "sum_value_stock", precision = 9, scale = 3)
	private BigDecimal sumValueStock;

	@Column(name = "estimed_value", precision = 9, scale = 3)
	private BigDecimal estimedValue;
	// valores manuais
	@Column(name = "stock_minimal", precision = 9, scale = 3)
	private BigDecimal stockMinimal;
	@Column(name = "stock_default", precision = 9, scale = 3)
	private BigDecimal stockDefault;
	@Column(name = "margin_sale", precision = 9, scale = 3)
	private BigDecimal marginSale;
	@Column(name = "margin_wholesale", precision = 9, scale = 3)
	private BigDecimal marginWholesale;
	@Column(name = "value", precision = 9, scale = 3)
	private BigDecimal value;
	@Column(name = "value_wholesale", precision = 9, scale = 3)
	private BigDecimal valueWholesale;

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
	public BigDecimal getCountIn() {
		return countIn;
	}
	public void setCountIn(BigDecimal countIn) {
		this.countIn = countIn;
	}
	public BigDecimal getCountOut() {
		return countOut;
	}
	public void setCountOut(BigDecimal countOut) {
		this.countOut = countOut;
	}
	public BigDecimal getReservedIn() {
		return reservedIn;
	}
	public void setReservedIn(BigDecimal reservedIn) {
		this.reservedIn = reservedIn;
	}
	public BigDecimal getReservedOut() {
		return reservedOut;
	}
	public void setReservedOut(BigDecimal reservedOut) {
		this.reservedOut = reservedOut;
	}
	public BigDecimal getEstimedIn() {
		return estimedIn;
	}
	public void setEstimedIn(BigDecimal estimedIn) {
		this.estimedIn = estimedIn;
	}
	public BigDecimal getEstimedOut() {
		return estimedOut;
	}
	public void setEstimedOut(BigDecimal estimedOut) {
		this.estimedOut = estimedOut;
	}
	public BigDecimal getStockValue() {
		return stockValue;
	}
	public void setStockValue(BigDecimal stockValue) {
		this.stockValue = stockValue;
	}
	public String getStockSerials() {
		return stockSerials;
	}
	public void setStockSerials(String stockSerials) {
		this.stockSerials = stockSerials;
	}
	public BigDecimal getSumValueIn() {
		return sumValueIn;
	}
	public void setSumValueIn(BigDecimal sumValueIn) {
		this.sumValueIn = sumValueIn;
	}
	public BigDecimal getSumValueOut() {
		return sumValueOut;
	}
	public void setSumValueOut(BigDecimal sumValueOut) {
		this.sumValueOut = sumValueOut;
	}
	public BigDecimal getSumValueStock() {
		return sumValueStock;
	}
	public void setSumValueStock(BigDecimal sumValueStock) {
		this.sumValueStock = sumValueStock;
	}
	public BigDecimal getEstimedValue() {
		return estimedValue;
	}
	public void setEstimedValue(BigDecimal estimedValue) {
		this.estimedValue = estimedValue;
	}
	public BigDecimal getStockMinimal() {
		return stockMinimal;
	}
	public void setStockMinimal(BigDecimal stockMinimal) {
		this.stockMinimal = stockMinimal;
	}
	public BigDecimal getStockDefault() {
		return stockDefault;
	}
	public void setStockDefault(BigDecimal stockDefault) {
		this.stockDefault = stockDefault;
	}
	public BigDecimal getMarginSale() {
		return marginSale;
	}
	public void setMarginSale(BigDecimal marginSale) {
		this.marginSale = marginSale;
	}
	public BigDecimal getMarginWholesale() {
		return marginWholesale;
	}
	public void setMarginWholesale(BigDecimal marginWholesale) {
		this.marginWholesale = marginWholesale;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public BigDecimal getValueWholesale() {
		return valueWholesale;
	}
	public void setValueWholesale(BigDecimal valueWholesale) {
		this.valueWholesale = valueWholesale;
	}

}
