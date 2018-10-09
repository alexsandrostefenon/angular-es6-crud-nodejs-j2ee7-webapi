package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Entity
@Table(name = "iso8583_router_chip_application_identifier")
public class Iso8583RouterChipApplicationIdentifier implements java.io.Serializable
{
   private static final long serialVersionUID = 5672002503180550142L;
   @Id
   @Column(name = "application_identifier_code_tag_9f06")
   @Size(max = 32)
   String applicationIdentifierCodeTag9f06;

   @Column(name = "version_i_tag_9f09")
   private Integer versionITag9f09;

   @Column(name = "version_ii_tag_9f09")
   private Integer versionIITag9f09;

   @Column(name = "version_iii_tag_9f09")
   private Integer versionIIITag9f09;

   private String tags;

   @Column(name = "terminal_capabilities_tag_9f33")
   @Size(min = 6, max = 6)
   private String terminalCapabilitiesTag9f33;

   @Column(name = "terminal_capabilities_aditional_tag_9f40")
   @Size(min = 10, max = 10)
   private String terminalCapabilitiesAditionalTag9f40;

   @Column(name = "terminal_action_code_default")
   @Size(min = 10, max = 10)
   private String terminalActionCodeDefault;

   @Column(name = "terminal_action_code_denial")
   @Size(min = 10, max = 10)
   private String terminalActionCodeDenial;

   @Column(name = "terminal_action_code_online")
   @Size(min = 10, max = 10)
   private String terminalActionCodeOnline;

   @Column(name = "terminal_type_tag_9f35")
   @Min(value = 21)
   @Max(value = 26)
   private Integer terminalTypeTag9f35;

   @Column(name = "target_percentage")
   private Integer targetPercentage;

   @Column(name = "max_target_percentage")
   private Integer maxTargetPercentage;

   @Column(name = "threshold_amount")
   private String thresholdAmount;

   @Column(name = "terminal_floor_limit_tag_9f1b")
   @Size(min = 8, max = 8)
   private String terminalFloorLimitTag9f1b;

   @Column(name = "merchant_category_code_tag_9f15")
   private Integer merchantCategoryCodeTag9f15;

   @Column(name = "transaction_category_code_tag_9f53")
   @Size(min = 2, max = 2)
   private String transactionCategoryCodeTag9f53;

   @Column(name = "transaction_currency_code_tag_5f2a")
   private Integer transactionCurrencyCodeTag5f2a;

   @Column(name = "transaction_currency_exponent_tag_5f36")
   private Integer transactionCurrencyExponentTag5f36;

   @Column(name = "transaction_certificate_data_object_list")
   @Size(min = 40, max = 40)
   private String transactionCertificateDataObjectList;

   @Column(name = "dynamic_data_authentication_data_object_list")
   @Size(min = 40, max = 40)
   private String dynamicDataAuthenticationDataObjectList;
   // 01 - credit, 02 - debit, 03 - voucher, no TEF é Tipo de Aplicacao
   private Integer product;
   @Column(name = "label")
   @Size(min = 24, max = 24)
   private String tefLabel;
   @Column(name = "terminal_country_code_tag_9f1A")
   private Integer terminalCountryCodeTag9f1A;
   @Column(name = "terminal_reference_currency_code_tag_9f3C")
   private Integer terminalReferenceCurrencyCodeTag9f3C;
   @Column(name = "response_code_offline_aproved")
   @Size(min = 2, max = 2)
   private String responseCodeOfflineAproved;
   @Column(name = "response_code_offline_declined")
   @Size(min = 2, max = 2)
   private String responseCodeOfflineDeclined;
   @Column(name = "response_code_online_approved")
   @Size(min = 2, max = 2)
   private String responseCodeOnlineApproved;
   @Column(name = "response_code_online_declined")
   @Size(min = 2, max = 2)
   private String responseCodeOnlineDeclined;

