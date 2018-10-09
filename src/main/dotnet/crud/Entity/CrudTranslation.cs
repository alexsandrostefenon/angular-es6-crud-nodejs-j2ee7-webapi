using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("crud_translation")]
    public partial class CrudTranslation
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required]
		[Column("locale", TypeName = "character varying(255)")][FilterUIHint("", "", "defaultValue", "pt-br")]
        public string Locale { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }
        [Column("translation", TypeName = "character varying(255)")]
        public string Translation { get; set; }
    }
}
