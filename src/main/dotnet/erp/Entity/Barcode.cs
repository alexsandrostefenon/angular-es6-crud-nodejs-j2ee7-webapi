using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("barcode")]
    public partial class Barcode
    {
        [Key][Column("number", TypeName = "character varying(14)")]
		public string Number { get; set; }
        [Column("manufacturer", TypeName = "character varying(64)")]
        public string Manufacturer { get; set; }
		[Column("product")][ForeignKey("Product")][Display(Name = "Código de Barras de fornecedores de produtos")]
        public int? Product { get; set; }
/*
        [ForeignKey("Product")]
        [InverseProperty("Barcode")]
        public Product ProductNavigation { get; set; }
*/
    }
}
