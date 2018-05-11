package org.domain.financial2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "iso8583_terminal")
public class ISO8583Terminal implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5041957907400728077L;
	@Id
	 Integer id;
	 String version;
	 @Column(name="help_desk")
	 String helpDesk;
	 String crypt;
	 @Column(name="working_key")
	 String workingkey;
	 @Column(name="index_master_key")
	 String indexMasterkey;
	 Integer flags1;
	 Integer flags2;
	 Integer flags3;
	 String receiptLine1;
	 String receiptLine2;
	 String receiptLine3;
	 String cnpj;
	 @Column(name="limit_remind")
	 String limitRemind;
	 String adquirent;
	 String dte1;
	 String dte2;
	 Integer timeout;
	 @Column(name="transaction_category")
	 String transactionCategory;
	 @Column(name="merchant_type")
	 Integer merchantType;
	 @Column(name="terminal_category")
	 String terminalCategory;
	 @Column(name="country_code")
	 String countryCode;
	 @Column(name="code_money")
	 String codeMoney;
	 @Column(name="frete_country")
		private Integer freteCountry;
		private String gaps1;
		private String gaps2;

		public ISO8583Terminal() {
		}
		
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getHelpDesk() {
		return helpDesk;
	}

	public void setHelpDesk(String helpDesk) {
		this.helpDesk = helpDesk;
	}

	public String getCrypt() {
		return crypt;
	}

	public void setCrypt(String crypt) {
		this.crypt = crypt;
	}

	public String getWorkingkey() {
		return workingkey;
	}

	public void setWorkingkey(String workingkey) {
		this.workingkey = workingkey;
	}

	public String getIndexMasterkey() {
		return indexMasterkey;
	}

	public void setIndexMasterkey(String indexMasterkey) {
		this.indexMasterkey = indexMasterkey;
	}

	public Integer getFlags1() {
		return flags1;
	}

	public void setFlags1(Integer flags1) {
		this.flags1 = flags1;
	}

	public Integer getFlags2() {
		return flags2;
	}

	public void setFlags2(Integer flags2) {
		this.flags2 = flags2;
	}

	public Integer getFlags3() {
		return flags3;
	}

	public void setFlags3(Integer flags3) {
		this.flags3 = flags3;
	}

	public String getReceiptLine1() {
		return receiptLine1;
	}

	public void setReceiptLine1(String receiptLine1) {
		this.receiptLine1 = receiptLine1;
	}

	public String getReceiptLine2() {
		return receiptLine2;
	}

	public void setReceiptLine2(String receiptLine2) {
		this.receiptLine2 = receiptLine2;
	}

	public String getReceiptLine3() {
		return receiptLine3;
	}

	public void setReceiptLine3(String receiptLine3) {
		this.receiptLine3 = receiptLine3;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	public String getLimitRemind() {
		return limitRemind;
	}

	public void setLimitRemind(String limitRemind) {
		this.limitRemind = limitRemind;
	}

	public String getAdquirent() {
		return adquirent;
	}

	public void setAdquirent(String adquirent) {
		this.adquirent = adquirent;
	}

	public String getDte1() {
		return dte1;
	}

	public void setDte1(String dte1) {
		this.dte1 = dte1;
	}

	public String getDte2() {
		return dte2;
	}

	public void setDte2(String dte2) {
		this.dte2 = dte2;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public String getTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(String transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	public Integer getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(Integer merchantType) {
		this.merchantType = merchantType;
	}

	public String getTerminalCategory() {
		return terminalCategory;
	}

	public void setTerminalCategory(String terminalCategory) {
		this.terminalCategory = terminalCategory;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCodeMoney() {
		return codeMoney;
	}

	public void setCodeMoney(String codeMoney) {
		this.codeMoney = codeMoney;
	}


	public Integer getFreteCountry() {
		return freteCountry;
	}
	

	public void setFreteCountry(Integer freteCountry) {
		this.freteCountry = freteCountry;
	}
	

	public String getGaps1() {
		return gaps1;
	}
	

	public void setGaps1(String gaps1) {
		this.gaps1 = gaps1;
	}
	

	public String getGaps2() {
		return gaps2;
	}
	

	public void setGaps2(String gaps2) {
		this.gaps2 = gaps2;
	}

}
