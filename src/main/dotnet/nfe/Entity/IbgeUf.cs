using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("ibge_uf")]
    public partial class IbgeUf
    {
        public IbgeUf()
        {
            //IbgeCity = new HashSet<IbgeCity>();
            //Person = new HashSet<Person>();
            //RequestFreight = new HashSet<RequestFreight>();
        }

        [Key][Column("id")]
        public int Id { get; set; }
		[Column("country")][ForeignKey("BacenCountry")]
        public int? Country { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }
        [Required]
        [Column("abr", TypeName = "character varying(2)")]
        public string Abr { get; set; }
        [Column("ddd", TypeName = "character varying(50)")]
        public string Ddd { get; set; }

        //[ForeignKey("Country")]
        //[InverseProperty("IbgeUf")]
        //public BacenCountry CountryNavigation { get; set; }
        //[InverseProperty("UfNavigation")]
        //public ICollection<IbgeCity> IbgeCity { get; set; }
        //[InverseProperty("UfNavigation")]
        //public ICollection<Person> Person { get; set; }
        //[InverseProperty("LicensePlateUfNavigation")]
        //public ICollection<RequestFreight> RequestFreight { get; set; }
    }
}
