#
#  This upgrade script creates a new SCARAB_NOTIFICATION_RULE table and
#  adds the content from the current SCARAB_NOTIFICATION_FILTER table
#  It also supplies a new entry into ID_TABLE for automatic creation of
#  unique RUL_ID entries.
#  
#  Created by: Hussayn Dabbous hussayn.dabbous@saxess.de
# 
# IMPORTANT NOTE: If you encounter problems during the conversion,
# most probably your database used a mix of MyIsam- and InnoDb- tables.
# The problem is that you can not define foreign keys between tables
# which are created on different engines.
#
# One possible solution is (worked for me) :
#
# - Dump your database
# - edit the dumpfile and change all occurances of "engine=MyIsam" to
#   engine="InnoDB"
# - recreate your database with the modified dumpfile.
# - Apply this script again. Now it should work.
#
# If you run this script multiple times, you will see errors:
#
#    $ mysql -f asp < upgrade-0.23-notification.sql
#    ERROR 1060 (42S21) at line 81: Duplicate column name 'USER_ID'
#    ERROR 1005 (HY000) at line 85: Can't create table '.\asp\#sql-20c_23.frm' (errno: 121)
#    ERROR 1062 (23000) at line 94: Duplicate entry '48' for key 1
#
# The errors indicate that the script has been processed before. If you see
# different erors, probably something else is going on. Check that!!!
#
# You may want to run the script using "mysql -f scarab < upgrade-0.23-notification.sql"
# 
# Check that everything is OK after you run the script:
#
# 1.) The table SCARAB_CONDITION has a new Column USER_ID
# 2.) The table SCARAB_NOTIFICATION_RULE exists and has got
#     its content copied from SCARAB_NOTIFICATION_FILTER
#
# If these conditions are met, the upgrade has bee successfull.
# -----------------------------------------------------------------------
# SCARAB_NOTIFICATION_RULE
# -----------------------------------------------------------------------
drop table if exists SCARAB_NOTIFICATION_RULE;

CREATE TABLE SCARAB_NOTIFICATION_RULE
(
    RULE_ID       INTEGER NOT NULL AUTO_INCREMENT,
    MODULE_ID     INTEGER NOT NULL,
    USER_ID       INTEGER NOT NULL,
    ACTIVITY_TYPE VARCHAR(30) NOT NULL,
    MANAGER_ID    INTEGER default 0 NOT NULL,
    FILTER_STATE  INTEGER default 0 NOT NULL,
    SEND_SELF     INTEGER default 0 NOT NULL,
    SEND_FAILURES INTEGER default 0 NOT NULL,
    PRIMARY KEY(RULE_ID));

# -------------------------------------------------------------------------------
#copy the data from SCARAB_NOTIFICATION_FILTER
# -------------------------------------------------------------------------------
insert into SCARAB_NOTIFICATION_RULE (MODULE_ID, USER_ID, ACTIVITY_TYPE, MANAGER_ID, FILTER_STATE, SEND_SELF, SEND_FAILURES)
	select MODULE_ID, USER_ID, ACTIVITY_TYPE, MANAGER_ID, FILTER_STATE, SEND_SELF, SEND_FAILURES from SCARAB_NOTIFICATION_FILTER;


# -------------------------------------------------------------------------------
#remove the auto-increment
# -------------------------------------------------------------------------------
ALTER TABLE SCARAB_NOTIFICATION_RULE CHANGE RULE_ID RULE_ID INTEGER NOT NULL;

ALTER TABLE SCARAB_NOTIFICATION_RULE
    ADD CONSTRAINT SCARAB_NOTIFICATION_RULE_FK_1
    FOREIGN KEY (USER_ID)
    REFERENCES TURBINE_USER(USER_ID)
    ;
ALTER TABLE SCARAB_NOTIFICATION_RULE
    ADD CONSTRAINT SCARAB_NOTIFICATION_RULE_FK_2
    FOREIGN KEY (MODULE_ID)
    REFERENCES SCARAB_MODULE(MODULE_ID)
    ;

ALTER TABLE SCARAB_CONDITION
    ADD ( USER_ID INTEGER )
    ;

ALTER TABLE SCARAB_CONDITION
    ADD ( OPERATOR INTEGER NOT NULL DEFAULT 0 )
    ;

ALTER TABLE SCARAB_CONDITION
    ADD CONSTRAINT SCARAB_CONDITION_FK_6
    FOREIGN KEY (USER_ID)
    REFERENCES TURBINE_USER (USER_ID)
    ;
   
# -------------------------------------------------------------------------------
# create additional ID_TABLE entry 
# -------------------------------------------------------------------------------
insert into ID_TABLE (id_table_id, table_name, next_id, quantity)
 select 48, 'SCARAB_NOTIFICATION_RULE', count(*) + 1, 10 from SCARAB_NOTIFICATION_FILTER;


# -------------------------------------------------------------------------------
# drop the old NOTIFICATION_TABLE 
# For the moment keep table SCARAB_NOTIFICATION_FILTER as a backup.
# Remove NOTIFICATION_FILTER in the next release
# -------------------------------------------------------------------------------
#drop table SCARAB_NOTIFICATION_FILTER;
