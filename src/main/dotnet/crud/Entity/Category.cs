using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("category")]
    public partial class Category
    {
        public Category()
        {
/*
            CategoryCompany = new HashSet<CategoryCompany>();
            Product = new HashSet<Product>();
            Service = new HashSet<Service>();
 */
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(100)")]
        public string Name { get; set; }
/*
        [InverseProperty("CategoryNavigation")]
        public ICollection<CategoryCompany> CategoryCompany { get; set; }
        [InverseProperty("CategoryNavigation")]
        public ICollection<Product> Product { get; set; }
        [InverseProperty("CategoryNavigation")]
        public ICollection<Service> Service { get; set; }
 */
    }
}
