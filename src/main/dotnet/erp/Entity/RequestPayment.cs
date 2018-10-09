using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request_payment")]
    public partial class RequestPayment
    {
		[Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
		[Key][Column("request")][ForeignKey("Request")]
        public int Request { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("type")][Required][ForeignKey("PaymentType")]
        public int Type { get; set; }
		[Column("value", TypeName = "numeric(9,2)")][Required]
        public decimal Value { get; set; }
		[Column("account")][Required][ForeignKey("Account")]
        public int Account { get; set; }
        [Column("number", TypeName = "character varying(16)")]
        public string Number { get; set; }
		[Column("due_date")][Required][FilterUIHint("", "", "defaultValue", "now")]
        public DateTime DueDate { get; set; }
        [Column("payday")]
        public DateTime? Payday { get; set; }
		[Column("balance", TypeName = "numeric(9,2)")][Editable(false)]
        public decimal Balance { get; set; }

        //[ForeignKey("Company,Account")]
        //[InverseProperty("RequestPayment")]
        //public Account AccountNavigation { get; set; }
        //[ForeignKey("Company")]
        //[InverseProperty("RequestPayment")]
        //public CrudCompany CompanyNavigation { get; set; }
        //[ForeignKey("Company,Request")]
        //[InverseProperty("RequestPayment")]
        //public Request RequestNavigation { get; set; }
        //[ForeignKey("Type")]
        //[InverseProperty("RequestPayment")]
        //public PaymentType TypeNavigation { get; set; }
    }
}
