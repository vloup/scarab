/*
 * This upgrade script increases the length of 
 * SCARAB_ATTACHMENT.ATTACHMENT_MIME_TYPE column.  
 * 25 is not long enough, don't know what is maximum, so go with 255.
 *
 * Created By: John McNally
 * $Id$ */

alter table SCARAB_ATTACHMENT change ATTACHMENT_MIME_TYPE ATTACHMENT_MIME_TYPE varchar(255) NOT NULL; 
