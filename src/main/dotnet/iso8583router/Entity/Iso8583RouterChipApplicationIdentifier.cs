using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_chip_application_identifier")]
    public partial class Iso8583RouterChipApplicationIdentifier
    {
        [Key][Column("application_identifier_code_tag_9f06", TypeName = "character varying(32)")]
        public string ApplicationIdentifierCodeTag9f06 { get; set; }
        [Column("dynamic_data_authentication_data_object_list", TypeName = "character varying(40)")]
        public string DynamicDataAuthenticationDataObjectList { get; set; }
        [Column("max_target_percentage")]
        public int? MaxTargetPercentage { get; set; }
        [Column("merchant_category_code_tag_9f15")]
        public int? MerchantCategoryCodeTag9f15 { get; set; }
        [Column("product")]
        public int? Product { get; set; }
        [Column("response_code_offline_aproved", TypeName = "character varying(2)")]
        public string ResponseCodeOfflineAproved { get; set; }
        [Column("response_code_offline_declined", TypeName = "character varying(2)")]
        public string ResponseCodeOfflineDeclined { get; set; }
        [Column("response_code_online_approved", TypeName = "character varying(2)")]
        public string ResponseCodeOnlineApproved { get; set; }
        [Column("response_code_online_declined", TypeName = "character varying(2)")]
        public string ResponseCodeOnlineDeclined { get; set; }
        [Column("tags", TypeName = "character varying(255)")]
        public string Tags { get; set; }
        [Column("target_percentage")]
        public int? TargetPercentage { get; set; }
        [Column("label", TypeName = "character varying(24)")]
        public string Label { get; set; }
        [Column("terminal_action_code_default", TypeName = "character varying(10)")]
        public string TerminalActionCodeDefault { get; set; }
        [Column("terminal_action_code_denial", TypeName = "character varying(10)")]
        public string TerminalActionCodeDenial { get; set; }
        [Column("terminal_action_code_online", TypeName = "character varying(10)")]
        public string TerminalActionCodeOnline { get; set; }
        [Column("terminal_capabilities_aditional_tag_9f40", TypeName = "character varying(10)")]
        public string TerminalCapabilitiesAditionalTag9f40 { get; set; }
        [Column("terminal_capabilities_tag_9f33", TypeName = "character varying(6)")]
        public string TerminalCapabilitiesTag9f33 { get; set; }
        [Column("terminal_country_code_tag_9f1a")]
        public int? TerminalCountryCodeTag9f1a { get; set; }
        [Column("terminal_floor_limit_tag_9f1b", TypeName = "character varying(8)")]
        public string TerminalFloorLimitTag9f1b { get; set; }
        [Column("terminal_reference_currency_code_tag_9f3c")]
        public int? TerminalReferenceCurrencyCodeTag9f3c { get; set; }
        [Column("terminal_type_tag_9f35")]
        public int? TerminalTypeTag9f35 { get; set; }
        [Column("threshold_amount", TypeName = "character varying(255)")]
        public string ThresholdAmount { get; set; }
        [Column("transaction_category_code_tag_9f53", TypeName = "character varying(2)")]
        public string TransactionCategoryCodeTag9f53 { get; set; }
        [Column("transaction_certificate_data_object_list", TypeName = "character varying(40)")]
        public string TransactionCertificateDataObjectList { get; set; }
        [Column("transaction_currency_code_tag_5f2a")]
        public int? TransactionCurrencyCodeTag5f2a { get; set; }
        [Column("transaction_currency_exponent_tag_5f36")]
        public int? TransactionCurrencyExponentTag5f36 { get; set; }
        [Column("version_iii_tag_9f09")]
        public int? VersionIiiTag9f09 { get; set; }
        [Column("version_ii_tag_9f09")]
        public int? VersionIiTag9f09 { get; set; }
        [Column("version_i_tag_9f09")]
        public int? VersionITag9f09 { get; set; }
    }
}
