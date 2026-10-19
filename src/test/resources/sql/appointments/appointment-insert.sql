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

insert into doctors (id, full_name, cpf, crm, specialty, active, user_id)
values
    ('b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Kaio Medico', '52998224725', '123456-SP', 'CARDIOLOGY', true, '6ba7b810-9dad-11d1-80b4-00c04fd430c8');

insert into specialty_config (id, specialty, consultation_fee, duration_minutes)
values
    ('c3d4e5f6-a7b8-9012-cdef-345678901234', 'CARDIOLOGY', 250.00, 30);

insert into appointments
(id, scheduled_at, reason, status, payment_status, payment_method, appointment_code, consultation_fee, duration_minutes, patient_id, doctor_id)
values
    ('d4e5f6a7-b8c9-0123-def0-456789012345',
     '2026-12-01 10:00:00', 'Consulta de rotina', 'SCHEDULED', 'PENDING', 'CREDIT_CARD', '20261201-a1b2c3d4', 250.00, 30,
     'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
     'b2c3d4e5-f6a7-8901-bcde-f23456789012');

insert into clients (id, full_name, cpf, phone, cep, street, neighborhood, city, state, number, user_id)
values ('e1a2b3c4-d5f6-7890-abcd-ef1234567890', 'Joao Silva', '12345678909', '11999999999', '01001000', 'Rua Teste', 'Centro', 'Sao Paulo', 'SP', '101', '123e4567-e89b-12d3-a456-426614174000');
