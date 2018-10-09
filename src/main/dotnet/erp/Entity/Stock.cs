using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("stock")]
    public partial class Stock
    {
        [Key][Column("company")][ForeignKey("CrudCompany")]
        public int? Company { get; set; }
		[Key][Column("id")][ForeignKey("Product")]
        public int Id { get; set; }
        [Column("count_in", TypeName = "numeric(9,3)")]
        public decimal? CountIn { get; set; }
        [Column("count_out", TypeName = "numeric(9,3)")]
        public decimal? CountOut { get; set; }
        [Column("estimed_in", TypeName = "numeric(9,3)")]
        public decimal? EstimedIn { get; set; }
        [Column("estimed_out", TypeName = "numeric(9,3)")]
        public decimal? EstimedOut { get; set; }
        [Column("estimed_value", TypeName = "numeric(9,3)")]
        public decimal? EstimedValue { get; set; }
        [Column("margin_sale", TypeName = "numeric(9,3)")]
        public decimal? MarginSale { get; set; }
        [Column("margin_wholesale", TypeName = "numeric(9,3)")]
        public decimal? MarginWholesale { get; set; }
        [Column("reserved_in", TypeName = "numeric(9,3)")]
        public decimal? ReservedIn { get; set; }
        [Column("reserved_out", TypeName = "numeric(9,3)")]
        public decimal? ReservedOut { get; set; }
        [Column("stock_value", TypeName = "numeric(9,3)")]
        public decimal? StockValue { get; set; }
        [Column("stock_default", TypeName = "numeric(9,3)")]
        public decimal? StockDefault { get; set; }
        [Column("stock_minimal", TypeName = "numeric(9,3)")]
        public decimal? StockMinimal { get; set; }
        [Column("stock_serials", TypeName = "character varying(1024)")]
        public string StockSerials { get; set; }
        [Column("sum_value_in", TypeName = "numeric(9,3)")]
        public decimal? SumValueIn { get; set; }
        [Column("sum_value_out", TypeName = "numeric(9,3)")]
        public decimal? SumValueOut { get; set; }
        [Column("sum_value_stock", TypeName = "numeric(9,3)")]
        public decimal? SumValueStock { get; set; }
        [Column("value", TypeName = "numeric(9,3)")]
        public decimal? Value { get; set; }
        [Column("value_wholesale", TypeName = "numeric(9,3)")]
        public decimal? ValueWholesale { get; set; }

        //[ForeignKey("Company")]
        //[InverseProperty("Stock")]
        //public CrudCompany CompanyNavigation { get; set; }
        //[ForeignKey("Id")]
        //[InverseProperty("Stock")]
        //public Product IdNavigation { get; set; }
    }
}
