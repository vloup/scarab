/*
 * This upgrade script moves the renames the DEFAULT_SUBSCRIPTION_FREQUENCY_ID 
 * column.  Some databases choke on the longer name.
 *
 * Created By: John McNally
 * $Id$
 */

alter table SCARAB_QUERY change DEFAULT_SUBSCRIPTION_FREQUENCY_ID SUBSCRIPTION_FREQUENCY_ID int(11); 

