/*
 * This upgrade script adds a new column to the SCARAB_ATTRIBUTE_GROUP table.
 *
 * Created By: Hussayn Dabbous
 */
ALTER TABLE SCARAB_ATTRIBUTE_GROUP add ( EDIT_ROLE_ID INTEGER default -1);
