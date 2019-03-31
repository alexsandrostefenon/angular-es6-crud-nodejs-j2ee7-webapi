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
        }

        [Key][Column("crud_group_owner")][ForeignKey("CrudGroupOwner")]
        public int? CrudGroupOwner { get; set; }
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
    }
}
