/*
 * Added more fields (release 0.23)
 * Style     : used as style and or class markup for display
 * Format    : regular expression which the input must conform too
 * Field size: size of displayfield (not field length!)
 * Hint      : example input as viusal hint for user entries
 *
 * Adds 2 flags to dynamically configure if attachments and/or
 * user controlled workflow is included into the new issue entry wizzard.
 *
 */

ALTER TABLE SCARAB_R_MODULE_ISSUE_TYPE ADD ( ENABLE_ATTACHMENTS INTEGER default 1);
ALTER TABLE SCARAB_R_MODULE_ISSUE_TYPE ADD ( ENABLE_WORKFLOW INTEGER default 1);
