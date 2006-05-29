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

ALTER TABLE SCARAB_R_MODULE_USER_ATTRIBUTE 
  ADD (INTERNAL_ATTRIBUTE VARCHAR(20) NULL);

alter table SCARAB_R_MODULE_USER_ATTRIBUTE 
  drop constraint SCARAB_R_MODULE_USER_ATTRI_U_1;

alter table SCARAB_R_MODULE_USER_ATTRIBUTE
  add constraint SCARAB_R_MODULE_USER_ATTRI_U_1 unique (LIST_ID, MODULE_ID, USER_ID, ISSUE_TYPE_ID, ATTRIBUTE_ID, INTERNAL_ATTRIBUTE)
  using index;
  
/*
 * Add new column to SCARAB_ISSUE with LAST_TRANS_ID, setting its value to
 * the older activity ID related to every issue
 *
 */

alter table SCARAB_ISSUE add LAST_TRANS_ID NUMBER(20);

alter table SCARAB_ISSUE
  add constraint SCARAB_ISSUE_FK_4 foreign key (LAST_TRANS_ID)
  references SCARAB_TRANSACTION (TRANSACTION_ID);


UPDATE SCARAB_ISSUE i set i.last_trans_id = (
  SELECT max(a.transaction_id) LAST_TRANS_ID
  FROM SCARAB_ACTIVITY a
  where a.issue_id = i.issue_id
);



