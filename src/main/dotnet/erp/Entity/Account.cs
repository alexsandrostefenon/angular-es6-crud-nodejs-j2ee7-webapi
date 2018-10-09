using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("account")]
    public partial class Account
    {
        public Account()
        {
/*
            RequestPayment = new HashSet<RequestPayment>();
 */
        }

        [Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("number", TypeName = "character varying(20)")]
        public string Number { get; set; }
        [Column("agency", TypeName = "character varying(20)")]
        public string Agency { get; set; }
        [Column("bank", TypeName = "character varying(20)")]
        public string Bank { get; set; }
        [Column("description", TypeName = "character varying(255)")]
        public string Description { get; set; }
/*
        [ForeignKey("Company")]
        [InverseProperty("Account")]
        public CrudCompany CompanyNavigation { get; set; }
        [InverseProperty("AccountNavigation")]
        public ICollection<RequestPayment> RequestPayment { get; set; }
*/
    }
}
