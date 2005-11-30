/**
  * Adds the field to describe ActivityType for every activity record.
  */
ALTER TABLE SCARAB_ACTIVITY ADD ACTIVITY_TYPE varchar(30) null;
