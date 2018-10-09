using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("request_type")]
    public partial class RequestType
    {
        public RequestType()
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

        //[InverseProperty("TypeNavigation")]
        //public ICollection<Request> Request { get; set; }
    }
}
