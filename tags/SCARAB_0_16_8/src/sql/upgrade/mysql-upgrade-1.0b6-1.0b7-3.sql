/*
 * This script adds a new table that is used by users who wish to be added
 * added to a module.
 *
 * Created By: John McNally 
 * $Id$
 */

# -----------------------------------------------------------------------
# SCARAB_PENDING_GROUP_USER_ROLE
# -----------------------------------------------------------------------
drop table if exists SCARAB_PENDING_GROUP_USER_ROLE;

CREATE TABLE SCARAB_PENDING_GROUP_USER_ROLE
(
    USER_ID INTEGER NOT NULL,
    GROUP_ID INTEGER NOT NULL,
    ROLE_NAME VARCHAR (255) NOT NULL,
    PRIMARY KEY(USER_ID,GROUP_ID,ROLE_NAME),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (GROUP_ID) REFERENCES SCARAB_MODULE (MODULE_ID)
);

