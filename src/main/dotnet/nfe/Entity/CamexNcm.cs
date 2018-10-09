using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("camex_ncm")]
    public partial class CamexNcm
    {
        public CamexNcm()
        {
/*			
            Product = new HashSet<Product>();
*/
        }

        [Key][Column("id")]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }
		[Column("unit", TypeName = "character varying(16)")][Required]
        public string Unit { get; set; }
		[Column("tec")][Required]
        public int? Tec { get; set; }
/*
        [InverseProperty("NcmNavigation")]
        public ICollection<Product> Product { get; set; }
*/
    }
}
