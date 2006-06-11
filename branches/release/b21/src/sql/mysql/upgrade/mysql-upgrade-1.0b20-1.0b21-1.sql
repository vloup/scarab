--
-- b20-b21 Migration.
--
-- Creates new fields to allow deleting issues
-- Creates new permissions for commenting and deleting issues
--
 
-- 
--  Adds the field to describe ActivityType for every activity record.
--  
ALTER TABLE SCARAB_ACTIVITY ADD ACTIVITY_TYPE varchar(30) null;

-- 
--  Adds the field to mark an issue as 'moved'. The 'deleted' will now be
--  used just for the deletions.
--  
ALTER TABLE SCARAB_ISSUE ADD MOVED INTEGER(1);

-- 
--  Move the 'deleted' values into 'moved' field.
--  
update SCARAB_ISSUE SET MOVED=DELETED;

-- 
--  Reset the 'deleted' issue.
--  
update SCARAB_ISSUE SET DELETED=0;

-- 
--  Insert new permissions (delete & comment)
--  
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (21, 'Issue | Comment');
 
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (20, 'Issue | Delete');

--
-- Grant the new 'comment' (21) permission to anyone previously having 'edit' (1)
-- permission
--
create table scarab_b20_b21_perms
select role_id, 21 as permission_id from turbine_role_permission where permission_id = 1;

insert into turbine_role_permission select role_id, permission_id from scarab_b20_b21_perms;

drop table scarab_b20_b21_perms;
