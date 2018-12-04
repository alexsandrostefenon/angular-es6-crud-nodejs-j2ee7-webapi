package org.domain.erp.entity;

import org.domain.erp.entity.RequestNfePK;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * The persistent class for the request_nfe database table.
 * 
 */
@Entity
@Table(name="request_nfe")
@NamedQuery(name="RequestNfe.findAll", query="SELECT r FROM RequestNfe r")
public class RequestNfe implements Serializable {
	private static final long serialVersionUID = 1L;
	@EmbeddedId
	private RequestNfePK id;
	private Integer cdv;
	private Timestamp dhemi;
	private Timestamp dhsaient;
	private Integer finnfe;
	private Integer iddest;
	private Integer indfinal;
	private Integer indiedest;
	private Integer indpag;
	private Integer indpres;
	private Integer mod;
	private String natop;
	@Column(name="nfe_id", columnDefinition="bpchar(47)", length=47)
	private String nfeId;
	private Integer nnf;
	private Integer person;
	private Integer procemi;
	private Integer serie;
	private Integer tpamb;
	private Integer tpemis;
	private Integer tpimp;
	private Integer tpnf;
	@Column(name="value_cofins")
	private BigDecimal valueCofins;
	@Column(name="value_icms")
	private BigDecimal valueIcms;
	@Column(name="value_icms_st")
	private BigDecimal valueIcmsSt;
	@Column(name="value_ii")
	private BigDecimal valueIi;
	@Column(name="value_ipi")
	private BigDecimal valueIpi;
	@Column(name="value_issqn")
	private BigDecimal valueIssqn;
	@Column(name="value_pis")
	private BigDecimal valuePis;
	@Column(name="value_tax")
	private BigDecimal valueTax;
	private String verproc;
	private String versao;

	public RequestNfe() {
	}

	public RequestNfePK getId() {
		return this.id;
	}

	public void setId(RequestNfePK id) {
		this.id = id;
	}


	public Integer getCdv() {
		return this.cdv;
	}

	public void setCdv(Integer cdv) {
		this.cdv = cdv;
	}


	public Timestamp getDhemi() {
		return this.dhemi;
	}

	public void setDhemi(Timestamp dhemi) {
		this.dhemi = dhemi;
	}


	public Timestamp getDhsaient() {
		return this.dhsaient;
	}

	public void setDhsaient(Timestamp dhsaient) {
		this.dhsaient = dhsaient;
	}


	public Integer getFinnfe() {
		return this.finnfe;
	}

	public void setFinnfe(Integer finnfe) {
		this.finnfe = finnfe;
	}


	public Integer getIddest() {
		return this.iddest;
	}

	public void setIddest(Integer iddest) {
		this.iddest = iddest;
	}


	public Integer getIndfinal() {
		return this.indfinal;
	}

	public void setIndfinal(Integer indfinal) {
		this.indfinal = indfinal;
	}


	public Integer getIndiedest() {
		return this.indiedest;
	}

	public void setIndiedest(Integer indiedest) {
		this.indiedest = indiedest;
	}


	public Integer getIndpag() {
		return this.indpag;
	}

	public void setIndpag(Integer indpag) {
		this.indpag = indpag;
	}


	public Integer getIndpres() {
		return this.indpres;
	}

	public void setIndpres(Integer indpres) {
		this.indpres = indpres;
	}


	public Integer getMod() {
		return this.mod;
	}

	public void setMod(Integer mod) {
		this.mod = mod;
	}


	public String getNatop() {
		return this.natop;
	}

	public void setNatop(String natop) {
		this.natop = natop;
	}


	public String getNfeId() {
		return this.nfeId;
	}

	public void setNfeId(String nfeId) {
		this.nfeId = nfeId;
	}


	public Integer getNnf() {
		return this.nnf;
	}

	public void setNnf(Integer nnf) {
		this.nnf = nnf;
	}


	public Integer getPerson() {
		return this.person;
	}

	public void setPerson(Integer person) {
		this.person = person;
	}


	public Integer getProcemi() {
		return this.procemi;
	}

	public void setProcemi(Integer procemi) {
		this.procemi = procemi;
	}


	public Integer getSerie() {
		return this.serie;
	}

	public void setSerie(Integer serie) {
		this.serie = serie;
	}


	public Integer getTpamb() {
		return this.tpamb;
	}

	public void setTpamb(Integer tpamb) {
		this.tpamb = tpamb;
	}


	public Integer getTpemis() {
		return this.tpemis;
	}

	public void setTpemis(Integer tpemis) {
		this.tpemis = tpemis;
	}


	public Integer getTpimp() {
		return this.tpimp;
	}

	public void setTpimp(Integer tpimp) {
		this.tpimp = tpimp;
	}


	public Integer getTpnf() {
		return this.tpnf;
	}

	public void setTpnf(Integer tpnf) {
		this.tpnf = tpnf;
	}


	public BigDecimal getValueCofins() {
		return this.valueCofins;
	}

	public void setValueCofins(BigDecimal valueCofins) {
		this.valueCofins = valueCofins;
	}


	public BigDecimal getValueIcms() {
		return this.valueIcms;
	}

	public void setValueIcms(BigDecimal valueIcms) {
		this.valueIcms = valueIcms;
	}


	public BigDecimal getValueIcmsSt() {
		return this.valueIcmsSt;
	}

	public void setValueIcmsSt(BigDecimal valueIcmsSt) {
		this.valueIcmsSt = valueIcmsSt;
	}


	public BigDecimal getValueIi() {
		return this.valueIi;
	}

	public void setValueIi(BigDecimal valueIi) {
		this.valueIi = valueIi;
	}


	public BigDecimal getValueIpi() {
		return this.valueIpi;
	}

	public void setValueIpi(BigDecimal valueIpi) {
		this.valueIpi = valueIpi;
	}


	public BigDecimal getValueIssqn() {
		return this.valueIssqn;
	}

	public void setValueIssqn(BigDecimal valueIssqn) {
		this.valueIssqn = valueIssqn;
	}


	public BigDecimal getValuePis() {
		return this.valuePis;
	}

	public void setValuePis(BigDecimal valuePis) {
		this.valuePis = valuePis;
	}


	public BigDecimal getValueTax() {
		return this.valueTax;
	}

	public void setValueTax(BigDecimal valueTax) {
		this.valueTax = valueTax;
	}


	public String getVerproc() {
		return this.verproc;
	}

	public void setVerproc(String verproc) {
		this.verproc = verproc;
	}


	public String getVersao() {
		return this.versao;
	}

	public void setVersao(String versao) {
		this.versao = versao;
	}

}