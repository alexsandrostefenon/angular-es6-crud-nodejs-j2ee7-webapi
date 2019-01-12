using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;

namespace AspNetCoreWebApi.Entity
{
    public partial class CrudContext : DbContext
    {
		public CrudContext()
        {
        }

		public CrudContext(DbContextOptions<CrudContext> options)
            : base(options)
        {
        }

        public virtual DbSet<Account> Account { get; set; }
        public virtual DbSet<BacenCountry> BacenCountry { get; set; }
        public virtual DbSet<Barcode> Barcode { get; set; }
        public virtual DbSet<CamexNcm> CamexNcm { get; set; }
        public virtual DbSet<Category> Category { get; set; }
        public virtual DbSet<CategoryCompany> CategoryCompany { get; set; }
        public virtual DbSet<ConfazCest> ConfazCest { get; set; }
        public virtual DbSet<CrudCompany> CrudCompany { get; set; }
        public virtual DbSet<CrudService> CrudService { get; set; }
        public virtual DbSet<CrudTranslation> CrudTranslation { get; set; }
        public virtual DbSet<CrudUser> CrudUser { get; set; }
        public virtual DbSet<IbgeCity> IbgeCity { get; set; }
        public virtual DbSet<IbgeCnae> IbgeCnae { get; set; }
        public virtual DbSet<IbgeUf> IbgeUf { get; set; }
        public virtual DbSet<Iso8583RouterChipApplicationIdentifier> Iso8583RouterChipApplicationIdentifier { get; set; }
        public virtual DbSet<Iso8583RouterChipPublicKey> Iso8583RouterChipPublicKey { get; set; }
        public virtual DbSet<Iso8583RouterComm> Iso8583RouterComm { get; set; }
        public virtual DbSet<Iso8583RouterLog> Iso8583RouterLog { get; set; }
        public virtual DbSet<Iso8583RouterMessageAdapter> Iso8583RouterMessageAdapter { get; set; }
        public virtual DbSet<Iso8583RouterMessageAdapterItem> Iso8583RouterMessageAdapterItem { get; set; }
        public virtual DbSet<Iso8583RouterTransaction> Iso8583RouterTransaction { get; set; }
        public virtual DbSet<NfeCfop> NfeCfop { get; set; }
        public virtual DbSet<NfeStCofins> NfeStCofins { get; set; }
        public virtual DbSet<NfeStCsosn> NfeStCsosn { get; set; }
        public virtual DbSet<NfeStIcms> NfeStIcms { get; set; }
        public virtual DbSet<NfeStIcmsDesoneracao> NfeStIcmsDesoneracao { get; set; }
        public virtual DbSet<NfeStIcmsModalidadeBc> NfeStIcmsModalidadeBc { get; set; }
        public virtual DbSet<NfeStIcmsModalidadeSt> NfeStIcmsModalidadeSt { get; set; }
        public virtual DbSet<NfeStIcmsOrigem> NfeStIcmsOrigem { get; set; }
        public virtual DbSet<NfeStIpi> NfeStIpi { get; set; }
        public virtual DbSet<NfeStIpiEnquadramento> NfeStIpiEnquadramento { get; set; }
        public virtual DbSet<NfeStIpiOperacao> NfeStIpiOperacao { get; set; }
        public virtual DbSet<NfeStPis> NfeStPis { get; set; }
        public virtual DbSet<NfeTaxGroup> NfeTaxGroup { get; set; }
        public virtual DbSet<PaymentType> PaymentType { get; set; }
        public virtual DbSet<Person> Person { get; set; }
        public virtual DbSet<Product> Product { get; set; }
        public virtual DbSet<Request> Request { get; set; }
        public virtual DbSet<RequestNfe> RequestNfe { get; set; }
        public virtual DbSet<RequestPayment> RequestPayment { get; set; }
        public virtual DbSet<RequestProduct> RequestProduct { get; set; }
        public virtual DbSet<RequestState> RequestState { get; set; }
        public virtual DbSet<RequestType> RequestType { get; set; }
        public virtual DbSet<Stock> Stock { get; set; }
        public virtual DbSet<StockAction> StockAction { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Account>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

				entity.HasIndex(e => new { e.Bank, e.Agency, e.Number })
                    .HasName("account_bank_agency_account_key")
                    .IsUnique();

                entity.Property(e => e.Id).ValueGeneratedOnAdd();
            });

            modelBuilder.Entity<BacenCountry>(entity =>
            {
                entity.HasIndex(e => e.Abr)
                    .HasName("bacen_country_abr_key")
                    .IsUnique();

                entity.HasIndex(e => e.Name)
                    .HasName("bacen_country_name_key")
                    .IsUnique();

                entity.HasIndex(e => e.NamePt)
                    .HasName("bacen_country_name_pt_key")
                    .IsUnique();

                entity.Property(e => e.Id).HasDefaultValueSql("1058");

                entity.Property(e => e.Abr).HasDefaultValueSql("'BR'::character varying");

                entity.Property(e => e.Name).HasDefaultValueSql("'Brazil'::character varying");

                entity.Property(e => e.NamePt).HasDefaultValueSql("'Brasil'::character varying");
            });