   public Iso8583RouterChipApplicationIdentifier() {
	      this.versionITag9f09 = 1;
	      this.versionIITag9f09 = 1;
	      this.versionIIITag9f09 = 1;
	      this.tags = "5F349F029F039F1A955F2A9A9C9F37829F369F279F109F269F33";
	      this.terminalCapabilitiesTag9f33 = "E0F8C8";
	      this.terminalCapabilitiesAditionalTag9f40 = "F000F0A001";
	      this.terminalActionCodeDefault = "FC78BC8800";
	      this.terminalActionCodeDenial = "0000000000";
	      this.terminalActionCodeOnline = "FC78BC8800";
	      this.terminalTypeTag9f35 = 22;
	      this.targetPercentage = 0;
	      this.maxTargetPercentage = 0;
	      this.thresholdAmount = "00000000";
	      this.terminalFloorLimitTag9f1b = "00000000";
	      this.merchantCategoryCodeTag9f15 = 0;
	      this.transactionCategoryCodeTag9f53 = "52";
	      this.transactionCertificateDataObjectList = "0000000000000000000000000000000000000000";
	      this.dynamicDataAuthenticationDataObjectList = "0000000000000000000000000000000000000000";
	      this.product = 2;
   }

public String getApplicationIdentifierCodeTag9f06()
   {
      return applicationIdentifierCodeTag9f06;
   }

   public void setApplicationIdentifierCodeTag9f06(
         String applicationIdentifierCodeTag9f06)
   {
      this.applicationIdentifierCodeTag9f06 = applicationIdentifierCodeTag9f06;
   }

   public Integer getVersionITag9f09()
   {
      return versionITag9f09;
   }

   public void setVersionITag9f09(Integer versionITag9f09)
   {
      this.versionITag9f09 = versionITag9f09;
   }

   public Integer getVersionIITag9f09()
   {
      return versionIITag9f09;
   }

   public void setVersionIITag9f09(Integer versionIITag9f09)
   {
      this.versionIITag9f09 = versionIITag9f09;
   }

   public Integer getVersionIIITag9f09()
   {
      return versionIIITag9f09;
   }

   public void setVersionIIITag9f09(Integer versionIIITag9f09)
   {
      this.versionIIITag9f09 = versionIIITag9f09;
   }

   public String getTags()
   {
      return tags;
   }

   public void setTags(String tags)
   {
      this.tags = tags;
   }

   public String getTerminalCapabilitiesTag9f33()
   {
      return terminalCapabilitiesTag9f33;
   }

   public void setTerminalCapabilitiesTag9f33(String terminalCapabilitiesTag9f33)
   {
      this.terminalCapabilitiesTag9f33 = terminalCapabilitiesTag9f33;
   }

   public String getTerminalCapabilitiesAditionalTag9f40()
   {
      return terminalCapabilitiesAditionalTag9f40;
   }

   public void setTerminalCapabilitiesAditionalTag9f40(
         String terminalCapabilitiesAditionalTag9f40)
   {
      this.terminalCapabilitiesAditionalTag9f40 = terminalCapabilitiesAditionalTag9f40;
   }

   public String getTerminalActionCodeDefault()
   {
      return terminalActionCodeDefault;
   }

   public void setTerminalActionCodeDefault(String terminalActionCodeDefault)
   {
      this.terminalActionCodeDefault = terminalActionCodeDefault;
   }

   public String getTerminalActionCodeDenial()
   {
      return terminalActionCodeDenial;
   }

   public void setTerminalActionCodeDenial(String terminalActionCodeDenial)
   {
      this.terminalActionCodeDenial = terminalActionCodeDenial;
   }

   public String getTerminalActionCodeOnline()
   {
      return terminalActionCodeOnline;
   }

   public void setTerminalActionCodeOnline(String terminalActionCodeOnline)
   {
      this.terminalActionCodeOnline = terminalActionCodeOnline;
   }

   public Integer getTerminalTypeTag9f35()
   {
      return terminalTypeTag9f35;
   }

   public void setTerminalTypeTag9f35(Integer terminalTypeTag9f35)
   {
      this.terminalTypeTag9f35 = terminalTypeTag9f35;
   }

   public Integer getTargetPercentage()
   {
      return targetPercentage;
   }

   public void setTargetPercentage(Integer targetPercentage)
   {
      this.targetPercentage = targetPercentage;
   }

   public Integer getMaxTargetPercentage()
   {
      return maxTargetPercentage;
   }

   public void setMaxTargetPercentage(Integer maxTargetPercentage)
   {
      this.maxTargetPercentage = maxTargetPercentage;
   }

   public String getThresholdAmount()
   {
      return thresholdAmount;
   }

   public void setThresholdAmount(String thresholdAmount)
   {
      this.thresholdAmount = thresholdAmount;
   }

   public String getTerminalFloorLimitTag9f1b()
   {
      return terminalFloorLimitTag9f1b;
   }

