/*
 * This upgrade script adds two columns to the SCARAB_QUERY table.
 *
 * Created By: Jon Scott Stevens
 * $Id$
 */

ALTER TABLE SCARAB_QUERY ADD column HOME_PAGE int(1) null default 0;
ALTER TABLE SCARAB_QUERY ADD column PREFERRED_ORDER integer null default 0;
