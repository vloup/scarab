/*
 * This script removes the date and float attribute types as they are not
 * handled properly in the schema or code.
 *
 * Created By: John McNally 
 * $Id$
 */

delete from SCARAB_ATTRIBUTE_TYPE where ATTRIBUTE_TYPE_ID=2;
delete from SCARAB_ATTRIBUTE_TYPE where ATTRIBUTE_TYPE_ID=4;