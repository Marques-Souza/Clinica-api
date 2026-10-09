
insert into users (id, email, password, perfil)
values
    ('550e8400-e29b-41d4-a716-446655440000', 'support@gmail.com',
     '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'ADMIN');

insert into users (id, email, password, perfil)
values
    ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'karla@gmail.com',
     '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'USER');

insert into users (id, email, password, perfil)
values
    ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'kaio@gmail.com',
     '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'DOCTOR');

insert into users (id, email, password, perfil)
values
    ('123e4567-e89b-12d3-a456-426614174000', 'joao@gmail.com',
     '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'USER');

insert into clients
(id, full_name, cpf, phone, cep, street, neighborhood, city, state, number, user_id)
values
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890',
     'Karla Santos', '98765432100', '11987654321', '01001000',
     'Praça da Sé', 'Sé', 'São Paulo', 'SP', '100',
     'f47ac10b-58cc-4372-a567-0e02b2c3d479');
