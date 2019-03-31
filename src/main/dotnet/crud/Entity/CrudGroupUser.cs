using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("crud_group_user")]
    public partial class CrudGroupUser
    {
		[Key][Column("crud_user")][ForeignKey("CrudUser")][Display(Name = "Grupos Vinculados")][FilterUIHint("", "", "isClonable", "true")]
        public String CrudUser { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("crud_group")][ForeignKey ("CrudGroup")][Required]
        public int CrudGroup { get; set; }
    }
}
