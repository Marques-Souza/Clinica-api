CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    perfil VARCHAR(25) NOT NULL,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    cep VARCHAR(8) NOT NULL,
    street VARCHAR(150) NOT NULL,
    neighborhood VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    number VARCHAR(10) NOT NULL,
    user_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_clients_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS doctors (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    crm VARCHAR(9) NOT NULL UNIQUE,
    specialty VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    user_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS specialty_config (
    id UUID PRIMARY KEY,
    specialty VARCHAR(255) NOT NULL UNIQUE,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    consultation_fee NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    payment_status VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    reason VARCHAR(500),
    duration_minutes INTEGER NOT NULL,
    appointment_code VARCHAR(17) NOT NULL UNIQUE,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES clients (id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
);
