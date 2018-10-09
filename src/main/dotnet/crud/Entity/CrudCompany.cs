using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("crud_company")]
    public partial class CrudCompany
    {
        public CrudCompany()
        {
/*			
            Account = new HashSet<Account>();
            CategoryCompany = new HashSet<CategoryCompany>();
            CrudUser = new HashSet<CrudUser>();
            Employed = new HashSet<Employed>();
            Person = new HashSet<Person>();
            Request = new HashSet<Request>();
            RequestFreight = new HashSet<RequestFreight>();
            RequestNfe = new HashSet<RequestNfe>();
            RequestPayment = new HashSet<RequestPayment>();
            RequestProduct = new HashSet<RequestProduct>();
            RequestRepair = new HashSet<RequestRepair>();
            RequestRepairInmetro = new HashSet<RequestRepairInmetro>();
            RequestService = new HashSet<RequestService>();
            Stock = new HashSet<Stock>();
            StockService = new HashSet<StockService>();
*/
        }

        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required]
        [Column("name", TypeName = "character varying(255)")]
        public string Name { get; set; }
/*
        [InverseProperty("CompanyNavigation")]
        public ICollection<Account> Account { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<CategoryCompany> CategoryCompany { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<CrudUser> CrudUser { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<Employed> Employed { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<Person> Person { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<Request> Request { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestFreight> RequestFreight { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestNfe> RequestNfe { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestPayment> RequestPayment { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestProduct> RequestProduct { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestRepair> RequestRepair { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestRepairInmetro> RequestRepairInmetro { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<RequestService> RequestService { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<Stock> Stock { get; set; }
        [InverseProperty("CompanyNavigation")]
        public ICollection<StockService> StockService { get; set; }
*/
    }
}
