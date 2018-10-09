using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request")]
    public partial class Request
    {
        public Request()
        {
            //RequestPayment = new HashSet<RequestPayment>();
            //RequestProduct = new HashSet<RequestProduct>();
            //RequestService = new HashSet<RequestService>();
        }

		[Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("type")][ForeignKey("RequestType")][Required][Editable(false,AllowInitialValue =true)]
        public int Type { get; set; }
		[Column("state")][ForeignKey("RequestState")][Required]
        public int State { get; set; }
		[Column("person")][ForeignKey("Person")][Required]
        public int Person { get; set; }
		[Column("date")][Required]
        public DateTime? Date { get; set; }
        [Column("additional_data", TypeName = "character varying(255)")]
        public string AdditionalData { get; set; }
		[Column("products_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("","","defaultValue","0.000")]
        public decimal ProductsValue { get; set; }
		[Column("services_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal ServicesValue { get; set; }
		[Column("transport_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal TransportValue { get; set; }
		[Column("sum_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal SumValue { get; set; }
		[Column("payments_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal PaymentsValue { get; set; }

        //[ForeignKey("Company")]
        //[InverseProperty("Request")]
        //public CrudCompany CompanyNavigation { get; set; }
        //[ForeignKey("Company,Person")]
        //[InverseProperty("Request")]
        //public Person PersonNavigation { get; set; }
        //[ForeignKey("State")]
        //[InverseProperty("Request")]
        //public RequestState StateNavigation { get; set; }
        //[ForeignKey("Type")]
        //[InverseProperty("Request")]
        //public RequestType TypeNavigation { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public RequestFreight RequestFreight { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public RequestNfe RequestNfe { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public RequestRepair RequestRepair { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public RequestRepairInmetro RequestRepairInmetro { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public ICollection<RequestPayment> RequestPayment { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public ICollection<RequestProduct> RequestProduct { get; set; }
        //[InverseProperty("RequestNavigation")]
        //public ICollection<RequestService> RequestService { get; set; }
    }
}
