
--select rp.id,rp.account,rp.payday,rp.due_date,GREATEST(rp.payday,rp.due_date) as rp_date,rp.type as rp_type,rp.value,r.type as r_type,rp.balance from request_payment rp,request r where r.id = rp.request order by rp.type,rp_date desc,rp.id desc;

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

CREATE TABLE IF NOT EXISTS account (
    company integer,
    id integer,
    account character varying(20),
    agency character varying(20),
    bank character varying(20),
    description character varying(255),
    PRIMARY KEY(company,id),
	unique(bank,agency,account)
);

CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name character varying(100) NOT NULL
);

CREATE TABLE category_company (
    company integer,
    id integer,
    category integer,
    PRIMARY KEY(company,id)
);

CREATE TABLE crud_company (
    id SERIAL PRIMARY KEY,
    name character varying(255) NOT NULL
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
    company integer NOT NULL,
    id integer NOT NULL,
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
    id integer PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL
);

CREATE TABLE person (
    company integer,
    id integer,
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
    id serial PRIMARY KEY,
    additional_data character varying(255),
    barcode varchar(13) unique,
    category integer NOT NULL,
    cl_fiscal character varying(16),
    departament character varying(64),
    description character varying(255),
    image_url character varying(255),
    manufacturer varchar(64), -- fabricante
    model character varying(255),
    name character varying(100) NOT NULL,
    tax_icms numeric(9,3) DEFAULT 0.000,
    tax_ipi numeric(9,3) DEFAULT 0.000,
    tax_iss numeric(9,3) DEFAULT 0.000,
    unit character varying(16),
    weight numeric(9,3) DEFAULT 0.000,
    unique(name,manufacturer,model,description)
);

CREATE TABLE request (
    company integer NOT NULL,
    id integer NOT NULL,
    additional_data character varying(255),
    date timestamp without time zone,
    payments_value numeric(19,2),
    person integer references person,
    products_value numeric(19,2),
    services_value numeric(19,2),
    state integer,
    sum_value numeric(19,2),
    transport_value numeric(19,2),
    type int DEFAULT 0 not null,-- compra,venda,conserto,fabricação,desmonte,orçamento,pedido               
    PRIMARY KEY(company,id)
);

CREATE TABLE request_payment (
    company integer NOT NULL,
    id integer NOT NULL,
    account integer references account,
    number character varying(16),
    payday timestamp without time zone,
    request integer references request,
    type integer,
    value numeric(9,3) NOT NULL,
    balance numeric(9,3) DEFAULT 0.000 NOT NULL,
    due_date timestamp without time zone,
    PRIMARY KEY(company,id)
);

CREATE TABLE request_product (
    company integer NOT NULL,
    id integer NOT NULL,
    containers int DEFAULT 1, -- quantidade de embalagens
    product integer references product,
    quantity numeric(9,3) DEFAULT 1.000 not null,
    request integer references request,
    serial character varying(64),
    space numeric(9,3) DEFAULT 1.000, -- volume
    tax_icms numeric(9,3) DEFAULT 0.000 not null,
    tax_ipi numeric(9,3) DEFAULT 0.000 not null,
    tax_upper_lower numeric(9,3) DEFAULT 0.000 not null,
    value numeric(9,3) NOT NULL,
    value_item numeric(9,3) NOT NULL,
    weight numeric(9,3) DEFAULT 0.000, -- peso líquido
    weight_final numeric(9,3) DEFAULT 0.000, -- peso bruto
    PRIMARY KEY(company,id),
    unique(request,product,serial)
);

CREATE TABLE request_state (
    id integer PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL,
    next integer,
    prev integer,
    stock_action integer,
    type integer
);

