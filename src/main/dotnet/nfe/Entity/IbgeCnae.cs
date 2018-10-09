using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("ibge_cnae")]
    public partial class IbgeCnae
    {
        public IbgeCnae()
        {
            //Person = new HashSet<Person>();
        }

        [Key][Column("id")]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(512)")]
        public string Name { get; set; }

        //[InverseProperty("CnaeNavigation")]
        //public ICollection<Person> Person { get; set; }
    }
}
