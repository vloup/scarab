/*
 * Changes to index names since some non-MySQL databases require
 * index names to be unique by schema rather than by table.
 *
 * Created By: Thierry Lach
 */

ALTER TABLE SCARAB_R_MODULE_ATTRIBUTE
    DROP INDEX IX_REQUIRED;

ALTER TABLE SCARAB_R_MODULE_ATTRIBUTE
    DROP INDEX IX_QUICKSEARCH;

CREATE INDEX IX_MOD_ATTR_REQUIRED ON SCARAB_R_MODULE_ATTRIBUTE (REQUIRED);
CREATE INDEX IX_MOD_ATTR_QUICKSEARCH ON SCARAB_R_MODULE_ATTRIBUTE (QUICK_SEARCH);

ALTER TABLE SCARAB_R_ISSUETYPE_ATTRIBUTE
    DROP INDEX IX_REQUIRED;

ALTER TABLE SCARAB_R_ISSUETYPE_ATTRIBUTE
    DROP INDEX IX_QUICKSEARCH;

CREATE INDEX IX_ISSUETYPE_ATTR_REQUIRED ON SCARAB_R_ISSUETYPE_ATTRIBUTE (REQUIRED);
CREATE INDEX IX_ISSUETYPE_ATTR_QUICKSEARCH ON SCARAB_R_ISSUETYPE_ATTRIBUTE (QUICK_SEARCH);


