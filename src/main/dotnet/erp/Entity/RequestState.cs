using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request_state")]
    public partial class RequestState
    {
        public RequestState()
        {
            //Request = new HashSet<Request>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("description", TypeName = "character varying(100)")]
        public string Description { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }
		[Column("next")][ForeignKey("RequestState")]
        public int? Next { get; set; }
		[Column("prev")][ForeignKey("RequestState")]
        public int? Prev { get; set; }
		[Column("stock_action")][ForeignKey("StockAction")]
        public int? StockAction { get; set; }
		[Column("type")][ForeignKey("RequestType")]
        public int? Type { get; set; }

        //[ForeignKey("StockAction")]
        //[InverseProperty("RequestState")]
        //public StockAction StockActionNavigation { get; set; }
        //[InverseProperty("StateNavigation")]
        //public ICollection<Request> Request { get; set; }
    }
}
