using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("product")]
    public partial class Product
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("crud_group")][ForeignKey("CrudGroup")]
        public int? CrudGroup { get; set; }
		[Column("ncm")][ForeignKey("CamexNcm")]
        public int? Ncm { get; set; }
		[Column("orig")][FilterUIHint("", "", "defaultValue", "0", "options", "0,3,4,5,8")][Required]
        public int? Orig { get; set; }
        [Column("name", TypeName = "character varying(120)")]
        public string Name { get; set; }
        [Column("departament", TypeName = "character varying(64)")]
        public string Departament { get; set; }
        [Column("model", TypeName = "character varying(255)")]
        public string Model { get; set; }
        [Column("description", TypeName = "character varying(255)")]
        public string Description { get; set; }
        [Column("weight", TypeName = "numeric(9,3)")]
        public decimal? Weight { get; set; }
        [Column("image_url", TypeName = "character varying(255)")]
        public string ImageUrl { get; set; }
        [Column("additional_data", TypeName = "character varying(255)")]
        public string AdditionalData { get; set; }
    }
}
