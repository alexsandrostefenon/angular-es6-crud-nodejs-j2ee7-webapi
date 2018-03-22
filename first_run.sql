CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name character varying(100) NOT NULL
);

CREATE TABLE crud_company (
    id SERIAL PRIMARY KEY,
    name character varying(255) NOT NULL
);

CREATE TABLE category_company (
    company integer references crud_company,
    id SERIAL,
    category integer references category,
    PRIMARY KEY(company,id)
);

CREATE TABLE IF NOT EXISTS account (
    company integer references crud_company,
    id SERIAL,
    account character varying(20),
    agency character varying(20),
    bank character varying(20),
    description character varying(255),
    PRIMARY KEY(company,id),
	unique(bank,agency,account)
);

CREATE TABLE crud_service (
    id SERIAL PRIMARY KEY,
    fields character varying(10240),
    filter_fields character varying(10240),
    is_on_line boolean,
    menu character varying(255),
    name character varying(255),
    save_and_exit boolean,
    template character varying(255),
    title character varying(255),
    orderby character varying(512),
    order_by character varying(512)
);

CREATE TABLE crud_translation (
    id SERIAL PRIMARY KEY,
    locale character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    translation character varying(255)
);

CREATE TABLE crud_user (
    company integer references crud_company,
    id SERIAL,
    authctoken character varying(255),
    ip character varying(255),
    menu character varying(10240),
    name character varying(255),
    password character varying(255),
    path character varying(255),
    roles character varying(10240),
    show_system_menu boolean,
    config character varying(10240),
    routes character varying(10240),
    routes_jsonb jsonb,
    PRIMARY KEY(company,id)
);

CREATE TABLE payment_type (
    id SERIAL PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL
);

CREATE TABLE person (
    company integer references crud_company,
    id SERIAL,
    additional_data character varying(255),
    address character varying(100),
    city character varying(64),
	cnpj_cpf varchar(18) unique,
	credit numeric(9,3) DEFAULT 0.000,
    district character varying(64),
    email character varying(100),
    fax character varying(16),
	ie_rg varchar(12) unique,
	name varchar(100) unique not null,
    phone character varying(16),
    site character varying(100),
    uf character varying(2),
    zip character varying(9),
    PRIMARY KEY(company,id)
);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    category integer references category,
    cl_fiscal character varying(16),
    departament character varying(64),
    name character varying(100) NOT NULL,
    model character varying(255),
    unit character varying(16),
    weight numeric(9,3) DEFAULT 0.000,
    image_url character varying(255),
    description character varying(255),
    additional_data character varying(255),
    tax_ipi numeric(9,3) DEFAULT 0.000,
    tax_icms numeric(9,3) DEFAULT 0.000,
    tax_iss numeric(9,3) DEFAULT 0.000,
    unique(name,manufacturer,model,description)
);

CREATE TABLE barcode (
    barcode varchar(13) PRIMARY KEY,
    manufacturer varchar(64), -- fabricante
    product integer references product
);

CREATE TABLE request (
    company integer references crud_company,
    id SERIAL NOT NULL,
    additional_data character varying(255),
    date timestamp without time zone,
    payments_value numeric(19,2),
    person integer,
    products_value numeric(19,2),
    services_value numeric(19,2),
    state integer,
    sum_value numeric(19,2),
    transport_value numeric(19,2),
    type int DEFAULT 0 not null,-- compra,venda,conserto,fabricação,desmonte,orçamento,pedido
    PRIMARY KEY(company,id),
    FOREIGN KEY(company, person) REFERENCES person(company, id)
);

CREATE TABLE request_payment (
    company integer references crud_company,
    id SERIAL NOT NULL,
    account integer,
    number character varying(16),
    payday timestamp without time zone,
    request integer,
    type integer,
    value numeric(9,3) NOT NULL,
    balance numeric(9,3) DEFAULT 0.000 NOT NULL,
    due_date timestamp without time zone,
    PRIMARY KEY(company,id),
    FOREIGN KEY(company,request) REFERENCES request(company, id),
    FOREIGN KEY(company,account) REFERENCES account(company, id)
);

CREATE TABLE request_product (
    company integer references crud_company,
    id SERIAL NOT NULL,
    containers int DEFAULT 1, -- quantidade de embalagens
    product integer references product,
    quantity numeric(9,3) DEFAULT 1.000 not null,
    request integer,
    serial character varying(64),
    space numeric(9,3) DEFAULT 1.000, -- volume
    tax_icms numeric(9,3) DEFAULT 0.000,
    tax_ipi numeric(9,3) DEFAULT 0.000,
    tax_upper_lower numeric(9,3) DEFAULT 0.000,
    value numeric(9,3) NOT NULL,
    value_item numeric(9,3) NOT NULL,
    weight numeric(9,3) DEFAULT 0.000, -- peso líquido
    weight_final numeric(9,3) DEFAULT 0.000, -- peso bruto
    PRIMARY KEY(company,id),
    FOREIGN KEY(company,request) REFERENCES request(company, id),
    unique(request,product,serial)
);

CREATE TABLE request_state (
    id SERIAL PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL,
    next integer,
    prev integer,
    stock_action integer,
    type integer
);

CREATE TABLE request_type (
    id SERIAL PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL
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

CREATE TABLE stock_action (
    id SERIAL PRIMARY KEY,
    name character varying(255) UNIQUE NOT NULL
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

