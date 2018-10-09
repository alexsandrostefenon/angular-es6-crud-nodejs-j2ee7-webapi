using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("bacen_country")]
    public partial class BacenCountry
    {
        public BacenCountry()
        {
			/*
            IbgeUf = new HashSet<IbgeUf>();
            Person = new HashSet<Person>();
			 */
        }

        [Key][Column("id")]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }
        [Required]
        [Column("name_pt", TypeName = "character varying(100)")]
        public string NamePt { get; set; }
        [Required]
        [Column("abr", TypeName = "character varying(2)")]
        public string Abr { get; set; }
/*
        [InverseProperty("CountryNavigation")]
        public ICollection<IbgeUf> IbgeUf { get; set; }
        [InverseProperty("CountryNavigation")]
        public ICollection<Person> Person { get; set; }
*/
    }
}
