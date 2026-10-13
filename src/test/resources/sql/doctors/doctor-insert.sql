insert into users (id, email, password, perfil)
values('550e8400-e29b-41d4-a716-446655440000', 'support@gmail.com', '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'ADMIN');

insert into users (id, email, password, perfil)
values('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'karla@gmail.com', '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'USER');

insert into users (id, email, password, perfil)
values('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'kaio@gmail.com', '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'DOCTOR');

insert into users (id, email, password, perfil)
values('123e4567-e89b-12d3-a456-426614174000', 'ana.medica@gmail.com', '$2a$12$.3nyflpxjA93BXc/VhkTAezhJTJj0wbUsNUaOBnQ/OGBtb6Q/jeie', 'DOCTOR');

insert into doctors (id, full_name, cpf, crm, specialty, active, user_id)
values('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Kaio Medico', '52998224725', '123456-SP', 'CARDIOLOGY', true, '6ba7b810-9dad-11d1-80b4-00c04fd430c8');
