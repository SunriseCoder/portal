USE `local-portal`;

   SELECT ae.id, CONCAT(u.id, ':', u.login) as user,
          ae.date,
          ae.ip,
          ot.name as operation,
          ot.severity,
          aet.name as event_type,
          aet.severity,
          ae.object_before,
          ae.object_after,
          ae.error
     FROM audit_events ae
LEFT JOIN users u on u.id = ae.user_id
LEFT JOIN e_operation_types ot on ot.id = ae.operation_id
LEFT JOIN e_audit_event_types aet on aet.id = ae.type_id
 ORDER BY ae.date DESC;

select * from operation_types;
select * from audit_event_types;
