/*
 * This script adds a scope_id foreign key to the saved records table.  It also
 * renames the global scope to module.
 *
 * Created By: John McNally 
 * $Id$
 */

alter table SCARAB_REPORT add SCOPE_ID INTEGER(11); 

alter table SCARAB_REPORT add FOREIGN KEY (SCOPE_ID) REFERENCES SCARAB_SCOPE (SCOPE_ID); 

update SCARAB_SCOPE set SCOPE_NAME='module' where SCOPE_NAME='global';