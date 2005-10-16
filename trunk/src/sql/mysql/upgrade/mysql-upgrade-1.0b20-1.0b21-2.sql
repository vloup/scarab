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
		            TRANSACTION_ID BIGINT NOT NULL,
		            CREATOR_ID INTEGER NOT NULL,
		            RECEIVER_ID INTEGER NOT NULL,
		            STATUS INTEGER NOT NULL,
		            CREATION_DATE DATETIME,
		            CHANGE_DATE DATETIME,
    PRIMARY KEY(TRANSACTION_ID,CREATOR_ID,RECEIVER_ID),
    FOREIGN KEY (CREATOR_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (RECEIVER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (TRANSACTION_ID) REFERENCES SCARAB_TRANSACTION (TRANSACTION_ID)
    
);

# -----------------------------------------------------------------------
# SCARAB_NOTIFICATION_FILTER
# -----------------------------------------------------------------------

CREATE TABLE SCARAB_NOTIFICATION_FILTER
(
		            MODULE_ID INTEGER NOT NULL,
		            USER_ID INTEGER NOT NULL,
		            ACTIVITY_TYPE VARCHAR (30) NOT NULL,
		            FILTER_TYPE INTEGER,
    PRIMARY KEY(MODULE_ID,USER_ID,ACTIVITY_TYPE,FILTER_TYPE),
    FOREIGN KEY (USER_ID) REFERENCES TURBINE_USER (USER_ID)
    ,
    FOREIGN KEY (MODULE_ID) REFERENCES SCARAB_MODULE (MODULE_ID)
    
);
  
