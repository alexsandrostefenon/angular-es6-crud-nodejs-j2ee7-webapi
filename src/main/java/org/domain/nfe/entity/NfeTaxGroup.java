package org.domain.nfe.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the nfe_tax_group database table.
 * 
 */
@Entity
@Table(name="nfe_tax_group")
@NamedQuery(name="NfeTaxGroup.findAll", query="SELECT n FROM NfeTaxGroup n")
public class NfeTaxGroup implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private BigDecimal taxCofins;
	private BigDecimal taxIcms;
	private BigDecimal taxIpi;
	private BigDecimal taxIssqn;
	private BigDecimal taxPis;
	private BigDecimal taxSimples;
	private Integer cstCofins;
	private Integer cstIcms;
	private Integer cstIpi;
	private Integer cstPis;

	public NfeTaxGroup() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Column(name="tax_cofins")
	public BigDecimal getTaxCofins() {
		return this.taxCofins;
	}

	public void setTaxCofins(BigDecimal taxCofins) {
		this.taxCofins = taxCofins;
	}


	@Column(name="tax_icms")
	public BigDecimal getTaxIcms() {
		return this.taxIcms;
	}

	public void setTaxIcms(BigDecimal taxIcms) {
		this.taxIcms = taxIcms;
	}


	@Column(name="tax_ipi")
	public BigDecimal getTaxIpi() {
		return this.taxIpi;
	}

	public void setTaxIpi(BigDecimal taxIpi) {
		this.taxIpi = taxIpi;
	}


	@Column(name="tax_issqn")
	public BigDecimal getTaxIssqn() {
		return this.taxIssqn;
	}

	public void setTaxIssqn(BigDecimal taxIssqn) {
		this.taxIssqn = taxIssqn;
	}


	@Column(name="tax_pis")
	public BigDecimal getTaxPis() {
		return this.taxPis;
	}

	public void setTaxPis(BigDecimal taxPis) {
		this.taxPis = taxPis;
	}


	@Column(name="tax_simples")
	public BigDecimal getTaxSimples() {
		return this.taxSimples;
	}

	public void setTaxSimples(BigDecimal taxSimples) {
		this.taxSimples = taxSimples;
	}


	@Column(name="cst_cofins")
	public Integer getCstCofins() {
		return this.cstCofins;
	}

	public void setCstCofins(Integer nfeStCofin) {
		this.cstCofins = nfeStCofin;
	}


	@Column(name="cst_icms")
	public Integer getCstIcms() {
		return this.cstIcms;
	}

	public void setCstIcms(Integer nfeStIcm) {
		this.cstIcms = nfeStIcm;
	}


	@Column(name="cst_ipi")
	public Integer getCstIpi() {
		return this.cstIpi;
	}

	public void setCstIpi(Integer nfeStIpi) {
		this.cstIpi = nfeStIpi;
	}


	@Column(name="cst_pis")
	public Integer getCstPis() {
		return this.cstPis;
	}

	public void setCstPis(Integer nfeStPi) {
		this.cstPis = nfeStPi;
	}

}