using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_icms_desoneracao")]
    public partial class NfeStIcmsDesoneracao
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }
    }
}
