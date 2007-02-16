/*
 * This upgrade script shortens the name of a long index name, so that
 * it will work for oracle as well as mysql.
 *
 * Created By: John McNally (originally by Matthew Stahl)
 * $Id$
 */

DROP INDEX IX_ISSUE_MODULE_PREFERRED_ORDER ON SCARAB_ATTRIBUTE_GROUP;
CREATE INDEX IX_ISSUE_MODULE_PREF_ORDER ON SCARAB_ATTRIBUTE_GROUP 
       (ISSUE_TYPE_ID, MODULE_ID, PREFERRED_ORDER);

