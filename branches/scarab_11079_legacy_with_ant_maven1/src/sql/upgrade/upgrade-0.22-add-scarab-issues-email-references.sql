/*
 * Adds a field to record MessageIDs for every email sent relating to changes to an Issue.
 *
 */
ALTER TABLE SCARAB_ISSUE ADD (EMAIL_REFERENCES VARCHAR(2000));