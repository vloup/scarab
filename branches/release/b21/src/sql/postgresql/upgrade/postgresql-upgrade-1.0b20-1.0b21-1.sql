---
--- b20-b21 Migration.
---
--- Creates new fields to allow for the deletion of issues
--- Creates new permissions for adding comments and deleting issues
---
 
---
--- Adds the field to describe ActivityType for every activity record.
---
ALTER TABLE SCARAB_ACTIVITY ADD ACTIVITY_TYPE varchar(30) null;

---
--- Adds the field to mark an issue as 'moved'. The 'deleted' will now be
--- used just for the deletions.
---
ALTER TABLE SCARAB_ISSUE ADD MOVED INT2;

---
--- Move the 'deleted' values into 'moved' field.
---
update SCARAB_ISSUE SET MOVED=DELETED;

---
--- Reset the 'deleted' issue.
---
update SCARAB_ISSUE SET DELETED=0;

---
--- Insert new permissions (delete & comment)
---
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (21, 'Issue | Comment');
INSERT INTO TURBINE_PERMISSION (PERMISSION_ID, PERMISSION_NAME) VALUES (20, 'Issue | Delete');
