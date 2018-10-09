using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request_nfe")]
    public partial class RequestNfe
    {
        public RequestNfe()
        {
            //RequestRepair = new HashSet<RequestRepair>();
        }

        [Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
		[Key][Column("request")][ForeignKey("Request")]
        public int Request { get; set; }
        [Column("person")]
        public int? Person { get; set; }
        [Column("versao", TypeName = "character varying(4)")]
        public string Versao { get; set; }
        [Column("nfe_id", TypeName = "character(47)")]
        public string NfeId { get; set; }
		[Column("natop", TypeName = "character varying(60)")][Required][FilterUIHint("", "", "defaultValue", "Venda")]
        public string Natop { get; set; }
		[Column("indpag")][Required][FilterUIHint("", "", "defaultValue", "0", "options", "0,1,2")]
        public int Indpag { get; set; }
        [Column("mod")]
        public int? Mod { get; set; }
        [Column("serie")]
        public int? Serie { get; set; }
        [Column("nnf")]
        public int Nnf { get; set; }
		[Column("dhemi")][FilterUIHint("", "", "defaultValue", "now")]
        public DateTime? Dhemi { get; set; }
		[Column("dhsaient")][FilterUIHint("", "", "defaultValue", "now")]
        public DateTime? Dhsaient { get; set; }
        [Column("tpnf")]
        public int? Tpnf { get; set; }
		[Column("iddest")][Required][FilterUIHint("", "", "defaultValue", "1", "options", "1,2,3")]
        public int Iddest { get; set; }
        [Column("tpimp")]
        public int? Tpimp { get; set; }
        [Column("tpemis")]
        public int? Tpemis { get; set; }
        [Column("cdv")]
        public int? Cdv { get; set; }
        [Column("tpamb")]
        public int? Tpamb { get; set; }
        [Column("finnfe")]
        public int? Finnfe { get; set; }
		[Column("indfinal")][Required][FilterUIHint("", "", "defaultValue", "1", "options", "0,1")]
        public int Indfinal { get; set; }
		[Column("indpres")][Required][FilterUIHint("", "", "defaultValue", "1", "options", "0,1,2,3,4,9")]
        public int Indpres { get; set; }
        [Column("procemi")]
        public int? Procemi { get; set; }
        [Column("verproc", TypeName = "character varying(20)")]
        public string Verproc { get; set; }
        [Column("indiedest")]
        public int? Indiedest { get; set; }
        [Column("value_ii", TypeName = "numeric(9,2)")]
        public decimal? ValueIi { get; set; }
        [Column("value_ipi", TypeName = "numeric(9,2)")]
        public decimal? ValueIpi { get; set; }
        [Column("value_pis", TypeName = "numeric(9,2)")]
        public decimal? ValuePis { get; set; }
        [Column("value_cofins", TypeName = "numeric(9,2)")]
        public decimal? ValueCofins { get; set; }
        [Column("value_icms", TypeName = "numeric(9,2)")]
        public decimal? ValueIcms { get; set; }
        [Column("value_icms_st", TypeName = "numeric(9,2)")]
        public decimal? ValueIcmsSt { get; set; }
        [Column("value_issqn", TypeName = "numeric(9,2)")]
        public decimal? ValueIssqn { get; set; }
        [Column("value_tax", TypeName = "numeric(9,2)")]
        public decimal? ValueTax { get; set; }

        //[ForeignKey("Company")]
        //[InverseProperty("RequestNfe")]
        //public CrudCompany CompanyNavigation { get; set; }
        //[ForeignKey("Company,Person")]
        //[InverseProperty("RequestNfe")]
        //public Person PersonNavigation { get; set; }
        //[ForeignKey("Company,Request")]
        //[InverseProperty("RequestNfe")]
        //public Request RequestNavigation { get; set; }
        //[InverseProperty("RequestNfe")]
        //public ICollection<RequestRepair> RequestRepair { get; set; }
    }
}
