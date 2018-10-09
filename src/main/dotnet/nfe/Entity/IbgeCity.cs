using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("ibge_city")]
    public partial class IbgeCity
    {
        public IbgeCity()
        {
            //Person = new HashSet<Person>();
        }

        [Key][Column("id")]
        public int Id { get; set; }
		[Column("uf")][ForeignKey("IbgeUf")]
        public int? Uf { get; set; }
        [Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }

        //[ForeignKey("Uf")]
        //[InverseProperty("IbgeCity")]
        //public IbgeUf UfNavigation { get; set; }
        //[InverseProperty("CityNavigation")]
        //public ICollection<Person> Person { get; set; }
    }
}
