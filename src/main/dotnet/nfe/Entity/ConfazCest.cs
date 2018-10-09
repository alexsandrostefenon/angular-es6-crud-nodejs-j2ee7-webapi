using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("confaz_cest")]
    public partial class ConfazCest
    {
        [Key][Column("id")]
        public int Id { get; set; }
		[Column("ncm")][ForeignKey("CamexNcm")][Required]
        public int Ncm { get; set; }
		[Column("name", TypeName = "character varying(1024)")][Required]
        public string Name { get; set; }
    }
}
