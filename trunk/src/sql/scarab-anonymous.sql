-- Create an account for system administrator (also used for initial
-- data population, etc.).
-- Remember to set a good password for this user in a production system!

INSERT INTO TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE) 
    VALUES (9, '@ANONYMOUS_USERNAME@', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Scarab', 'anonymous', 'anonymous@scarab.example.org', 'CONFIRMED');


-- Script to fill the tables with default roles and permissions

INSERT INTO TURBINE_ROLE (ROLE_ID, ROLE_NAME) VALUES (8, 'Anonymous');

insert into TURBINE_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
       select  TURBINE_ROLE.ROLE_ID, TURBINE_PERMISSION.PERMISSION_ID
         from  TURBINE_ROLE, TURBINE_PERMISSION
         where TURBINE_ROLE.ROLE_NAME = 'Anonymous'
           and TURBINE_PERMISSION.PERMISSION_NAME in (
                  'Issue | Search',
                  'Issue | View')
;

-- Assign the user '@ANONYMOUS_USERNAME@' a system-wide role 'Anonymous'

INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID from 
TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = '@ANONYMOUS_USERNAME@' AND 
TURBINE_ROLE.ROLE_NAME in ('Anonymous');