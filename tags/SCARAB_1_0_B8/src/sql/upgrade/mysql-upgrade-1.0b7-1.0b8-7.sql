/*
 * This upgrade script adds an ATTACHMENT_ID column to the SCARAB_ACTIVITY table.
 *
 * Created By: Jon Scott Stevens
 * $Id$
 */

ALTER TABLE SCARAB_ACTIVITY ADD column ATTACHMENT_ID integer null default 0;
