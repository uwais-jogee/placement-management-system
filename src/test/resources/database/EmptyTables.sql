-- Delete all data from all tables in the database --

DELETE FROM admin WHERE username != '';
DELETE FROM message WHERE id != '';
DELETE FROM user_manager WHERE username != '';
DELETE FROM user_notifications WHERE notification_id != '';
DELETE FROM notification WHERE id != '';
DELETE FROM visit WHERE id != '';
DELETE FROM placement WHERE id != '';
DELETE FROM placement_auth_request WHERE id != '';
DELETE FROM placement_evaluation WHERE id != '';
DELETE FROM company WHERE id != '';
DELETE FROM address WHERE place_id != '';
DELETE FROM student WHERE username != '';
DELETE FROM tutor WHERE username != '';
DELETE FROM pms_user WHERE username != '';
DELETE FROM email_token WHERE token != '';