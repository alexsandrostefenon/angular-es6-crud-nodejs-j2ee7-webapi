using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_message_adapter_item")]
    public partial class Iso8583RouterMessageAdapterItem
    {
        [Key][Column("id")][DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column("message_adapter", TypeName = "character varying(64)")]
        public string MessageAdapter { get; set; }
        [Column("alignment")]
        public int? Alignment { get; set; }
        [Column("data_format", TypeName = "character varying(255)")]
        public string DataFormat { get; set; }
        [Column("data_type")]
        public int DataType { get; set; }
        [Column("field_name", TypeName = "character varying(255)")]
        public string FieldName { get; set; }
        [Column("max_length")]
        public int MaxLength { get; set; }
        [Column("min_length")]
        public int MinLength { get; set; }
        [Required]
        [Column("root_pattern", TypeName = "character varying(255)")]
        public string RootPattern { get; set; }
        [Column("size_header")]
        public int SizeHeader { get; set; }
        [Required]
        [Column("tag", TypeName = "character varying(255)")]
        public string Tag { get; set; }

        [ForeignKey("MessageAdapter")]
        [InverseProperty("Iso8583RouterMessageAdapterItem")]
        public Iso8583RouterMessageAdapter MessageAdapterNavigation { get; set; }
    }
}
