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

		[Key][Column("crud_group_owner")][ForeignKey("CrudGroupOwner")]
        public int? CrudGroupOwner { get; set; }
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
		[Column("desc_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal descValue { get; set; }
		[Column("sum_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal SumValue { get; set; }
		[Column("payments_value", TypeName = "numeric(19,2)")][Editable(false)][Required][FilterUIHint("", "", "defaultValue", "0.000")]
        public decimal PaymentsValue { get; set; }
    }
}
