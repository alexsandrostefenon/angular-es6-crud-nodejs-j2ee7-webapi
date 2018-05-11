CREATE TABLE account (
	company integer references crud_company,
	id SERIAL,
	account character varying(20),
	agency character varying(20),
	bank character varying(20),
	description character varying(255),
	PRIMARY KEY(company,id),
	unique(bank,agency,account)
);

CREATE TABLE person (
	company integer references crud_company,
	id SERIAL,
	name varchar(100) not null,
	fantasy varchar(100),
	cnpj_cpf varchar(18),
	ie_rg varchar(12),
	suframa varchar(9), -- Inscrição na SUFRAMA
	im varchar(12), -- inscricao municipal
	cnae integer references ibge_cnae,
	crt integer default 1, -- 1=Simples Nacional; 2=Simples Nacional, excesso sublimite de receita bruta; 3=Regime Normal. (v2.0).
	zip char(9),
	country integer references bacen_country default 1058,
	uf integer references ibge_uf default 43,
	city integer references ibge_city default 4304606,
	district character varying(64),
	address character varying(100),-- logradouro
	address_number character varying(16),
	complement character varying(16),
	email character varying(100),
	site character varying(100),
	phone character varying(16),
	fax character varying(16),
	credit numeric(9,3) DEFAULT 0.000,
	additional_data character varying(255),
	PRIMARY KEY(company,id),
	UNIQUE(company,name),
	UNIQUE(company,fantasy),
	UNIQUE(company,cnpj_cpf),
	UNIQUE(company,ie_rg)
);

CREATE TABLE product (
	id SERIAL PRIMARY KEY,
	category integer references category, -- replace to camex_ncm
	ncm integer references camex_ncm,
	orig integer references nfe_st_icms_origem default 0,
	name character varying(120) UNIQUE,
	departament character varying(64),
	model character varying(255),
	description character varying(255),
	weight numeric(9,3) DEFAULT 0.000,
	image_url character varying(255),
	additional_data character varying(255)
);

CREATE TABLE barcode (
	barcode varchar(14) PRIMARY KEY, -- GTIN-8, GTIN-12, GTIN-13 ou GTIN-14 (antigos códigos EAN, UPC e DUN-14),
	manufacturer varchar(64), -- fabricante
	product integer references product
);
-- compra,venda,fabricação,desmonte,conserto
CREATE TABLE request_type (
	id SERIAL PRIMARY KEY,
	description character varying(100),
	name character varying(255) NOT NULL
);

CREATE TABLE stock_action (
	id SERIAL PRIMARY KEY,
	name character varying(255) UNIQUE NOT NULL
);

-- aguardando aprovação, aguardando resposta 
CREATE TABLE request_state (
	id SERIAL PRIMARY KEY,
	description character varying(100),
	name character varying(255) NOT NULL,
	next integer,
	prev integer,
	stock_action integer references stock_action,
	type integer
);

CREATE TABLE request (
	company integer references crud_company,
	id SERIAL NOT NULL,
	type integer references request_type,
	state integer references request_state,
	person integer,
	date timestamp without time zone,
	additional_data character varying(255),
	products_value numeric(19,2),
	services_value numeric(19,2),
	transport_value numeric(19,2),
	sum_value numeric(19,2),
	payments_value numeric(19,2),
	PRIMARY KEY(company,id),
	FOREIGN KEY(company, person) REFERENCES person(company, id)
);

