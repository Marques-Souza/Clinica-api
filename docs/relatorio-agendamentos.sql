SELECT
    c.cpf AS paciente_cpf,
    c.full_name AS paciente,
    c.phone AS telefone,
    a.appointment_code AS codigo_agendamento,
    d.full_name AS medico,
    d.crm AS crm,
    d.specialty AS especialidade,
    a.scheduled_at AS data_hora,
    a.duration_minutes AS duracao_minutos,
    a.reason AS motivo,
    a.status AS status_agendamento,
    a.consultation_fee AS valor_consulta,
    a.payment_method AS forma_pagamento,
    a.payment_status AS status_pagamento
FROM appointments a
INNER JOIN clients c ON c.id = a.patient_id
INNER JOIN doctors d ON d.id = a.doctor_id
WHERE c.cpf = 'PREENCHA_COM_CPF'
  AND a.scheduled_at >= '2026-01-01 00:00:00'
  AND a.scheduled_at <  '2027-01-01 00:00:00'
ORDER BY a.scheduled_at DESC