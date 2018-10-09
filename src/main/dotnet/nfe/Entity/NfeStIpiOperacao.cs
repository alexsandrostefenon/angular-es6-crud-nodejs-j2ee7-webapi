using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_ipi_operacao")]
    public partial class NfeStIpiOperacao
    {
        public NfeStIpiOperacao()
        {
            //NfeStIpiEnquadramento = new HashSet<NfeStIpiEnquadramento>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }

        //[InverseProperty("IpiOperacaoNavigation")]
        //public ICollection<NfeStIpiEnquadramento> NfeStIpiEnquadramento { get; set; }
    }
}