CREATE TABLE request_freight ( -- frete
	company integer references crud_company,
	request integer,
	person integer,
	pay_by integer DEFAULT 0, -- 0=Por conta do emitente; 1=Por conta do destinatário/remetente; 2=Por conta de terceiros; 9=Sem frete. (V2.0)
	license_plate char(9),--ABC123456
	license_plate_uf integer references ibge_uf default 43,    
	containers_type varchar(60) default 'Volumes', -- caixas, garrafas, paletes, bag, etc...
	containers_count int DEFAULT 1, -- quantidade de embalagens
	weight numeric(9,3) DEFAULT 0.000, -- peso líquido
	weight_final numeric(9,3) DEFAULT 0.000, -- peso bruto
	logo varchar(60), -- marca visível da embalagem
	value numeric(9,2) DEFAULT 0.00,
	PRIMARY KEY(company,request),
	FOREIGN KEY(company,request) REFERENCES request(company, id),
	FOREIGN KEY(company,person) REFERENCES person(company, id),
	UNIQUE(company,request)
);
-- um por request
CREATE TABLE request_nfe ( -- nota fiscal
	company integer references crud_company,
	request integer,
	person integer,-- emit/dest primeiro cadastro de person para esta company
	versao varchar(4) default '3.10',
	nfe_id char(47),
	natOp varchar(60) default 'VENDA',
	indPag integer,-- 0=Pagamento à vista; 1=Pagamento a prazo; 2=Outros.
	mod integer default 55,-- 55=NF-e emitida em substituição ao modelo 1 ou 1A; 65=NFC-e, utilizada nas operações de venda no varejo (a critério da UF aceitar este modelo de documento).
	serie integer default 1,
	nNF SERIAL not null,
	dhEmi timestamp without time zone default now(),-- Data e hora no formato UTC (Universal Coordinated Time): AAAA-MM-DDThh:mm:ssTZD
	dhSaiEnt timestamp without time zone default now(),
	tpNF integer default 1,-- 0=Entrada; 1=Saída
	idDest integer default 1,-- 1=Operação interna; 2=Operação interestadual; 3=Operação com exterior.
	tpImp integer default 1,-- 0=Sem geração de DANFE; 1=DANFE normal, Retrato; 2=DANFE normal, Paisagem; 3=DANFE Simplificado; 4=DANFE NFC-e; 5=DANFE NFC-e somente em mensagem eletrônica
	tpEmis integer default 1,
	cDV integer,-- DV da Chave de Acesso da NF-e, o DV será calculado com a aplicação do algoritmo módulo 11 (base 2,9) da Chave de Acesso. (vide item 5 do Manual de Orientação)
	tpAmb integer default 1,-- 1=Produção/2=Homologação
	finNFe integer default 1,-- 1=NF-e normal; 2=NF-e complementar; 3=NF-e de ajuste; 4=Devolução de mercadoria.
	indFinal integer default 1, -- 0=Normal; 1=Consumidor final;
	indPres integer default 1, -- 0=Não se aplica (por exemplo, Nota Fiscal complementar ou de ajuste); 1=Operação presencial; 2=Operação não presencial, pela Internet; 3=Operação não presencial, Teleatendimento; 4=NFC-e em operação com entrega a domicílio; 9=Operação não presencial, outros
	procEmi integer default 0,
	verProc varchar(20) default '1.0.000',
	indIEDest integer default 9,-- 1=Contribuinte ICMS (informar a IE do destinatário); 2=Contribuinte isento ICMS; 9=Não Contribuinte, que pode ou não possuir Inscrição Estadual
	value_ii numeric(9,2) default 0.00,
	value_ipi numeric(9,2) default 0.00,
	value_pis numeric(9,2) default 0.00,
	value_cofins numeric(9,2) default 0.00,
	value_icms numeric(9,2) default 0.00,
	value_icms_st numeric(9,2) default 0.00,
	value_issqn numeric(9,2) default 0.00,
	value_tax numeric(9,2) default 0.00,
	PRIMARY KEY(company,request),
	FOREIGN KEY(company,request) REFERENCES request(company, id),
	FOREIGN KEY(company,person) REFERENCES person(company, id),
	UNIQUE(company,request)
);

CREATE TABLE request_product (
	company integer references crud_company,
	request integer,
	id SERIAL NOT NULL,
	quantity numeric(9,3) DEFAULT 1.000 not null,
	value numeric(9,3) NOT NULL,
	value_item numeric(9,2) default 0.00,
	value_desc numeric(9,2) default 0.00,
	value_freight numeric(9,2) default 0.00, -- NFE
	cfop integer references nfe_cfop,
	tax integer references nfe_tax_group,
	value_all_tax numeric(9,2) default 0.00, -- NFE
	product integer references product,
	serials character varying(255),
	PRIMARY KEY(company,id),
	FOREIGN KEY(company,request) REFERENCES request(company, id)
);

CREATE TABLE request_payment (
	company integer references crud_company,
	request integer,
	id SERIAL NOT NULL,
	type integer references payment_type,
	value numeric(9,2) DEFAULT 0.000 NOT NULL,
	account integer,
	number character varying(16),
	due_date timestamp without time zone,
	payday timestamp without time zone,
	balance numeric(9,2) DEFAULT 0.000 NOT NULL,
	PRIMARY KEY(company,id),
	FOREIGN KEY(company,request) REFERENCES request(company, id),
	FOREIGN KEY(company,account) REFERENCES account(company, id)
);

