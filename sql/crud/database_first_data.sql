INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudService', 'admin', NULL, NULL, NULL, '{}');
INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudGroupOwner', 'admin', NULL, NULL, NULL, '{}');
INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudUser', 'admin', false, NULL, NULL, '{}');
INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudTranslation', 'admin', true, NULL, NULL, '{}');
INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudGroup', 'admin', false, NULL, 'Controle de Categorias de Produtos e Serviços', '{}');
INSERT INTO crud_service (name, menu, save_and_exit, is_on_line, title, fields) VALUES ('crudGroupUser', 'admin', true, NULL, 'Grupos de cada Usuário', '{"crudUser":{"isClonable":true,"title":"Categorias Vinculadas"}}');

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

INSERT INTO crud_group_owner (name) VALUES ('ADMIN');

INSERT INTO crud_user (crud_group_owner, name, password) SELECT id,'admin','admin' FROM crud_group_owner WHERE name='ADMIN';

UPDATE crud_user SET
path='crud_service/search',
roles='{"crudService":{"create":true,"update":true,"delete":true},"crudGroupOwner":{"create":true,"update":true,"delete":true},"crudUser":{"create":true,"update":true,"delete":true},"crudTranslation":{"create":true,"update":true,"delete":true},"crudGroup":{"create":true,"update":true,"delete":true},"crudGroupUser":{"create":true,"update":true,"delete":true}}',
show_system_menu=true,
routes='[{"path": "/app/crud_service/:action", "controller": "crud/CrudServiceController"}, {"path": "/app/crud_user/:action", "controller": "crud/UserController"}]' 
WHERE name='admin';
