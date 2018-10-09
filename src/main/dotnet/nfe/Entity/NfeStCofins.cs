using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("nfe_st_cofins")]
    public partial class NfeStCofins
    {
        public NfeStCofins()
        {
            //NfeTaxGroup = new HashSet<NfeTaxGroup>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("name", TypeName = "character varying(1024)")]
        public string Name { get; set; }

        //[InverseProperty("CstCofinsNavigation")]
        //public ICollection<NfeTaxGroup> NfeTaxGroup { get; set; }
    }
}