CREATE TABLE stock (
	company integer references crud_company,
	id integer references product,
	count_in numeric(9,3) DEFAULT 0.000,
	count_out numeric(9,3) DEFAULT 0.000,
	estimed_in numeric(9,3),
	estimed_out numeric(9,3) DEFAULT 0.000,
	estimed_value numeric(9,3) DEFAULT 0.000,
	margin_sale numeric(9,3) DEFAULT 50.000, -- varejo
	margin_wholesale numeric(9,3) DEFAULT 25.000, -- atacado
	reserved_in numeric(9,3) DEFAULT 0.000,
	reserved_out numeric(9,3) DEFAULT 0.000,
	stock numeric(9,3) DEFAULT 0.000,
	stock_default numeric(9,3) DEFAULT 0.000,
	stock_minimal numeric(9,3) DEFAULT 0.000,
	stock_serials character varying(1024),
	sum_value_in numeric(9,3) DEFAULT 0.000,
	sum_value_out numeric(9,3) DEFAULT 0.000,
	sum_value_stock numeric(9,3) DEFAULT 0.000,
	value numeric(9,3) DEFAULT 0.000,
	value_wholesale numeric(9,3) DEFAULT 0.000, -- valor para venda em atacado/revendedores
	PRIMARY KEY(company,id)
);

-- select rp.id,rp.account,rp.payday,rp.due_date,GREATEST(rp.payday,rp.due_date) as rp_date,rp.type as rp_type,rp.value,r.type as r_type,rp.balance from request_payment rp,request r where r.id = rp.request order by rp.type,rp_date desc,rp.id desc;

CREATE OR REPLACE FUNCTION account_balance_after_date(_company integer, _account integer, date_ref timestamp, ref_id integer, diff numeric(9,2), request_type integer) RETURNS integer AS $account_balance_after_date$
DECLARE
BEGIN
  	IF request_type = 1 THEN
	  		diff := diff * (-1.0);
	  ELSEIF request_type = 2 THEN
	  END IF;

	  UPDATE request_payment SET balance = balance + diff
	  	WHERE
	  		company = _company AND
	  		account = _account AND
	  			(
	  			GREATEST(payday,due_date) > date_ref OR
	  			(GREATEST(payday,due_date) = date_ref AND id >= ref_id)
	  			);
	RETURN 1;
END;
$account_balance_after_date$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION request_payment_change() RETURNS TRIGGER AS $request_payment_change$
DECLARE
		request_rec RECORD;
BEGIN
		IF (TG_OP = 'INSERT') THEN
			  SELECT INTO request_rec * FROM request WHERE request.id = NEW.request;
				PERFORM account_balance_after_date(NEW.company, NEW.account, GREATEST(NEW.due_date, NEW.payday), NEW.id, NEW.value, request_rec.type);
				RETURN NEW;
		ELSEIF (TG_OP = 'UPDATE') THEN
			  SELECT INTO request_rec * FROM request WHERE request.id = OLD.request;
				PERFORM account_balance_after_date(OLD.company, OLD.account, GREATEST(OLD.due_date, OLD.payday), OLD.id, OLD.value * (-1.0), request_rec.type);
				PERFORM account_balance_after_date(NEW.company, NEW.account, GREATEST(NEW.due_date, NEW.payday), NEW.id, NEW.value, request_rec.type);
				RETURN NEW;
		ELSEIF (TG_OP = 'DELETE') THEN
			  SELECT INTO request_rec * FROM request WHERE request.id = OLD.request;
				PERFORM account_balance_after_date(OLD.company, OLD.account, GREATEST(OLD.due_date, OLD.payday), OLD.id, OLD.value * (-1.0), request_rec.type);
				RETURN OLD;
		END IF;

	RETURN NULL;
END;
$request_payment_change$ LANGUAGE plpgsql;

CREATE TRIGGER request_payment_trigger AFTER INSERT OR UPDATE OR DELETE ON request_payment FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE request_payment_change();

CREATE OR REPLACE FUNCTION request_payment_before() RETURNS TRIGGER AS $request_payment_before$
DECLARE
		rec_payment RECORD;
		date_new timestamp;
BEGIN
		date_new = GREATEST(NEW.payday,NEW.due_date);

		FOR rec_payment IN SELECT id,GREATEST(payday,due_date) as date_ref,balance FROM request_payment WHERE company = NEW.company AND account = NEW.account AND GREATEST(payday,due_date) <= date_new ORDER BY date_ref desc,id desc LIMIT 1 LOOP
			NEW.balance = rec_payment.balance;
		END LOOP;

		RETURN NEW;
END;
$request_payment_before$ LANGUAGE plpgsql;

CREATE TRIGGER request_payment_trigger_before BEFORE INSERT OR UPDATE ON request_payment FOR EACH ROW WHEN (pg_trigger_depth() = 0) EXECUTE PROCEDURE request_payment_before();

