/*
 * b19-Workflow Migration.
 *
 * Creates the field that support the requirement of a given role to get to
 * view an attributegroup.
 *
 */
ALTER TABLE SCARAB_ATTRIBUTE_GROUP ADD (VIEW_ROLE_ID INTEGER);