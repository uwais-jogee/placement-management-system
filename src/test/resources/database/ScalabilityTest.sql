DELIMITER $$

CREATE PROCEDURE generate_dummy_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE placement_status VARCHAR(50);

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO address (place_id, formatted_address)
            VALUES (CONCAT('place_', i), CONCAT('Formatted Address ', i));
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO pms_user (
                username, account_non_expired, account_non_locked, created,
                credentials_non_expired, email, enabled, first_name, last_login,
                last_name, password, role, account_activation_email_token_token,
                password_reset_email_token_token
            )
            VALUES (
                       CONCAT('student_', i), b'1', b'1', '2025-01-01 00:00:00', b'1',
                       CONCAT('student_', i, '@example.com'), b'1', CONCAT('Student', i),
                       NULL, 'User', '$2a$10$nYXot4NdkdbhquJdxTF7ZO5yITFCd3YS6qZ7sIb5gAtyqfZyUpGFu', 'ROLE_STUDENT', NULL, NULL
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO pms_user (
                username, account_non_expired, account_non_locked, created,
                credentials_non_expired, email, enabled, first_name, last_login,
                last_name, password, role, account_activation_email_token_token,
                password_reset_email_token_token
            )
            VALUES (
                       CONCAT('tutor_', i), b'1', b'1', '2025-01-01 00:00:00', b'1',
                       CONCAT('tutor_', i, '@example.com'), b'1', CONCAT('Tutor', i),
                       NULL, 'User', '$2a$10$nYXot4NdkdbhquJdxTF7ZO5yITFCd3YS6qZ7sIb5gAtyqfZyUpGFu', 'ROLE_TUTOR', NULL, NULL
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO pms_user (
                username, account_non_expired, account_non_locked, created,
                credentials_non_expired, email, enabled, first_name, last_login,
                last_name, password, role, account_activation_email_token_token,
                password_reset_email_token_token
            )
            VALUES (
                       CONCAT('admin_', i), b'1', b'1', '2025-01-01 00:00:00', b'1',
                       CONCAT('admin_', i, '@example.com'), b'1', CONCAT('Admin', i),
                       NULL, 'User', '$2a$10$nYXot4NdkdbhquJdxTF7ZO5yITFCd3YS6qZ7sIb5gAtyqfZyUpGFu', 'ROLE_ADMIN', NULL, NULL
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO student (username)
            VALUES (CONCAT('student_', i));
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO tutor (username)
            VALUES (CONCAT('tutor_', i));
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO admin (username)
            VALUES (CONCAT('admin_', i));
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 100 DO
            INSERT INTO company (company_name, industry, web_address, address_place_id)
            VALUES (
                       CONCAT('Company ', i),
                       CONCAT('Industry ', i),
                       CONCAT('http://company', i, '.com'),
                       CONCAT('place_', ((i - 1) MOD 100) + 1)
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 500 DO
            INSERT INTO placement_auth_request (
                company_contact_email, company_contact_name, company_contact_phone, company_formatted_address,
                company_industry, company_name_other, company_place_id, company_select, company_web_address,
                hours_per_week, internation_student, personal_adjustments, placement_end_date, placement_start_date,
                programme_of_study, rejection_reason, remote, requested_on, residential_arrangements, role_description,
                role_title, salary, status, student_phone, travel_arrangements, visa_status, company_id, email_token_token, student_username
            )
            VALUES (
                       CONCAT('contact_', i, '@company.com'),
                       CONCAT('Contact ', i),
                       '123-456-7890',
                       CONCAT('Address for Company ', i),
                       CONCAT('Industry ', i),
                       CONCAT('Alternate Company ', i),
                       CONCAT('place_', ((i - 1) MOD 100) + 1),
                       'Select Option',
                       CONCAT('http://companyauthreq_', i, '.com'),
                       40,
                       'No',
                       'None',
                       '2025-12-31',
                       '2025-01-01',
                       CONCAT('Programme ', i),
                       NULL,
                       'No',
                       '2025-01-01',
                       'None',
                       CONCAT('Role description ', i),
                       CONCAT('Role Title ', i),
                       50000 + i,
                       'APPROVED',
                       '555-1234',
                       'None',
                       'Valid',
                       ((i - 1) MOD 100) + 1,
                       NULL,
                       CONCAT('student_', ((i - 1) MOD 100) + 1)
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 300 DO
            INSERT INTO placement_evaluation (
                evaluation_date, feedback_rating, industry_skills_rating, overall_rating,
                recommendation_rating, resources_rating, soft_skills_rating, support_rating,
                training_rating, work_environment_rating, company_id
            )
            VALUES (
                       '2025-06-01',
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 5) + 1,
                       ((i - 1) MOD 100) + 1
                   );
            SET i = i + 1;
        END WHILE;

    SET i = 1;
    WHILE i <= 500 DO
            IF i <= 300 THEN
                SET placement_status = 'COMPLETED';
            ELSEIF i <= 400 THEN
                SET placement_status = 'IN_PROGRESS';
            ELSE
                SET placement_status = 'UPCOMING';
            END IF;

            INSERT INTO placement (
                end_date, start_date, status, company_id, placement_auth_request_id, placement_evaluation_id,
                student_username, tutor_username
            )
            VALUES (
                       '2025-12-31',
                       '2025-01-01',
                       placement_status,
                       ((i - 1) MOD 100) + 1,
                       i,
                       CASE
                           WHEN i <= 300 THEN i
                           ELSE NULL
                           END,
                       CONCAT('student_', ((i - 1) MOD 100) + 1),
                       CONCAT('tutor_', ((i - 1) MOD 100) + 1)
                   );
            SET i = i + 1;
        END WHILE;

END$$

DELIMITER ;

CALL generate_dummy_data();
drop procedure generate_dummy_data;