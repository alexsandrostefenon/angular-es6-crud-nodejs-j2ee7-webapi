using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace AspNetCoreWebApi.Entity
{
    [Table("iso8583_router_chip_public_key")]
    public partial class Iso8583RouterChipPublicKey
    {
        [Key][Column("public_key_check_sum", TypeName = "character varying(40)")]
        public string PublicKeyCheckSum { get; set; }
        [Column("exp_size")]
        public int? ExpSize { get; set; }
        [Column("hash_status")]
        public int? HashStatus { get; set; }
        [Column("public_key_check_exponent_tag_9f2e", TypeName = "character varying(6)")]
        public string PublicKeyCheckExponentTag9f2e { get; set; }
        [Column("public_key_index_tag_9f22", TypeName = "character varying(2)")]
        public string PublicKeyIndexTag9f22 { get; set; }
        [Column("public_key_modulus_tag_9f2d", TypeName = "character varying(496)")]
        public string PublicKeyModulusTag9f2d { get; set; }
        [Column("registered_application_provider_identifier", TypeName = "character varying(10)")]
        public string RegisteredApplicationProviderIdentifier { get; set; }
    }
}
