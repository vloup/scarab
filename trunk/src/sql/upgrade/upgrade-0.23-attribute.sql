/*
 * Added more fields (release 0.23)
 * Style     : used as style and or class markup for display
 * Format    : regular expression which the input must conform too
 * Field size: size of displayfield (not field length!)
 * Hint      : example input as viusal hint for user entries
 *
 * Creates the fields that support the enhanced formatting capabilities
 * of user defined Attributes
 *
 */

ALTER TABLE SCARAB_ATTRIBUTE_OPTION ADD ( STYLE VARCHAR(255) );

ALTER TABLE SCARAB_ATTRIBUTE ADD ( STYLE VARCHAR(255) );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( FORMAT VARCHAR(255) );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( HINT VARCHAR(255) );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( FIELDSIZE INTEGER );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( MULTI_VALUE INTEGER NOT NULL DEFAULT 0); 

/*
 * The following rows are needed to enable attribute visibility.
 * Basically we added the feature to hide atributes from the view unless
 * they are required.
 */

ALTER TABLE SCARAB_R_MODULE_ATTRIBUTE    ADD ( VISIBLE INTEGER default 1);
ALTER TABLE SCARAB_R_ISSUETYPE_ATTRIBUTE ADD ( VISIBLE INTEGER default 1);

insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
       values(6, 2, 'Dropdown tree', 'org.tigris.scarab.attribute.ComboTreeBoxAttribute');
