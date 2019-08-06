INSERT INTO crud_service (name, menu, title, fields) VALUES ('account', 'requestPayment', 'Contas Bancárias', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('person', 'person', 'Cadastros de Clientes e Fornecedores', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('product', 'product', 'Produtos, Peças e Componentes', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('barcode', 'product', 'Código de Barras de fornecedores de produtos', '{"product":{"isClonable":false,"title":"Código de Barras dos fornecedores"}}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestType', 'request', 'Tipo de Requisição', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('stockAction', 'requestProduct', 'Ação sobre o Estoque', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestState', 'request', 'Situação da Requisição', '{"name":{"shortDescription":true},"type":{"shortDescription":true}}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('request', 'request', 'Requisições de Entrada e Saída', '{"date":{"orderIndex":1,"sortType":"desc"},"type":{"tableVisible":false}}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestNfe', 'report', 'Nota Fiscal Eletrônica', '{"indpag":{"options":"0,1,2"},"iddest":{"options":"1,2,3"},"cdv":{"hiden":true},"indfinal":{"options":"0,1"},"indpres":{"options":"0,1,2,3,4,9"}}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestFreight', 'report', 'Frete', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestProduct', 'report', 'Entrada e Saída de Produtos', '{}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('requestPayment', 'report', 'Pagamentos', '{"balance":{"readOnly":true}}');
INSERT INTO crud_service (name, menu, title, fields) VALUES ('stock', 'requestProduct', 'Estoque de Produtos', '{"id":{"hiden":false,"isClonable":false,"title":"Estoque"}}');

UPDATE crud_service SET save_and_exit = false WHERE name = 'product';

INSERT INTO request_type (id, description, name) VALUES (1, NULL, 'Compra');
INSERT INTO request_type (id, description, name) VALUES (2, NULL, 'Venda');

INSERT INTO stock_action (id, name) VALUES (1, 'countIn');
INSERT INTO stock_action (id, name) VALUES (2, 'countOut');
INSERT INTO stock_action (id, name) VALUES (4, 'reservedIn');
INSERT INTO stock_action (id, name) VALUES (8, 'reservedOut');
INSERT INTO stock_action (id, name) VALUES (16, 'estimedIn');
INSERT INTO stock_action (id, name) VALUES (32, 'estimedOut');

INSERT INTO request_state (id, description, name, next, prev, stock_action, type) VALUES
(10, NULL, 'Solicitar Orçamento', NULL, NULL, 16, 1),
(20, NULL, 'Aguardando Resposta', NULL, 10, 16, 1),
(30, NULL, 'Recusado, orçar em outro fornecedor', NULL, 20, NULL, 1),
(40, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 30, NULL, 1),
(50, NULL, 'Aprovado, dar prosseguimento', NULL, 10, 4, 1),
(60, NULL, 'Aprovado, aguardando entrega', 80, 50, 4, 1),
(70, NULL, 'Aprovado, aguardando retirada', 80, 50, 4, 1),
(80, NULL, 'Aprovado e Concluído', NULL, NULL, 1, 1),

(210, NULL, 'Montar Orçamento', NULL, NULL, NULL, 2),
(220, NULL, 'Montando Orçamento', NULL, 210, NULL, 2),
(230, NULL, 'Enviar Orçamento', NULL, 220, NULL, 2),
(240, NULL, 'Aguardando Resposta', NULL, 230, NULL, 2),
(250, NULL, 'Recusado, efetuado em outro fornecedor', NULL, 240, NULL, 2),
(260, NULL, 'Aprovado, dar andamento', NULL, 240, 8, 2),
(270, NULL, 'Aprovado, aguardando peças', NULL, 260, 8, 2),
(280, NULL, 'Aprovado, efetuar serviços', NULL, 260, 8, 2),
(290, NULL, 'Aprovado, efetuando serviços', NULL, 280, 8, 2),
(300, NULL, 'Aprovado, aguardando entrega', 320, 290, 8, 2),
(310, NULL, 'Aprovado, aguardando retirada', 320, 290, 8, 2),
(320, NULL, 'Aprovado e Concluído', NULL, NULL, 2, 2);

INSERT INTO crud_translation (locale, name, translation) VALUES
('pt-br', 'Date', 'Data'),
('pt-br', 'Unit', 'Unidade'),
('pt-br', 'Quantity', 'Quantidade'),
('pt-br', 'Value', 'Valor'),
('pt-br', 'Type', 'Tipo'),
('pt-br', 'State', 'Situação'),
('pt-br', 'Person', 'Cliente/Fornecedor'),
('pt-br', 'Additional Data', 'Dados Adicionais'),
('pt-br', 'Products Value', 'Valor Produtos'),
('pt-br', 'Services Value', 'Valor Serviços'),
('pt-br', 'Transport Value', 'Valor Transporte'),
('pt-br', 'Sum Value', 'Valor Total'),
('pt-br', 'Payments Value', 'Valor Faturas'),
('pt-br', 'Product', 'Produto'),
('pt-br', 'Serial', 'N. Série'),
('pt-br', 'Defect', 'Defeito'),
('pt-br', 'Barcode', 'Código de Barras'),
('pt-br', 'Manufacturer', 'Fabricante'),
('pt-br', 'Model', 'Modelo'),
('pt-br', 'Weight', 'Peso'),
('pt-br', 'Phone', 'Telefone'),
('pt-br', 'Zip', 'CEP'),
('pt-br', 'City', 'Cidade'),
('pt-br', 'District', 'Bairro'),
('pt-br', 'Address', 'Endereço'),
('pt-br', 'Credit', 'Credito'),
('pt-br', 'Number', 'Numero'),
('pt-br', 'Number Form', 'Numero do Formulário'),
('pt-br', 'Date Fiscal', 'Data Fiscal'),
('pt-br', 'Request', 'Requisição'),
('pt-br', 'Account', 'Conta'),
('pt-br', 'Due Date', 'Data Vencimento'),
('pt-br', 'Payday', 'Data do Pagamento'),
('pt-br', 'Pay By', 'Pago Por'),
('pt-br', 'Bank', 'Banco'),
('pt-br', 'Agency', 'Agência'),
('pt-br', 'Stock', 'Estoque'),
('pt-br', 'Count In', 'Quantidade Entrada'),
('pt-br', 'Count Out', 'Quantidade Saída'),
('pt-br', 'Stock Default', 'Estoque Ideal'),
('pt-br', 'Stock Minimal', 'Estoque Mínimo'),
('pt-br', 'Reserved Out', 'Saída Reservada'),
('pt-br', 'Reserved In', 'Entrada Reservada'),
('pt-br', 'Estimed Out', 'Estimativa de Saída'),
('pt-br', 'Sum Value In', 'Soma dos Valores de Entrada'),
('pt-br', 'Sum Value Out', 'Soma dos Valores de Saída'),
('pt-br', 'Sum Value Stock', 'Soma dos Valores em Estoque'),
('pt-br', 'Margin Sale', 'Margem de Venda'),
('pt-br', 'Margin Wholesale', 'Margem de Revenda'),
('pt-br', 'Estimed Value', 'Valor Estimado'),
('pt-br', 'Value Wholesale', 'Valor Revenda'),
('pt-br', 'Stock Serials', 'Seriais em Estoque');

INSERT INTO crud_group_owner (name) VALUES ('HOME');

INSERT INTO crud_group (name) VALUES ('Mercado');

INSERT INTO crud_user (crud_group_owner, name, password, path, menu, roles, show_system_menu, routes) SELECT id, 'spending', '123456', 'request/search', '{"buy":{"menu":"actions","label":"Compra","path":"request/new?overwrite={\"type\":1,\"state\":10}"},"sale":{"menu":"actions","label":"Venda","path":"request/new?overwrite={\"type\":2,\"state\":10}"},"requestPayment":{"menu":"form","label":"Financeiro","path":"request_payment/search"},"stock":{"menu":"form","label":"Estoque","path":"stock/search"},"product":{"menu":"form","label":"Produtos","path":"product/search"},"person":{"menu":"form","label":"Clientes e Fornecedores","path":"person/search"},"requests":{"menu":"form","label":"Requisições","path":"request/search"},"account":{"menu":"form","label":"Contas","path":"account/search"}}', '{"crudTranslation":{},"crudGroupOwner":{},"crudGroup":{},"nfeCfop":{},"bacenCountry":{},"ibgeUf":{},"ibgeCity":{},"ibgeCnae":{},"nfeTaxGroup":{},"camexNcm":{},"account":{"create":true},"stockAction":{},"requestType":{},"requestState":{},"paymentType":{},"person":{"create":true,"update":true},"nfeStIcmsOrigem":{},"product":{"create":true,"update":true},"barcode":{"create":true,"update":true},"request":{"create":true,"update":true},"requestProduct":{"create":true,"update":true,"delete":true},"requestPayment":{"create":true,"update":true,"delete":true},"stock":{"create":true,"update":true}}', false, '[{"path":"/app/request/:action","controller":"erp/RequestController"}]' FROM crud_group_owner WHERE name='HOME';

INSERT INTO crud_group_user (crud_user, crud_group) SELECT u.id, g.id FROM crud_user AS u, crud_group AS g WHERE u.name='spending' AND g.name='Mercado';