            modelBuilder.Entity<Barcode>(entity =>
            {
				entity.Property(e => e.Number).ValueGeneratedNever();
            });

            modelBuilder.Entity<CamexNcm>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<CategoryCompany>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.Property(e => e.Id).ValueGeneratedOnAdd();
            });

            modelBuilder.Entity<ConfazCest>(entity =>
            {
                entity.HasKey(e => new { e.Id, e.Ncm });
            });

            modelBuilder.Entity<CrudService>(entity =>
            {
                entity.Property(e => e.Name).ValueGeneratedNever();
            });

            modelBuilder.Entity<CrudUser>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Name });
            });

            modelBuilder.Entity<IbgeCity>(entity =>
            {
                entity.HasIndex(e => new { e.Name, e.Uf })
                    .HasName("ibge_city_name_uf_key")
                    .IsUnique();

                entity.Property(e => e.Id).ValueGeneratedNever();

                entity.Property(e => e.Uf).HasDefaultValueSql("43");
            });

            modelBuilder.Entity<IbgeCnae>(entity =>
            {
                entity.HasIndex(e => e.Name)
                    .HasName("ibge_cnae_name_key")
                    .IsUnique();

                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<IbgeUf>(entity =>
            {
                entity.HasIndex(e => e.Abr)
                    .HasName("ibge_uf_abr_key")
                    .IsUnique();

                entity.HasIndex(e => e.Name)
                    .HasName("ibge_uf_name_key")
                    .IsUnique();

                entity.Property(e => e.Id).ValueGeneratedNever();

                entity.Property(e => e.Abr).HasDefaultValueSql("'RS'::character varying");

                entity.Property(e => e.Country).HasDefaultValueSql("1058");

                entity.Property(e => e.Ddd).HasDefaultValueSql("NULL::character varying");
            });

            modelBuilder.Entity<Iso8583RouterChipApplicationIdentifier>(entity =>
            {
                entity.Property(e => e.ApplicationIdentifierCodeTag9f06).ValueGeneratedNever();
            });

            modelBuilder.Entity<Iso8583RouterChipPublicKey>(entity =>
            {
                entity.Property(e => e.PublicKeyCheckSum).ValueGeneratedNever();
            });

            modelBuilder.Entity<Iso8583RouterComm>(entity =>
            {
                entity.Property(e => e.Name).ValueGeneratedNever();
            });

            modelBuilder.Entity<Iso8583RouterLog>(entity =>
            {
                entity.Property(e => e.TimeId).ValueGeneratedNever();

                entity.HasOne(d => d.TransactionNavigation)
                    .WithMany(p => p.Iso8583RouterLog)
                    .HasForeignKey(d => d.TransactionId)
                    .HasConstraintName("iso8583_router_log_transaction_id_fkey");
            });

            modelBuilder.Entity<Iso8583RouterMessageAdapterItem>(entity =>
            {
                entity.HasKey(e => new { e.MessageAdapter, e.RootPattern, e.Tag });
            });

            modelBuilder.Entity<NfeCfop>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();

                entity.Property(e => e.IndComunica).HasDefaultValueSql("0");

                entity.Property(e => e.IndDevol).HasDefaultValueSql("0");

                entity.Property(e => e.IndNfe).HasDefaultValueSql("1");

                entity.Property(e => e.IndTransp).HasDefaultValueSql("0");
            });

            modelBuilder.Entity<NfeStCofins>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStCsosn>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIcms>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIcmsDesoneracao>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIcmsModalidadeBc>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIcmsModalidadeSt>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIcmsOrigem>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIpi>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIpiEnquadramento>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStIpiOperacao>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeStPis>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<NfeTaxGroup>(entity =>
            {
                entity.HasIndex(e => e.Name)
                    .HasName("nfe_tax_group_name_key")
                    .IsUnique();

                entity.Property(e => e.TaxCofins).HasDefaultValueSql("0.00");

                entity.Property(e => e.TaxIcms).HasDefaultValueSql("0.00");

                entity.Property(e => e.TaxIpi).HasDefaultValueSql("0.00");

                entity.Property(e => e.TaxIssqn).HasDefaultValueSql("0.00");

                entity.Property(e => e.TaxPis).HasDefaultValueSql("0.00");

                entity.Property(e => e.TaxSimples).HasDefaultValueSql("0.00");
            });

            modelBuilder.Entity<PaymentType>(entity =>
            {
                entity.Property(e => e.Id).ValueGeneratedNever();
            });

            modelBuilder.Entity<Person>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.HasIndex(e => new { e.Company, e.CnpjCpf })
                    .HasName("person_company_cnpj_cpf_key")
                    .IsUnique();

                entity.HasIndex(e => new { e.Company, e.Fantasy })
                    .HasName("person_company_fantasy_key")
                    .IsUnique();

                entity.HasIndex(e => new { e.Company, e.IeRg })
                    .HasName("person_company_ie_rg_key")
                    .IsUnique();

                entity.HasIndex(e => new { e.Company, e.Name })
                    .HasName("person_company_name_key")
                    .IsUnique();

                entity.Property(e => e.Id).ValueGeneratedOnAdd();

                entity.Property(e => e.City).HasDefaultValueSql("4304606");

                entity.Property(e => e.Country).HasDefaultValueSql("1058");

                entity.Property(e => e.Credit).HasDefaultValueSql("0.000");

                entity.Property(e => e.Crt).HasDefaultValueSql("1");

                entity.Property(e => e.Uf).HasDefaultValueSql("43");
            });

            modelBuilder.Entity<Product>(entity =>
            {
                entity.HasIndex(e => e.Name)
                    .HasName("product_name_key")
                    .IsUnique();

                entity.Property(e => e.Orig).HasDefaultValueSql("0");

                entity.Property(e => e.Weight).HasDefaultValueSql("0.000");
            });

            modelBuilder.Entity<Request>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.Property(e => e.Id).ValueGeneratedOnAdd();
            });

            modelBuilder.Entity<RequestNfe>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Request });

                entity.Property(e => e.Dhemi).HasDefaultValueSql("now()");

                entity.Property(e => e.Dhsaient).HasDefaultValueSql("now()");

                entity.Property(e => e.Finnfe).HasDefaultValueSql("1");

                entity.Property(e => e.Iddest).HasDefaultValueSql("1");

                entity.Property(e => e.Indfinal).HasDefaultValueSql("1");

                entity.Property(e => e.Indiedest).HasDefaultValueSql("9");

                entity.Property(e => e.Indpres).HasDefaultValueSql("1");

                entity.Property(e => e.Mod).HasDefaultValueSql("55");

                entity.Property(e => e.Natop).HasDefaultValueSql("'VENDA'::character varying");

                entity.Property(e => e.Nnf).ValueGeneratedOnAdd();

                entity.Property(e => e.Procemi).HasDefaultValueSql("0");

                entity.Property(e => e.Serie).HasDefaultValueSql("1");

                entity.Property(e => e.Tpamb).HasDefaultValueSql("1");

                entity.Property(e => e.Tpemis).HasDefaultValueSql("1");

                entity.Property(e => e.Tpimp).HasDefaultValueSql("1");

                entity.Property(e => e.Tpnf).HasDefaultValueSql("1");

                entity.Property(e => e.ValueCofins).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueIcms).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueIcmsSt).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueIi).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueIpi).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueIssqn).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValuePis).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueTax).HasDefaultValueSql("0.00");

                entity.Property(e => e.Verproc).HasDefaultValueSql("'1.0.000'::character varying");

                entity.Property(e => e.Versao).HasDefaultValueSql("'3.10'::character varying");
            });

            modelBuilder.Entity<RequestPayment>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.Property(e => e.Id).ValueGeneratedOnAdd();

                entity.Property(e => e.Balance).HasDefaultValueSql("0.000");

                entity.Property(e => e.Value).HasDefaultValueSql("0.000");
            });

            modelBuilder.Entity<RequestProduct>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.Property(e => e.Id).ValueGeneratedOnAdd();

                entity.Property(e => e.Quantity).HasDefaultValueSql("1.000");

                entity.Property(e => e.ValueAllTax).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueDesc).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueFreight).HasDefaultValueSql("0.00");

                entity.Property(e => e.ValueItem).HasDefaultValueSql("0.00");
            });

            modelBuilder.Entity<RequestState>(entity =>
            {
            });

            modelBuilder.Entity<Stock>(entity =>
            {
                entity.HasKey(e => new { e.Company, e.Id });

                entity.Property(e => e.CountIn).HasDefaultValueSql("0.000");

                entity.Property(e => e.CountOut).HasDefaultValueSql("0.000");

                entity.Property(e => e.EstimedOut).HasDefaultValueSql("0.000");

                entity.Property(e => e.EstimedValue).HasDefaultValueSql("0.000");

                entity.Property(e => e.MarginSale).HasDefaultValueSql("50.000");

                entity.Property(e => e.MarginWholesale).HasDefaultValueSql("25.000");

                entity.Property(e => e.ReservedIn).HasDefaultValueSql("0.000");

                entity.Property(e => e.ReservedOut).HasDefaultValueSql("0.000");

				entity.Property(e => e.StockValue).HasDefaultValueSql("0.000");

                entity.Property(e => e.StockDefault).HasDefaultValueSql("0.000");

                entity.Property(e => e.StockMinimal).HasDefaultValueSql("0.000");

                entity.Property(e => e.SumValueIn).HasDefaultValueSql("0.000");

                entity.Property(e => e.SumValueOut).HasDefaultValueSql("0.000");

                entity.Property(e => e.SumValueStock).HasDefaultValueSql("0.000");

                entity.Property(e => e.Value).HasDefaultValueSql("0.000");

                entity.Property(e => e.ValueWholesale).HasDefaultValueSql("0.000");
            });

            modelBuilder.Entity<StockAction>(entity =>
            {
                entity.HasIndex(e => e.Name)
                    .HasName("stock_action_name_key")
                    .IsUnique();
            });

            modelBuilder.HasSequence("hibernate_sequence");
        }
    }
}

