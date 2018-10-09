using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("category_company")]
    public partial class CategoryCompany
    {
		[Key][Column("company")][ForeignKey("CrudCompany")][Display(Name = "Categorias Vinculadas")][FilterUIHint("", "", "isClonable", "true")]
        public int? Company { get; set; }
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
		[Column("category")][ForeignKey ("Category")][Required]
        public int? Category { get; set; }
/*
        [ForeignKey("Category")]
        [InverseProperty("CategoryCompany")]
        public Category CategoryNavigation { get; set; }
        [ForeignKey("Company")]
        [InverseProperty("CategoryCompany")]
        public CrudCompany CompanyNavigation { get; set; }
 */
    }
}
