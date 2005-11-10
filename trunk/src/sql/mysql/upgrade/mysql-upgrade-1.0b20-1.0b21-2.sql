/*
 * b19-Workflow Migration.
 *
 * Create the new table that defines the transitions between diferent values of a
 * dropdown list attribute.
 */
# -----------------------------------------------------------------------
# SCARAB_NOTIFICATION_STATUS
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_NOTIFICATION_STATUS
(
    ACTIVITY_ID BIGINT NOT NULL,
    CREATOR_ID MEDIUMINT NOT NULL,
    RECEIVER_ID MEDIUMINT NOT NULL,
    STATUS MEDIUMINT NOT NULL,
    CREATION_DATE DATETIME,
    CHANGE_DATE DATETIME,
    COMMENT VARCHAR(255),
    PRIMARY KEY(ACTIVITY_ID,CREATOR_ID,RECEIVER_ID),
    FOREIGN KEY (CREATOR_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (RECEIVER_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (ACTIVITY_ID) REFERENCES SCARAB_ACTIVITY (ACTIVITY_ID),
    INDEX IX_NOTIF_STATUS (STATUS)
);

# -----------------------------------------------------------------------
# SCARAB_NOTIFICATION_FILTER
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_NOTIFICATION_FILTER
(
    MODULE_ID MEDIUMINT NOT NULL,
    USER_ID MEDIUMINT NOT NULL,
    ACTIVITY_TYPE VARCHAR(30) NOT NULL,
    MANAGER_ID MEDIUMINT default 0 NOT NULL,
    FILTER_STATE INTEGER(1) default 0 NOT NULL,
    SEND_SELF INTEGER(1) default 0 NOT NULL,
    SEND_FAILURES INTEGER(1) default 0 NOT NULL,
    PRIMARY KEY(MODULE_ID,USER_ID,ACTIVITY_TYPE,MANAGER_ID),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID),
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID)
);
