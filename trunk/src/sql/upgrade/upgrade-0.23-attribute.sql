/*
 * Added Attribute Format Field size and Hinted field (release 0.23) 
 *
 * Creates the fields that support the enhanced formatting capabilities
 * of user defined Attributes
 *
 */
ALTER TABLE SCARAB_ATTRIBUTE ADD ( FORMAT VARCHAR(255) );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( HINT VARCHAR(255) );
ALTER TABLE SCARAB_ATTRIBUTE ADD ( FIELDSIZE INTEGER );
