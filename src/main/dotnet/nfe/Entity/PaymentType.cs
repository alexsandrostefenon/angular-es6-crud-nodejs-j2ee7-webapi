using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("payment_type")]
    public partial class PaymentType
    {
        public PaymentType()
        {
            //RequestPayment = new HashSet<RequestPayment>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("description", TypeName = "character varying(255)")]
        public string Description { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(50)")]
        public string Name { get; set; }

        //[InverseProperty("TypeNavigation")]
        //public ICollection<RequestPayment> RequestPayment { get; set; }
    }
}
