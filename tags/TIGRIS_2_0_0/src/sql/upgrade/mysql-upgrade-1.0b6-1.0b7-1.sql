/*
 * This upgrade script renames the combo-box attribute type name to be
 * Dropdown list.
 *
 * Created By: Jon Scott Stevens
 * $Id$
 */

update SCARAB_ATTRIBUTE_TYPE 
    set ATTRIBUTE_TYPE_NAME='Dropdown list'
    where ATTRIBUTE_TYPE_NAME='combo-box';
