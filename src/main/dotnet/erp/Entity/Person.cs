using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("person")]
    public partial class Person
    {
        public Person()
        {
/*
            Request = new HashSet<Request>();
            RequestFreight = new HashSet<RequestFreight>();
            RequestNfe = new HashSet<RequestNfe>();
 */
        }

        [Column("crud_group_owner")]
        public int? CrudGroupOwner { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }
        [Column("fantasy", TypeName = "character varying(100)")]
        public string Fantasy { get; set; }
        [Column("cnpj_cpf", TypeName = "character varying(18)")]
        public string CnpjCpf { get; set; }
        [Column("ie_rg", TypeName = "character varying(12)")]
        public string IeRg { get; set; }
        [Column("suframa", TypeName = "character varying(9)")]
        public string Suframa { get; set; }
        [Column("im", TypeName = "character varying(12)")]
        public string Im { get; set; }
		[Column("cnae")][ForeignKey("IbgeCnae")]
        public int? Cnae { get; set; }
		// 1=Simples Nacional; 2=Simples Nacional, excesso sublimite de receita bruta; 3=Regime Normal. (v2.0).
		[Column("crt")][FilterUIHint("", "", "defaultValue", "1", "options", "1 - Simples Nacional,2 - Simples Nacional (excesso sublimite de receita bruta),3 - Regime Normal")]
        public int? Crt { get; set; }
        [Column("zip", TypeName = "character(9)")]
        public string Zip { get; set; }
		[Column("country")][ForeignKey("BacenCountry")]
        public int? Country { get; set; }
		[Column("uf")][ForeignKey("IbgeUf")]
        public int? Uf { get; set; }
		[Column("city")][ForeignKey("IbgeCity")]
        public int? City { get; set; }
        [Column("district", TypeName = "character varying(64)")]
        public string District { get; set; }
        [Column("address", TypeName = "character varying(100)")]
        public string Address { get; set; }
        [Column("address_number", TypeName = "character varying(16)")]
        public string AddressNumber { get; set; }
        [Column("complement", TypeName = "character varying(16)")]
        public string Complement { get; set; }
        [Column("email", TypeName = "character varying(100)")]
        public string Email { get; set; }
        [Column("site", TypeName = "character varying(100)")]
        public string Site { get; set; }
        [Column("phone", TypeName = "character varying(16)")]
        public string Phone { get; set; }
        [Column("fax", TypeName = "character varying(16)")]
        public string Fax { get; set; }
		[Column("credit", TypeName = "numeric(9,3)")][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal? Credit { get; set; }
        [Column("additional_data", TypeName = "character varying(255)")]
        public string AdditionalData { get; set; }
/*
        [ForeignKey("City")]
        [InverseProperty("Person")]
        public IbgeCity CityNavigation { get; set; }
        [ForeignKey("Cnae")]
        [InverseProperty("Person")]
        public IbgeCnae CnaeNavigation { get; set; }
        [ForeignKey("Country")]
        [InverseProperty("Person")]
        public BacenCountry CountryNavigation { get; set; }
        [ForeignKey("Uf")]
        [InverseProperty("Person")]
        public IbgeUf UfNavigation { get; set; }
        [InverseProperty("Person")]
        public Employed Employed { get; set; }
        [InverseProperty("PersonNavigation")]
        public ICollection<Request> Request { get; set; }
        [InverseProperty("PersonNavigation")]
        public ICollection<RequestFreight> RequestFreight { get; set; }
        [InverseProperty("PersonNavigation")]
        public ICollection<RequestNfe> RequestNfe { get; set; }
*/
    }
}
