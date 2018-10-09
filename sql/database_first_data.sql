INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudService', 'admin', NULL, NULL, 'id,name', NULL, NULL, NULL, '{"name":{"type":"s","primaryKey":true,"required":true},"title":{"type":"s"},"template":{"type":"s"},"menu":{"type":"s"},"saveAndExit":{"type":"b"},"filterFields":{"type":"s"},"orderBy":{"type":"s"},"isOnLine":{"type":"b"},"fields":{"type":"s","readOnly":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudCompany', 'admin', NULL, NULL, 'id,name', NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudUser', 'admin', NULL, false, 'id,name,company', NULL, NULL, NULL, '{"company":{"type":"i","hiden":true,"primaryKey":true,"service":"crudCompany","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s"},"path":{"type":"s"},"menu":{"type":"s"},"roles":{"type":"s"},"showSystemMenu":{"type":"b","defaultValue":"false"},"routes":{"type":"s"},"authctoken":{"type":"s"},"ip":{"type":"s"},"config":{"type":"s"},"password":{"type":"p"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('crudTranslation', 'admin', NULL, true, 'id,name,locale,translation', NULL, NULL, NULL, '{"id":{"type":"i","primaryKey":true,"hiden":true},"locale":{"type":"s","defaultValue":"pt-br","required":true},"name":{"type":"s","required":true},"translation":{"type":"s"}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('category', 'admin', NULL, false, 'id,name', NULL, NULL, 'Controle de Categorias de Produtos e Serviços', '{"id":{"type":"i","primaryKey":true,"hiden":true},"name":{"type":"s","required":true}}');
INSERT INTO crud_service (name, menu, template, save_and_exit, filter_fields, order_by, is_on_line, title, fields) VALUES ('categoryCompany', 'admin', NULL, true, 'id,category,company', NULL, NULL, 'Categorias de cada Empresa', '{"company":{"type":"i","hiden":true,"primaryKey":true,"isClonable":true,"service":"crudCompany","title":"Categorias Vinculadas","required":true},"id":{"type":"i","primaryKey":true,"hiden":true},"category":{"type":"i","required":true,"service":"category"}}');

INSERT INTO crud_company (id, name) VALUES
(1, 'ADMIN')
;


INSERT INTO crud_user (company, authctoken, ip, menu, name, password, path, roles, show_system_menu, config, routes, routes_jsonb) VALUES
(1, '', '', NULL, 'admin', 'admin', 'crud_service/search', '{"crudService":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudUser":{"read":true,"query":true,"create":true,"update":true,"delete":true},"crudTranslation":{"read":true,"query":true,"create":true,"update":true,"delete":true},"category":{"read":true,"query":true,"create":true,"update":true,"delete":true},"categoryCompany":{"read":true,"query":true,"create":true,"update":true,"delete":true}}', true, NULL, '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]', '[{"path": "/app/crud_service/:action", "controller": "CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "UserController"}]')
;

INSERT INTO crud_translation (locale, name, translation) VALUES
('pt-br', 'User', 'Usuário'),
('pt-br', 'Exit', 'Sair'),
('pt-br', 'New', 'Novo'),
('pt-br', 'Filter', 'Filtrar'),
('pt-br', 'Search', 'Localizar'),
('pt-br', 'View', 'Visualizar'),
('pt-br', 'Edit', 'Editar'),
('pt-br', 'Delete', 'Apagar'),
('pt-br', 'Actions', 'Ações'),
('pt-br', 'Cancel', 'Cancelar'),
('pt-br', 'Create', 'Criar'),
('pt-br', 'Save', 'Salvar'),
('pt-br', 'Save as New', 'Salvar como Novo'),
('pt-br', 'Name', 'Nome'),
('pt-br', 'Description', 'Descrição'),
('pt-br', 'Category', 'Categoria')
;

