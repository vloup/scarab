/*
 * Sample user
 */
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (2, 'jon@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Jon', 'Stevens', 'jon@latchkey.com', 'CONFIRMED' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (3, 'jss@latchkey.com', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Jon', 'Stevens', 'jon@latchkey.com', 'abcdef' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (4, 'jmcnally@collab.net', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'John', 'McNally', 'jmcnally@collab.net', 'CONFIRMED' );
insert into TURBINE_USER (USER_ID, LOGIN_NAME, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL, CONFIRM_VALUE ) 
    values (5, 'elicia@collab.net', 'NWoZK3kTsExUV00Ywo1G5jlUKKs=', 'Elicia', 'David', 'elicia@collab.net', 'CONFIRMED' );


/*
 * Sample Project
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, DOMAIN, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(1, 'Pacman JVM', 'PAC', 'testinst', 'Sample project', '/PacmanJVM/', 0, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, DOMAIN, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(3, 'Turbine', 'TBN', 'testinst', 'The Turbine Project', '/Turbine/', 0, 2, 2);

/*
 * Sample Component
 */

insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(2, 'Docs', 'PACD', 'Documentation', '/PacmanJVM/docs/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(5, 'Source', 'PACS', 'Source', '/PacmanJVM/source/', 1, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(4, 'Docs', 'TBND', 'Documentation', '/Turbine/docs/', 3, 2, 2);
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_CODE, MODULE_DESCRIPTION, MODULE_URL, PARENT_ID, OWNER_ID, QA_CONTACT_ID) 
    values(6, 'Source', 'TBNS', 'Source', '/Turbine/source/', 3, 2, 2);

/*
 * id_table entries for the module_codes
 */
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PAC', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PACD', 2, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('PACS', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBN', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBND', 1, 1);
insert into ID_TABLE (table_name, next_id, quantity) VALUES ('TBNS', 1, 1);


/*
 * Module 2.
 * module_id, attr_id, display_value, active, required, preferred order, 
 * dedupe, quick_search
 */
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,1);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,2);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,3);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,4);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,5);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,6);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,7);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,9);
#insert into SCARAB_R_MODULE_ATTRIBUTE(MODULE_ID, ATTRIBUTE_ID) values(2,10);
#INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (2,11,NULL,1,1,1,0,0);


/*
 * Insert some values for project 5
 * module_id, attr_id, display_value, active, required, preferred order, 
 * dedupe, quick_search
 */
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,1,'Description',1,1,100,0,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,2,'Assigned To',0,0,200,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,3,'Status',1,0,300,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,4,'Resolution',1,0,400,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,5,'Platform',1,1,2,1,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES 
    (5,6,'Operating System',1,1,3,1,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,7,'Priority',1,0,500,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,8,'Vote',1,0,600,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,9,'Severity',1,0,700,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,10,'Tracking',0,0,800,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,11,'Summary',1,1,1,1,1);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (5,12,'Functional Area',1,0,1000,0,0);

/*
 * Insert a relationship between user_id 2 and module_id 5
 * Insert a relationship between user_id 4 and module_id 5
 * Insert a relationship between user_id 5 and module_id 5
 */
INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID 
from TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = 'jon@latchkey.com'
AND SCARAB_MODULE.MODULE_ID = 5 
AND TURBINE_ROLE.ROLE_NAME = 'Developer';

INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID 
from TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = 'jmcnally@collab.net'
AND SCARAB_MODULE.MODULE_ID = 5 
AND TURBINE_ROLE.ROLE_NAME = 'Developer';

INSERT INTO TURBINE_USER_GROUP_ROLE ( USER_ID, GROUP_ID, ROLE_ID ) 
SELECT TURBINE_USER.USER_ID, SCARAB_MODULE.MODULE_ID, TURBINE_ROLE.ROLE_ID 
from TURBINE_USER, SCARAB_MODULE, TURBINE_ROLE 
WHERE TURBINE_USER.LOGIN_NAME = 'elicia@collab.net'
AND SCARAB_MODULE.MODULE_ID = 5 
AND TURBINE_ROLE.ROLE_NAME = 'Developer';


/*
 * Sample Issues
 */

insert into SCARAB_ISSUE(ISSUE_ID, MODULE_ID, ID_PREFIX, ID_COUNT, CREATED_BY) values (1, 5, 'PACD', 1,5);

/* description */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, VALUE) values (1, 1, 1, 'Documents are not as current as they should be.');
/* summary */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, VALUE) values (2, 1, 11, 'Docs are out of date.');
/* status is New */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (4, 1, 3, 2, 'New');
/* platform is SGI */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (6, 1, 5, 21, 'SGI');
/* os is Linux */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (7, 1, 6, 38, 'Linux');
/* priority is p3 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (8, 1, 7, 56, 'Low');
/* severity is major */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (9, 1, 9, 66, 'major');



insert into SCARAB_ISSUE(ISSUE_ID, MODULE_ID, ID_PREFIX, ID_COUNT, CREATED_BY) values (2, 2, 'PACS', 1, 5);
/* description */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, VALUE) values (10, 2, 1, 'Items do not display correctly.');
/* summary */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, VALUE) values (11, 2, 11, 'Dates display in long form instead of short form.');
/* assigned to visitor id 1 */
#insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, USER_ID, VALUE) values (12, 2, 2, 1, 'jon');
/* status is New */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (13, 2, 3, 2, 'New');
/* resolution is verified */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (14, 2, 4, 6, 'verified');
/* platform is SGI */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (15, 2, 5, 20, 'PC');
/* os is OpenVMS */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (16, 2, 6, 75, 'Windows');
/* priority is p3 */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (17, 2, 7, 58, 'High');
/* severity is major */
insert into SCARAB_ISSUE_ATTRIBUTE_VALUE(VALUE_ID, ISSUE_ID, ATTRIBUTE_ID, OPTION_ID, VALUE) values (18, 2, 9, 65, 'normal');


/* make this issue a child issue of issue 1 */
insert into SCARAB_DEPEND values (1, 2, 3, "No");

