/*
 * This upgrade script removes the SCARAB_ISSUE_ATTRIBUTE_VOTE table.
 * The table was not used by the application, so there should be no
 * data related changes.
 *
 * Created By: John McNally
 * $Id$
 */

drop table if exists SCARAB_ISSUE_ATTRIBUTE_VOTE;

