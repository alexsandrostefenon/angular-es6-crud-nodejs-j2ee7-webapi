using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_ipi_enquadramento")]
    public partial class NfeStIpiEnquadramento
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }
        [Column("ipi_operacao")]
        public int? IpiOperacao { get; set; }

        //[ForeignKey("IpiOperacao")]
        //[InverseProperty("NfeStIpiEnquadramento")]
        //public NfeStIpiOperacao IpiOperacaoNavigation { get; set; }
    }
}
