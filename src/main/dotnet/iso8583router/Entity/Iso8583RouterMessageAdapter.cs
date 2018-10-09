using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_message_adapter")]
    public partial class Iso8583RouterMessageAdapter
    {
        public Iso8583RouterMessageAdapter()
        {
            InverseParentNavigation = new HashSet<Iso8583RouterMessageAdapter>();
            Iso8583RouterMessageAdapterItem = new HashSet<Iso8583RouterMessageAdapterItem>();
        }

        [Key][Column("name", TypeName = "character varying(64)")]
        public string Name { get; set; }
        [Column("parent", TypeName = "character varying(64)")]
        public string Parent { get; set; }
        [Required]
        [Column("adapter_class", TypeName = "character varying(255)")]
        public string AdapterClass { get; set; }
        [Column("compress")]
        public bool? Compress { get; set; }
        [Column("tag_prefix", TypeName = "character varying(32)")]
        public string TagPrefix { get; set; }

        [ForeignKey("Parent")]
        [InverseProperty("InverseParentNavigation")]
        public Iso8583RouterMessageAdapter ParentNavigation { get; set; }
        [InverseProperty("ParentNavigation")]
        public ICollection<Iso8583RouterMessageAdapter> InverseParentNavigation { get; set; }
        [InverseProperty("MessageAdapterNavigation")]
        public ICollection<Iso8583RouterMessageAdapterItem> Iso8583RouterMessageAdapterItem { get; set; }
    }
}
