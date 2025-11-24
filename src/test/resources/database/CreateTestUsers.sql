-- Create the 4 test users for testing the application --

-- testadmin : 08+<064Nv7t!
-- teststudent : U+FNka79W1=8
-- testtutor : tYj0^0^Bf=T3
-- usermanager : Zj>6wH18m]0U

-- Insert the 4 test users
INSERT INTO pms_user (username, account_non_expired, account_non_locked, credentials_non_expired, email, enabled,
                      first_name, last_login, last_name, password, role, created)
VALUES ('testadmin', true, true, true, 'testadmin@pms.test', true, 'Test', '2025-01-01 00:00:00', 'Admin',
        '$2a$10$OoGn8IPZnGSaSrHfmMh9WOVKm1nTrYvyWOl7vmKgovHZvDpazPlkK', 'ROLE_ADMIN', '2025-01-01 00:00:00'),
       ('testtutor', true, true, true, 'testtutor@pms.test', true, 'Test', '2025-01-01 00:00:00', 'Tutor',
        '$2a$10$U3c1mphrpsGb67J6osW.kO/vl2vftdoHGFM5W5Wan8bgTc1kKlU6u', 'ROLE_TUTOR', '2025-01-01 00:00:00'),
       ('teststudent', true, true, true, 'teststudent@pms.test', true, 'Test', '2025-01-01 00:00:00', 'Student',
        '$2a$10$zDaTuFYnOhsLpr0m5txwq.bjFET29poVQbViuEMFsxSg7phSLJ532', 'ROLE_STUDENT', '2025-01-01 00:00:00'),
       ('usermanager', true, true, true, 'usermanager@pms.test', true, 'User', '2025-01-01 00:00:00', 'Manager',
        '$2a$10$sC8qG/IYd0TyKzPX26C9wuY/JCt22AqkjdKcfLIRuKF8C4deYYiYG', 'ROLE_USER_MANAGER', '2025-01-01 00:00:00');

-- Associate test users with their relevant role tables
INSERT INTO admin (username)
VALUES ('testadmin');
INSERT INTO tutor (username)
VALUES ('testtutor');
INSERT INTO student (username)
VALUES ('teststudent');
INSERT INTO user_manager (username)
VALUES ('usermanager');