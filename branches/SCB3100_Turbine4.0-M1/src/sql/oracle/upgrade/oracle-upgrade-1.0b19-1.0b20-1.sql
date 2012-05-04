/*
 * b19->b20: Key generation was dependent on scarab.http.domain up to
 *      release b19.
 *      Now it is dependent on scarab.instance.id.
 *      This script updates the relevant DB-tables accordingly.
 *
 */
UPDATE ID_TABLE 
       SET TABLE_NAME = REPLACE(TABLE_NAME,'${scarab.http.domain}','${scarab.instance.id}')
       WHERE TABLE_NAME LIKE '${scarab.http.domain}%';       
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='${scarab.instance.id}' where NAME='${scarab.http.domain}';
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='${scarab.instance.id}' where NAME='module-domain';
UPDATE SCARAB_GLOBAL_PARAMETER set VALUE='@SCARAB.HTTP.PORT@'   where NAME='scarab.http.port';
UPDATE SCARAB_MODULE set DOMAIN='${scarab.instance.id}';
UPDATE SCARAB_ISSUE  set ID_DOMAIN='${scarab.instance.id}';
