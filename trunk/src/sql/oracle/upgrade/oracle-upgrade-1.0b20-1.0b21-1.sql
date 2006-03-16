--
-- Adds the field to describe ActivityType for every activity record.
--
ALTER TABLE SCARAB_ACTIVITY ADD ACTIVITY_TYPE varchar(30) null;

--
-- Adds the field to mark an issue as 'moved'. The 'deleted' will now be
-- used just for the deletions.
--
ALTER TABLE SCARAB_ISSUE ADD MOVED NUMBER(1) default 0;

--
-- Move the 'deleted' values into 'moved' field for every existing issue.
--
update SCARAB_ISSUE SET MOVED=DELETED;

--
-- Reset the 'deleted' field for every existing issue ('deleted' is a new state)
--
update SCARAB_ISSUE SET DELETED=0;