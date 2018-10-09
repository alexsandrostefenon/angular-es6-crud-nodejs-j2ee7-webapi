using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_icms_origem")]
    public partial class NfeStIcmsOrigem
    {
        public NfeStIcmsOrigem()
        {
            //Product = new HashSet<Product>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }

        //[InverseProperty("OrigNavigation")]
        //public ICollection<Product> Product { get; set; }
    }
}