CREATE TABLE request_type (
    id integer PRIMARY KEY,
    description character varying(100),
    name character varying(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS stock (
    company integer,
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
    id integer PRIMARY KEY,
    name character varying(255) UNIQUE NOT NULL
);

INSERT INTO account (company, id, account, agency, bank, description) VALUES (4, 1, NULL, NULL, NULL, 'Caixa');
INSERT INTO account (company, id, account, agency, bank, description) VALUES (4, 2, NULL, NULL, NULL, 'Conta Bancária Principal');

INSERT INTO category (id, name) VALUES (1, 'categoria 1');
INSERT INTO category (id, name) VALUES (2, 'categoria 2');
INSERT INTO category (id, name) VALUES (3, 'mercado');

INSERT INTO category_company (company, id, category) VALUES (1, 1, 1);
INSERT INTO category_company (company, id, category) VALUES (1, 2, 2);
INSERT INTO category_company (company, id, category) VALUES (2, 1, 1);
INSERT INTO category_company (company, id, category) VALUES (2, 2, 2);
INSERT INTO category_company (company, id, category) VALUES (3, 1, 1);
INSERT INTO category_company (company, id, category) VALUES (3, 2, 2);
INSERT INTO category_company (company, id, category) VALUES (4, 3, 3);

INSERT INTO crud_company (id, name) VALUES (1, 'ADMIN');
INSERT INTO crud_company (id, name) VALUES (2, 'ENTERPRISE 1');
INSERT INTO crud_company (id, name) VALUES (3, 'ENTERPRISE 2');
INSERT INTO crud_company (id, name) VALUES (4, 'USO DOMÉSTICO');

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (1, '{"id":{"type":"i","hiden":true,"primaryKey":true},"menu":{},"name":{},"template":{},"title":{},"filterFields":{},"isOnLine":{"type":"b"},"fields":{"readOnly":true},"orderBy":{},"saveAndExit":{"type":"b"}}', 'id,name', NULL, 'admin', 'crudService', NULL, NULL, NULL, NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (2, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"required":true}}', 'id,name', NULL, 'admin', 'crudCompany', NULL, NULL, NULL, NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (3, '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"service":"crudCompany"},"name":{},"path":{},"password":{"type":"p"},"roles":{},"menu":{},"routes":{},"showSystemMenu":{"type":"b","defaultValue":"false"},"authctoken":{},"ip":{}}', 'id,name,company', NULL, 'admin', 'crudUser', false, NULL, NULL, NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (4, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"translation":{},"locale":{"defaultValue":"en-us"}}', 'id,name,locale,translation', NULL, 'admin', 'crudTranslation', true, NULL, NULL, NULL, NULL);

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (6, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"description":{}}', 'id,name', NULL, 'config', 'paymentType', true, NULL, 'Tipo de Pagamento', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (7, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"description":{}}', 'id,name', NULL, 'config', 'requestType', true, NULL, 'Tipo de Requisição', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (8, '{"id":{"type":"i","primaryKey":true,"hiden":true},"type":{"type":"i","service":"requestType","defaultValue":"1"},"name":{},"stockAction":{"service":"stockAction"},"prev":{"type":"i","service":"requestState","defaultValue":"0"},"next":{"type":"i","service":"requestState","defaultValue":"0"},"description":{}}', 'id,type,name,stockAction', NULL, 'config', 'requestState', true, NULL, 'Situação da Requisição', NULL, NULL);

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (9, '{"id":{"type":"i","primaryKey":true,"hiden":true},"barcode":{},"category":{"service":"category"},"name":{},"manufacturer":{},"model":{},"description":{},"additionalData":{},"unit":{},"clFiscal":{},"departament":{},"imageUrl":{},"weight":{"type":"n"},"taxIpi":{"type":"n"},"taxIcms":{"type":"n"},"taxIss":{"type":"n"}}', 'id,barcode,name', NULL, 'product', 'product', true, NULL, 'Produtos, Peças e Componentes', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (13, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{}}', 'id,name', NULL, 'config', 'stockAction', true, NULL, 'Ação sobre o Estoque', NULL, NULL);

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (14, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{},"phone":{},"cnpjCpf":{},"ieRg":{},"zip":{},"uf":{},"city":{},"district":{},"address":{},"fax":{},"email":{},"site":{},"additionalData":{},"credit":{"type":"n"}}', 'id,name,cnpjCpf,ieRg,phone', NULL, 'form', 'person', true, NULL, 'Cadastros de Clientes e Fornecedores', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (15, '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"type":"i","primaryKey":true,"service":"crudCompany"},"description":{},"bank":{},"agency":{},"account":{}}', 'id,description', NULL, 'config', 'account', true, NULL, 'Contas Bancárias', NULL, NULL);

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (17, '{"id":{"type":"i","hiden":true,"primaryKey":true},"company":{"type":"i","hiden":true,"primaryKey":true},"type":{"type":"i","hiden":false,"required":true,"readOnly":true,"service":"requestType"},"state":{"type":"i","required":true,"service":"requestState"},"person":{"type":"i","required":true,"service":"person"},"date":{"type":"datetime-local","required":true},"additionalData":{},"productsValue":{"defaultValue":"0.0","readOnly":true},"servicesValue":{"defaultValue":"0.0","readOnly":true},"transportValue":{"defaultValue":"0.0","readOnly":true},"sumValue":{"defaultValue":"0.0","readOnly":true},"paymentsValue":{"defaultValue":"0.0","readOnly":true}}', 'id,person,date', NULL, 'form', 'request', false, NULL, 'Requisições de Entrada e Saída', NULL, 'date desc,id desc');
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (19, '{"id":{"type":"i","hiden":true,"primaryKey":true},"company":{"type":"i","hiden":true,"primaryKey":true},"request":{"type":"i","service":"request"},"product":{"type":"i","required":true,"service":"product"},"quantity":{"type":"n3","defaultValue":"1.000","required":true},"value":{"type":"n3","defaultValue":"0.0","required":true},"valueItem":{"type":"n3","defaultValue":"0.0","required":true,"readOnly":true},"serial":{},"weight":{"type":"n3","hiden":true},"containers":{"type":"i","hiden":true},"weightFinal":{"type":"n3","hiden":true},"space":{"type":"n","hiden":true},"taxIpi":{"type":"n","hiden":true},"taxIcms":{"type":"n","hiden":true},"taxUpperLower":{"type":"n","hiden":true}}', 'id,product,serial,quantity,value', NULL, 'report', 'requestProduct', true, NULL, 'Entrada e Saída de Produtos', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (22, '{"id":{"type":"i","hiden":true,"primaryKey":true},"company":{"type":"i","hiden":true,"primaryKey":true},"request":{"type":"i","service":"request"},"type":{"type":"i","required":true,"service":"paymentType"},"account":{"type":"i","required":true,"service":"account"},"number":{},"value":{"type":"n2","required":true},"dueDate":{"type":"datetime-local","required":true},"payday":{"type":"datetime-local"},"balance":{"type":"n2","required":false,"readOnly":true}}', 'id,type,account,number', NULL, 'report', 'requestPayment', true, NULL, 'Pagamentos', NULL, 'due_date,id');

INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (23, '{"id":{"type":"i","primaryKey":true,"hiden":true,"service":"product"},"company":{"type":"i","primaryKey":true,"hiden":true},"value":{"type":"n"},"stock":{"type":"n"},"stockSerials":{},"stockDefault":{"type":"n"},"stockMinimal":{"type":"n"},"sumValueStock":{"type":"n"},"reservedOut":{"type":"n"},"reservedIn":{"type":"n"},"estimedOut":{"type":"n"},"marginSale":{"type":"n"},"marginWholesale":{"type":"n"},"estimedValue":{"type":"n"},"valueWholesale":{"type":"n"},"countIn":{"type":"n"},"countOut":{"type":"n"},"sumValueIn":{"type":"n"},"sumValueOut":{"type":"n"}}', 'id', NULL, 'stock', 'stock', true, NULL, 'Estoque de Produtos', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (26, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}', 'id,name', NULL, 'admin', 'category', false, NULL, 'Controle de Categorias de Produtos e Serviços', NULL, NULL);
INSERT INTO crud_service (id, fields, filter_fields, is_on_line, menu, name, save_and_exit, template, title, orderby, order_by) VALUES (27, '{"id":{"type":"i","primaryKey":true,"hiden":true},"company":{"primaryKey":true,"service":"crudCompany","required":true,"title":"Categorias Vinculadas","isClonable":true},"category":{"service":"category","required":true}}', NULL, NULL, 'admin', 'categoryCompany', true, NULL, 'Categorias de cada Empresa', NULL, NULL);

INSERT INTO crud_translation (id, locale, name, translation) VALUES (1, 'en-us', 'User', 'Usuário');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (2, 'en-us', 'Exit', 'Sair');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (3, 'en-us', 'New', 'Novo');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (4, 'en-us', 'Filter', 'Filtrar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (5, 'en-us', 'View', 'Visualizar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (6, 'en-us', 'Edit', 'Editar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (7, 'en-us', 'Delete', 'Apagar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (8, 'en-us', 'Actions', 'Ações');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (9, 'en-us', 'Cancel', 'Cancelar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (10, 'en-us', 'Create', 'Criar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (11, 'en-us', 'Save', 'Salvar');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (12, 'en-us', 'Save as New', 'Salvar como Novo');

INSERT INTO crud_translation (id, locale, name, translation) VALUES (13, 'en-us', 'Name', 'Nome');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (14, 'en-us', 'Description', 'Descrição');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (15, 'en-us', 'Category', 'Categoria');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (16, 'en-us', 'Date', 'Data');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (17, 'en-us', 'Unit', 'Unidade');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (18, 'en-us', 'Quantity', 'Quantidade');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (19, 'en-us', 'Value', 'Valor');

INSERT INTO crud_translation (id, locale, name, translation) VALUES (20, 'en-us', 'Type', 'Tipo');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (21, 'en-us', 'State', 'Situação');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (22, 'en-us', 'Person', 'Cliente/Fornecedor');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (23, 'en-us', 'Additional Data', 'Dados Adicionais');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (24, 'en-us', 'Products Value', 'Valor Produtos');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (25, 'en-us', 'Services Value', 'Valor Serviços');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (26, 'en-us', 'Transport Value', 'Valor Transporte');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (27, 'en-us', 'Sum Value', 'Valor Total');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (28, 'en-us', 'Payments Value', 'Valor Faturas');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (29, 'en-us', 'Product', 'Produto');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (30, 'en-us', 'Serial', 'N. Série');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (31, 'en-us', 'Defect', 'Defeito');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (32, 'en-us', 'Service', 'Serviço');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (33, 'en-us', 'Employed', 'Funcionário');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (34, 'en-us', 'Tax Iss', 'Taxa ISS');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (35, 'en-us', 'Barcode', 'Código de Barras');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (36, 'en-us', 'Manufacturer', 'Fabricante');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (37, 'en-us', 'Model', 'Modelo');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (38, 'en-us', 'Departament', 'Departamento');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (39, 'en-us', 'Image Url', 'URL da imagem');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (40, 'en-us', 'Weight', 'Peso');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (41, 'en-us', 'Tax Ipi', 'Taxa IPI');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (42, 'en-us', 'Tax Icms', 'Taxa ICMS');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (43, 'en-us', 'Phone', 'Telefone');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (44, 'en-us', 'Zip', 'CEP');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (45, 'en-us', 'City', 'Cidade');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (46, 'en-us', 'District', 'Bairro');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (47, 'en-us', 'Address', 'Endereço');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (48, 'en-us', 'Credit', 'Credito');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (49, 'en-us', 'Number', 'Numero');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (50, 'en-us', 'Number Form', 'Numero do Formulário');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (51, 'en-us', 'Date Fiscal', 'Data Fiscal');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (52, 'en-us', 'Request', 'Requisição');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (53, 'en-us', 'Account', 'Conta');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (54, 'en-us', 'Due Date', 'Data Vencimento');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (55, 'en-us', 'Payday', 'Data do Pagamento');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (56, 'en-us', 'Pay By', 'Pago Por');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (57, 'en-us', 'Hourly Pay Value', 'Custo por hora');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (58, 'en-us', 'Bank', 'Banco');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (59, 'en-us', 'Agency', 'Agência');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (60, 'en-us', 'Stock', 'Estoque');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (61, 'en-us', 'Count In', 'Quantidade Entrada');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (62, 'en-us', 'Count Out', 'Quantidade Saída');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (63, 'en-us', 'Stock Default', 'Estoque Ideal');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (64, 'en-us', 'Stock Minimal', 'Estoque Mínimo');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (65, 'en-us', 'Reserved Out', 'Saída Reservada');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (66, 'en-us', 'Reserved In', 'Entrada Reservada');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (67, 'en-us', 'Estimed Out', 'Estimativa de Saída');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (68, 'en-us', 'Sum Value In', 'Soma dos Valores de Entrada');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (69, 'en-us', 'Sum Value Out', 'Soma dos Valores de Saída');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (70, 'en-us', 'Sum Value Stock', 'Soma dos Valores em Estoque');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (71, 'en-us', 'Margin Sale', 'Margem de Venda');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (72, 'en-us', 'Margin Wholesale', 'Margem de Revenda');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (73, 'en-us', 'Estimed Value', 'Valor Estimado');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (74, 'en-us', 'Value Wholesale', 'Valor Revenda');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (75, 'en-us', 'Stock Serials', 'Seriais em Estoque');
INSERT INTO crud_translation (id, locale, name, translation) VALUES (76, 'en-us', 'Search', 'Localizar');

INSERT INTO crud_user (company, id, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (1, 1, '706a6f68-0590-f2c2-6263-e08941278039', '127.0.0.1', NULL, 'admin', '123456', '/app/crud_service/search', '{"crudCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudService":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudUser":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudTranslation":{"read":true,"query":true,"create":true,"update":true,"delete":true},"category":{"read":true,"query":true,"create":true,"update":true,"delete":true},"categoryCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true},"paymentType":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestType":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestState":{"read":true,"query":true,"create":true,"update":true,"delete":true},"stockAction":{"read":true,"query":true,"create":true,"update":true,"delete":true},"account":{"read":true,"query":true,"create":true,"update":true,"delete":true},"person":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]', '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]');
INSERT INTO crud_user (company, id, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES (4, 4, '73cfef54-4231-c43b-d896-29b48ffad47f', '127.0.0.1', '{"buy":{"menu":"actions","label":"Compra","path":"request/new?type=1&state=10"},"requestPayment":{"menu":"form","label":"Financeiro","path":"request_payment/search"},"stock":{"menu":"form","label":"Estoque","path":"stock/search"},"product":{"menu":"form","label":"Produtos","path":"product/search"},"person":{"menu":"form","label":"Clientes e Fornecedores","path":"person/search"},"requests":{"menu":"form","label":"Requisições","path":"request/search"},"account":{"menu":"form","label":"Contas","path":"account/search"}}', 'spending', '123456', '/app/request/search', '{"crudCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudService":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudTranslation":{"read":true,"query":true,"create":false,"update":false,"delete":false},"crudUser":{"read":true,"query":true,"create":false,"update":false,"delete":false},"category":{"read":true,"query":true,"create":false,"update":false,"delete":false},"categoryCompany":{"read":true,"query":true,"create":false,"update":false,"delete":false},"account":{"read":true,"query":true,"create":true,"update":false,"delete":false},"requestType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"requestState":{"read":true,"query":true,"create":false,"update":false,"delete":false},"stockAction":{"read":true,"query":true,"create":false,"update":false,"delete":false},"paymentType":{"read":true,"query":true,"create":false,"update":false,"delete":false},"person":{"read":true,"query":true,"create":true,"update":true,"delete":false},"product":{"read":true,"query":true,"create":true,"update":true,"delete":false},"request":{"read":true,"query":true,"create":true,"update":true,"delete":false},"requestProduct":{"read":true,"query":true,"create":true,"update":true,"delete":true},"requestPayment":{"read":true,"query":true,"create":true,"update":true,"delete":true},"stock":{"read":true,"query":true,"create":true,"update":true,"delete":false}}', false, NULL, '[{"path":"/app/request/:action","controller":"RequestController"}]', '[{"path": "/app/request/:action", "controller": "RequestController"}]');

INSERT INTO payment_type (id, description, name) VALUES (1, NULL, 'Dinheiro');
INSERT INTO payment_type (id, description, name) VALUES (2, NULL, 'Boleto');
INSERT INTO payment_type (id, description, name) VALUES (4, NULL, 'Cheque');
INSERT INTO payment_type (id, description, name) VALUES (8, NULL, 'Cartão de débito');
INSERT INTO payment_type (id, description, name) VALUES (16, NULL, 'Cartão de crédito à vista');
INSERT INTO payment_type (id, description, name) VALUES (32, NULL, 'Cartão de crédito parcelado lojista');
INSERT INTO payment_type (id, description, name) VALUES (64, NULL, 'Cartão de crédito parcelado emissor');
INSERT INTO payment_type (id, description, name) VALUES (128, NULL, 'Carteira');

-- compra
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (10, NULL, 'Solicitar Orçamento', NULL, NULL, 16, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (20, NULL, 'Aguardando Resposta', NULL, 10, 16, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (30, NULL, 'Recusado, orçar em outro fornecedor', NULL, 20, NULL, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (40, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 30, NULL, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (50, NULL, 'Aprovado, enviar resposta', NULL, 10, 4, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (60, NULL, 'Aprovado, aguardando entrega', 80, 50, 4, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (70, NULL, 'Aprovado, aguardando retirada', 80, 50, 4, 1);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (80, NULL, 'Aprovado e Concluído', NULL, NULL, 1, 1);

-- venda
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (210, NULL, 'Montar Orçamento', NULL, NULL, NULL, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (220, NULL, 'Montando Orçamento', NULL, 210, NULL, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (230, NULL, 'Enviar Orçamento', NULL, 220, NULL, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (240, NULL, 'Aguardando Resposta', NULL, 230, NULL, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (250, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 240, NULL, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (260, NULL, 'Aprovado, dar andamento', NULL, 240, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (270, NULL, 'Aprovado, aguardando peças', NULL, 260, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (280, NULL, 'Aprovado, efetuar serviços', NULL, 260, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (290, NULL, 'Aprovado, efetuando serviços', NULL, 280, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (300, NULL, 'Aprovado, aguardando entrega', 320, 290, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (310, NULL, 'Aprovado, aguardando retirada', 320, 290, 8, 2);
INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES (320, NULL, 'Aprovado e Concluído', NULL, NULL, 2, 2);

INSERT INTO request_type (id, description, name) VALUES (1, NULL, 'Compra');
INSERT INTO request_type (id, description, name) VALUES (2, NULL, 'Venda');

INSERT INTO stock_action (id, name) VALUES (1, 'countIn');
INSERT INTO stock_action (id, name) VALUES (2, 'countOut');

