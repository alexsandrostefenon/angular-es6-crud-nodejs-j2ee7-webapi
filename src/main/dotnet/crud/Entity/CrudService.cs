using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("crud_service")]
    public partial class CrudService
    {
        [Key][Column("name", TypeName = "character varying(512)")]
        public string Name { get; set; }
		[Column ("title", TypeName = "character varying(255)")]
		public string Title { get; set; }
        [Column("template", TypeName = "character varying(512)")]
        public string Template { get; set; }
        [Column("menu", TypeName = "character varying(255)")]
        public string Menu { get; set; }
        [Column("save_and_exit")]
        public bool? SaveAndExit { get; set; }
        [Column("filter_fields", TypeName = "character varying(10240)")]
        public string FilterFields { get; set; }
        [Column("order_by", TypeName = "character varying(512)")]
        public string OrderBy { get; set; }
		[Column ("is_on_line")]
		public bool? IsOnLine { get; set; }
		[Column ("fields", TypeName = "character varying(10240)")][Editable (false)]
		public string Fields { get; set; }
    }
}
