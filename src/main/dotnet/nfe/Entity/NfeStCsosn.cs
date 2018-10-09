using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_csosn")]
    public partial class NfeStCsosn
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }
        [Column("description", TypeName = "character varying(1024)")]
        public string Description { get; set; }
    }
}
