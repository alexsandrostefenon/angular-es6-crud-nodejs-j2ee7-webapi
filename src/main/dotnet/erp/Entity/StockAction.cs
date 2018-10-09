using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("stock_action")]
    public partial class StockAction
    {
        public StockAction()
        {
            //RequestState = new HashSet<RequestState>();
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }

        //[InverseProperty("StockActionNavigation")]
        //public ICollection<RequestState> RequestState { get; set; }
    }
}
