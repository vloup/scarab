/*
 * Add columns to ISSUE_TYPE
 *
 * $Id$
 *
 * Created By: Elicia David
 * Modified for Oracle By: Thierry Lach
 */

ALTER TABLE SCARAB_ISSUE_TYPE ADD ISDEFAULT NUMBER (1) default 0;