   public void setTerminalFloorLimitTag9f1b(String terminalFloorLimitTag9f1b)
   {
      this.terminalFloorLimitTag9f1b = terminalFloorLimitTag9f1b;
   }

   public Integer getMerchantCategoryCodeTag9f15()
   {
      return merchantCategoryCodeTag9f15;
   }

   public void setMerchantCategoryCodeTag9f15(Integer merchantCategoryCodeTag9f15)
   {
      this.merchantCategoryCodeTag9f15 = merchantCategoryCodeTag9f15;
   }

   public String getTransactionCategoryCodeTag9f53()
   {
      return transactionCategoryCodeTag9f53;
   }

   public void setTransactionCategoryCodeTag9f53(
         String transactionCategoryCodeTag9f53)
   {
      this.transactionCategoryCodeTag9f53 = transactionCategoryCodeTag9f53;
   }

   public Integer getTransactionCurrencyCodeTag5f2a()
   {
      return transactionCurrencyCodeTag5f2a;
   }

   public void setTransactionCurrencyCodeTag5f2a(
         Integer transactionCurrencyCodeTag5f2a)
   {
      this.transactionCurrencyCodeTag5f2a = transactionCurrencyCodeTag5f2a;
   }

   public Integer getTransactionCurrencyExponentTag5f36()
   {
      return transactionCurrencyExponentTag5f36;
   }

   public void setTransactionCurrencyExponentTag5f36(
         Integer transactionCurrencyExponentTag5f36)
   {
      this.transactionCurrencyExponentTag5f36 = transactionCurrencyExponentTag5f36;
   }

   public String getTransactionCertificateDataObjectList()
   {
      return transactionCertificateDataObjectList;
   }

   public void setTransactionCertificateDataObjectList(
         String transactionCertificateDataObjectList)
   {
      this.transactionCertificateDataObjectList = transactionCertificateDataObjectList;
   }

   public String getDynamicDataAuthenticationDataObjectList()
   {
      return dynamicDataAuthenticationDataObjectList;
   }

   public void setDynamicDataAuthenticationDataObjectList(
         String dynamicDataAuthenticationDataObjectList)
   {
      this.dynamicDataAuthenticationDataObjectList = dynamicDataAuthenticationDataObjectList;
   }

   public Integer getProduct()
   {
      return product;
   }

   public void setProduct(Integer product)
   {
      this.product = product;
   }

   public String getTefLabel()
   {
      return tefLabel;
   }

   public void setTefLabel(String tefLabel)
   {
      this.tefLabel = tefLabel;
   }

   public Integer getTerminalCountryCodeTag9f1A()
   {
      return terminalCountryCodeTag9f1A;
   }

   public void setTerminalCountryCodeTag9f1A(Integer terminalCountryCodeTag9f1A)
   {
      this.terminalCountryCodeTag9f1A = terminalCountryCodeTag9f1A;
   }

   public Integer getTerminalReferenceCurrencyCodeTag9f3C()
   {
      return terminalReferenceCurrencyCodeTag9f3C;
   }

   public void setTerminalReferenceCurrencyCodeTag9f3C(
         Integer terminalReferenceCurrencyCodeTag9f3C)
   {
      this.terminalReferenceCurrencyCodeTag9f3C = terminalReferenceCurrencyCodeTag9f3C;
   }

   public String getResponseCodeOfflineAproved()
   {
      return responseCodeOfflineAproved;
   }

   public void setResponseCodeOfflineAproved(String responseCodeOfflineAproved)
   {
      this.responseCodeOfflineAproved = responseCodeOfflineAproved;
   }

   public String getResponseCodeOfflineDeclined()
   {
      return responseCodeOfflineDeclined;
   }

   public void setResponseCodeOfflineDeclined(String responseCodeOfflineDeclined)
   {
      this.responseCodeOfflineDeclined = responseCodeOfflineDeclined;
   }

   public String getResponseCodeOnlineApproved()
   {
      return responseCodeOnlineApproved;
   }

   public void setResponseCodeOnlineApproved(String responseCodeOnlineApproved)
   {
      this.responseCodeOnlineApproved = responseCodeOnlineApproved;
   }

   public String getResponseCodeOnlineDeclined()
   {
      return responseCodeOnlineDeclined;
   }

   public void setResponseCodeOnlineDeclined(String responseCodeOnlineDeclined)
   {
      this.responseCodeOnlineDeclined = responseCodeOnlineDeclined;
   }

}
