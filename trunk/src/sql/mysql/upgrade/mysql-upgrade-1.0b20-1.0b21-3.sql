/*
 * b20-b21 Migration.
 *
 * SCB1565: Include 'last modified' and 'creation' information in search results
 *  - Modifications on SCARAB_R_MODULE_USER_ATTRIBUTE
 *  - Dump and recreate SCARAB_ISSUE with the new field (LAST_TRANS_ID) loaded
 *
 */

/*
 * SCB1565: Creation and Last Modification date and users in Query Results
 *
 * - Add new column to SCARAB_R_MODULE_USER_ATTRIBUTE and modify the unique constraint
 *
 */

ALTER TABLE SCARAB_R_MODULE_USER_ATTRIBUTE ADD (INTERNAL_ATTRIBUTE VARCHAR(20) NULL);

DROP INDEX LIST_ID ON SCARAB_R_MODULE_USER_ATTRIBUTE;

CREATE UNIQUE INDEX LIST_ID ON SCARAB_R_MODULE_USER_ATTRIBUTE (LIST_ID,MODULE_ID,USER_ID,ISSUE_TYPE_ID,ATTRIBUTE_ID,INTERNAL_ATTRIBUTE);

/*
 * Add new column to SCARAB_ISSUE with LAST_TRANS_ID, setting its value to
 * the older activity ID related to every issue
 *
 * A backup table (SCARAB_ISSUE_BACKUP_b20_b21) is created
 *
 */

CREATE TABLE SCARAB_ISSUE_BACKUP_b20_b21
SELECT * FROM SCARAB_ISSUE;

DROP TABLE SCARAB_ISSUE;

CREATE TABLE SCARAB_ISSUE
SELECT i.ISSUE_ID, i.ID_PREFIX, i.ID_COUNT, i.ID_DOMAIN, i.TYPE_ID, i.MODULE_ID, i.CREATED_TRANS_ID,
max(a.transaction_id) LAST_TRANS_ID, i.DELETED, i.MOVED
FROM SCARAB_ISSUE_BACKUP_b20_b21 i LEFT OUTER JOIN SCARAB_ACTIVITY a ON (i.ISSUE_ID = a.ISSUE_ID)
GROUP BY i.ISSUE_ID;
