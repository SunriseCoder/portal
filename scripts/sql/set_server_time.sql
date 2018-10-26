SET @@global.time_zone = '+03:00';
SET @@session.time_zone = '+03:00';

SELECT @@global.time_zone, @@session.time_zone;

select now();
