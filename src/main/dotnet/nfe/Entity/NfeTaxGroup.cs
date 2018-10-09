using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_tax_group")]
    public partial class NfeTaxGroup
    {
        public NfeTaxGroup()
        {
            //RequestProduct = new HashSet<RequestProduct>();
            //RequestService = new HashSet<RequestService>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Required][Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }
        [Column("cst_ipi")]
        public int? CstIpi { get; set; }
        [Column("cst_icms")]
        public int? CstIcms { get; set; }
        [Column("cst_pis")]
        public int? CstPis { get; set; }
        [Column("cst_cofins")]
        public int? CstCofins { get; set; }
        [Column("tax_simples", TypeName = "numeric(5,2)")]
        public decimal? TaxSimples { get; set; }
        [Column("tax_ipi", TypeName = "numeric(5,2)")]
        public decimal? TaxIpi { get; set; }
        [Column("tax_icms", TypeName = "numeric(5,2)")]
        public decimal? TaxIcms { get; set; }
        [Column("tax_pis", TypeName = "numeric(5,2)")]
        public decimal? TaxPis { get; set; }
        [Column("tax_cofins", TypeName = "numeric(5,2)")]
        public decimal? TaxCofins { get; set; }
        [Column("tax_issqn", TypeName = "numeric(5,2)")]
        public decimal? TaxIssqn { get; set; }

        //[ForeignKey("CstCofins")]
        //[InverseProperty("NfeTaxGroup")]
        //public NfeStCofins CstCofinsNavigation { get; set; }
        //[ForeignKey("CstIcms")]
        //[InverseProperty("NfeTaxGroup")]
        //public NfeStIcms CstIcmsNavigation { get; set; }
        //[ForeignKey("CstIpi")]
        //[InverseProperty("NfeTaxGroup")]
        //public NfeStIpi CstIpiNavigation { get; set; }
        //[ForeignKey("CstPis")]
        //[InverseProperty("NfeTaxGroup")]
        //public NfeStPis CstPisNavigation { get; set; }
        //[InverseProperty("TaxNavigation")]
        //public ICollection<RequestProduct> RequestProduct { get; set; }
        //[InverseProperty("TaxNavigation")]
        //public ICollection<RequestService> RequestService { get; set; }
    }
}
