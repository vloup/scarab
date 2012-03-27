/*
 * b19->b20: Key generation was dependent on scarab.http.domain up to
 *      release b19.
 *      Now it is dependent on scarab.instance.id.
 *      This script updates the relevant DB-tables accordingly.
 *
 * Create the new table that defines the transitions between different values of a
 * dropdown list attribute.
 */
UPDATE ID_TABLE 
       SET TABLE_NAME = REPLACE(TABLE_NAME,'@SCARAB.HTTP.DOMAIN@','@SCARAB.INSTANCE.ID@')
       WHERE TABLE_NAME LIKE '@SCARAB.HTTP.DOMAIN@%';       
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='@SCARAB.INSTANCE.ID@' where NAME='@SCARAB.HTTP.DOMAIN@';
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='@SCARAB.INSTANCE.ID@' where NAME='module-domain';
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='@SCARAB.HTTP.PORT@'    where NAME='scarab.http.port';
UPDATE SCARAB_MODULE set DOMAIN='@SCARAB.INSTANCE.ID@';
UPDATE SCARAB_ISSUE  set ID_DOMAIN='@SCARAB.INSTANCE.ID@';
