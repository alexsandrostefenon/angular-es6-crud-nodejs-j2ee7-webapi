using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request_product")]
    public partial class RequestProduct
    {
        [Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
		[Key][Column("request")][ForeignKey("Request")]
        public int Request { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("product")][Required][ForeignKey("Product")]
        public int? Product { get; set; }
		[Column("quantity", TypeName = "numeric(9,3)")][Required][FilterUIHint("", "", "defaultValue", "1.000")]
        public decimal Quantity { get; set; }
		[Column("value", TypeName = "numeric(9,3)")][Required][FilterUIHint("", "", "defaultValue", "0.0")]
        public decimal Value { get; set; }
		[Column("value_item", TypeName = "numeric(9,2)")][Required][Editable(false)][FilterUIHint("", "", "defaultValue", "0.0")]
        public decimal ValueItem { get; set; }
		[Column("value_desc", TypeName = "numeric(9,2)")][Required][FilterUIHint("", "", "defaultValue", "0.0")]
        public decimal ValueDesc { get; set; }
		[Column("value_freight", TypeName = "numeric(9,2)")][Required][FilterUIHint("", "", "defaultValue", "0.0")]
        public decimal? ValueFreight { get; set; }
		[Column("cfop")][ForeignKey("NfeCfop")]
        public int? Cfop { get; set; }
		[Column("tax")][ForeignKey("NfeTaxGroup")]
        public int? Tax { get; set; }
		[Column("value_all_tax", TypeName = "numeric(9,2)")][Required][FilterUIHint("", "", "defaultValue", "0.0")][Editable(false)]
        public decimal ValueAllTax { get; set; }
        [Column("serials", TypeName = "character varying(255)")]
        public string Serials { get; set; }

        //[ForeignKey("Cfop")]
        //[InverseProperty("RequestProduct")]
        //public NfeCfop CfopNavigation { get; set; }
        //[ForeignKey("Company")]
        //[InverseProperty("RequestProduct")]
        //public CrudCompany CompanyNavigation { get; set; }
        //[ForeignKey("Product")]
        //[InverseProperty("RequestProduct")]
        //public Product ProductNavigation { get; set; }
        //[ForeignKey("Company,Request")]
        //[InverseProperty("RequestProduct")]
        //public Request RequestNavigation { get; set; }
        //[ForeignKey("Tax")]
        //[InverseProperty("RequestProduct")]
        //public NfeTaxGroup TaxNavigation { get; set; }
    }
}
