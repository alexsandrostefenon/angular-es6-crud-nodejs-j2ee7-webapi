using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_cfop")]
    public partial class NfeCfop
    {
        public NfeCfop()
        {
            //RequestProduct = new HashSet<RequestProduct>();
            //RequestService = new HashSet<RequestService>();
        }

        [Key][Column("id")]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }
        [Column("ind_nfe")]
        public int? IndNfe { get; set; }
        [Column("ind_comunica")]
        public int? IndComunica { get; set; }
        [Column("ind_transp")]
        public int? IndTransp { get; set; }
        [Column("ind_devol")]
        public int? IndDevol { get; set; }

        //[InverseProperty("CfopNavigation")]
        //public ICollection<RequestProduct> RequestProduct { get; set; }
        //[InverseProperty("CfopNavigation")]
        //public ICollection<RequestService> RequestService { get; set; }
    }
}
