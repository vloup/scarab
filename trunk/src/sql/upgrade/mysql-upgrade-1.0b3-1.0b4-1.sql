/*
 * This upgrade script removes a few tables that were not used by the 
 * application, so there should be no data related changes.
 *
 * Created By: John McNally
 * $Id$
 */

drop table if exists SCARAB_ISSUE_ATTRIBUTE_VOTE;
drop table if exists SCARAB_R_ATTRIBUTE_VALUE_WORD;
drop table if exists SCARAB_WORD;