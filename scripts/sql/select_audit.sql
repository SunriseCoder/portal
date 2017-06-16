USE `local-portal`;

   SELECT ae.id, CONCAT(u.id, ':', u.login) as user,
          ae.date,
          ae.ip,
          ot.name as operation,
          ot.severity,
          aet.name as event_type,
          aet.severity
     FROM audit_events ae
LEFT JOIN users u on u.id = ae.user_id
LEFT JOIN operation_types ot on ot.id = ae.operation_id
LEFT JOIN audit_event_types aet on aet.id = ae.type_id;