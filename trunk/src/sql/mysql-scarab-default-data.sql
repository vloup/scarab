/*
 *  Attribute classes
 */

insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(1, 'free-form', 'Free-form atribute', 'org.tigris.scarab.attribute.FreeFormAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(2, 'select-one', 'Select_one attribute', 'org.tigris.scarab.attribute.SelectOneAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(3, 'voted', 'Voted attribute', 'org.tigris.scarab.attribute.VotedAttribute');
insert into SCARAB_ATTRIBUTE_CLASS(ATTRIBUTE_CLASS_ID, ATTRIBUTE_CLASS_NAME, ATTRIBUTE_CLASS_DESC, JAVA_CLASS_NAME)
        values(4, 'user', 'User attribute', 'org.tigris.scarab.attribute.UserAttribute');

/*
 *  Attribute types
 */

insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(1, 1, 'string', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(2, 1, 'date', 'org.tigris.scarab.attribute.DateAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(3, 1, 'integer', 'org.tigris.scarab.attribute.IntegerAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(4, 1, 'float', 'org.tigris.scarab.attribute.FloatAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(5, 2, 'combo-box', 'org.tigris.scarab.attribute.ComboBoxAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(6, 3, 'voted-average', 'org.tigris.scarab.attribute.VotedAverageAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(7, 3, 'voted-simple-majority', 'org.tigris.scarab.attribute.VotedSimpleMajorityAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(8, 4, 'user', 'org.tigris.scarab.attribute.UserAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Bugzilla-style vote */
        values(9, 3, 'voted-total', 'org.tigris.scarab.attribute.VotedTotalAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME) /* Tracking */
        values(10, 3, 'voted-tracking', 'org.tigris.scarab.attribute.TrackingAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(11, 1, 'email', 'org.tigris.scarab.attribute.StringAttribute');
insert into SCARAB_ATTRIBUTE_TYPE(ATTRIBUTE_TYPE_ID, ATTRIBUTE_CLASS_ID, ATTRIBUTE_TYPE_NAME, JAVA_CLASS_NAME)
        values(12, 1, 'long-string', 'org.tigris.scarab.attribute.StringAttribute');

/*
 *  Attributes
 */

insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Description */
        values(0, 'Null Attribute', 1, 'Null Attribute');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Description */
        values(1, 'Description', 12, 'Description');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, 
       ATTRIBUTE_TYPE_ID, PERMISSION, DESCRIPTION) /* Assigned to */
       values(2, 'Assigned To', 8, 'edit_issues', 'Assigned To');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Status */
        values(3, 'Status', 5, 'Status');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, 
       ATTRIBUTE_TYPE_ID, REQUIRED_OPTION_ID, DESCRIPTION) /* resolution */
        values(4, 'Resolution', 5, 5, 'Resolution');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, 
       ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Platform */
       values(5, 'Platform', 5, 'Platform');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Operating System */
        values(6, 'Operating System', 5, 'Operating System');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Priority select-one*/
        values(7, 'Priority', 5, 'Priority');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Priority voted*/
        values(8, 'Vote', 5, 'Vote');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Severity */
        values(9, 'Severity', 5, 'Severity');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Tracking */
        values(10, 'Tracking', 10, 'Tracking');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Brief (one-line) Description */
        values(11, 'Summary', 1, 'Summary');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Functional Area */
        values(12, 'Functional Area', 5, 'Functional Area');
insert into SCARAB_ATTRIBUTE(ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_TYPE_ID, DESCRIPTION) /* Total Votes */
        values(13, 'Total Votes', 9, 'Total Votes');

update SCARAB_ATTRIBUTE set CREATED_BY='2';

/*
 *
 */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* null option */
        values(0, 0, 'null', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Unconfirmed */
        values(1, 3, 'unconfirmed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* New */
        values(2, 3, 'new', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Assigned */
        values(3, 3, 'assigned', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Reopened */
        values(4, 3, 'reopened', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Resolved */
        values(5, 3, 'resolved', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Verified */
        values(6, 3, 'verified', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Closed */
        values(7, 3, 'closed', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Fixed */
        values(8, 4, 'fixed', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Invalid */
        values(9, 4, 'invalid', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* WONTFIX */
        values(10, 4, 'wontfix', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* LATER */
        values(11, 4, 'later', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* REMIND */
        values(12, 4, 'remind', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* DUPLICATE */
        values(13, 4, 'duplicate', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* WORKSFORME */
        values(14, 4, 'worksforme', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* MOVED */
        values(15, 4, 'moved', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* All */
        values(16, 5, 'all', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* DEC */
        values(17, 5, 'DEC', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* HP */
        values(18, 5, 'HP', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Macintosh */
        values(19, 5, 'Macintosh', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* PC */
        values(20, 5, 'PC', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* SGI */
        values(21, 5, 'SGI', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Sun */
        values(22, 5, 'sun', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Other */
        values(23, 5, 'other', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Operating systems */
        values(24, 6, 'All', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(75, 6, 'Windows', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(25, 6, 'windows 3.1', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(26, 6, 'windows 95', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(27, 6, 'windows 98', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(28, 6, 'windows ME', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(29, 6, 'windows 2000', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(30, 6, 'windows NT', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(76, 6, 'MacOS', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(31, 6, 'mac system 7', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(32, 6, 'mac system 7.5', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(33, 6, 'mac system 7.6.1', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(34, 6, 'mac system 8.0', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(35, 6, 'mac system 8.5', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(36, 6, 'mac system 8.6', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(37, 6, 'mac system 9.0', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(77, 6, 'OSX', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(87, 6, 'Unix', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(38, 6, 'Linux', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(78, 6, 'Redhat', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(79, 6, 'Suse', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(80, 6, 'Debian', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(81, 6, 'Other Linux', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(82, 6, '*BSD', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(40, 6, 'FreeBSD', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(41, 6, 'NetBSD', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(42, 6, 'OpenBSD', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(83, 6, 'Commercial Unix', 9);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(39, 6, 'BSDI', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(43, 6, 'AIX', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(44, 6, 'BeOS', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(45, 6, 'HP-UX', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(46, 6, 'IRIX', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(50, 6, 'OSF/1', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(51, 6, 'Solaris', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(52, 6, 'SunOS', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(53, 6, 'Other', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(48, 6, 'OpenVMS', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(49, 6, 'OS/2', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(84, 6, 'Realtime/Embedded', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(47, 6, 'Neutrino', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(85, 6, 'Handheld/PDA', 10);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(86, 6, 'PalmOS', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Priorities select-one */
        values(54, 7, 'High', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(55, 7, 'Medium', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(56, 7, 'Low', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(57, 7, 'Undecided', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Priorities voted */
        values(58, 8, 'High', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(59, 8, 'Medium', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(60, 8, 'Low', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(61, 8, 'Undecided', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Severities */
        values(62, 9, 'blocker', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(63, 9, 'critical', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(64, 9, 'major', 3);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(65, 9, 'normal', 4);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(66, 9, 'minor', 5);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(67, 9, 'trivial', 6);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(68, 9, 'enhancement', 7);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(69, 9, 'cosmetic', 8);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(70, 9, 'serious', 9);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(71, 9, 'undecided', 10);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Tracking options */
        values(72, 10, 'never', 1); /* never send notification */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(73, 10, 'major', 2); /* send notification on major change */
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(74, 10, 'any', 3); /* send notification on any change */

insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /* Functional Area */
        values(88, 12, 'UI', 1);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(89, 12, 'Setup', 2);
insert into SCARAB_ATTRIBUTE_OPTION(OPTION_ID, ATTRIBUTE_ID, OPTION_NAME, WEIGHT) /*  */
        values(90, 12, 'Help', 3);

/*
 * Option relationships
 * id, name
 */
insert into SCARAB_OPTION_RELATIONSHIP values (1, '1parent-child2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (2, '1required-prior2'); 
insert into SCARAB_OPTION_RELATIONSHIP values (3, '1required-after2'); 

/*
 * Option_Option relationships
 * option1_id, option2_id, relationship_id, preferred_order
 */
insert into SCARAB_R_OPTION_OPTION values (0,1,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,2,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,3,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,4,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,5,1,5);
insert into SCARAB_R_OPTION_OPTION values (0,6,1,6);
insert into SCARAB_R_OPTION_OPTION values (0,7,1,7);
insert into SCARAB_R_OPTION_OPTION values (0,8,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,9,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,10,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,11,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,12,1,5);
insert into SCARAB_R_OPTION_OPTION values (0,13,1,6);
insert into SCARAB_R_OPTION_OPTION values (0,14,1,7);
insert into SCARAB_R_OPTION_OPTION values (0,15,1,8);
insert into SCARAB_R_OPTION_OPTION values (0,16,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,17,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,18,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,19,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,20,1,5);
insert into SCARAB_R_OPTION_OPTION values (0,21,1,6);
insert into SCARAB_R_OPTION_OPTION values (0,22,1,7);
insert into SCARAB_R_OPTION_OPTION values (0,23,1,8);
insert into SCARAB_R_OPTION_OPTION values (0,54,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,55,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,56,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,57,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,58,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,59,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,60,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,61,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,62,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,63,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,64,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,65,1,4);
insert into SCARAB_R_OPTION_OPTION values (0,66,1,5);
insert into SCARAB_R_OPTION_OPTION values (0,67,1,6);
insert into SCARAB_R_OPTION_OPTION values (0,68,1,7);
insert into SCARAB_R_OPTION_OPTION values (0,69,1,8);
insert into SCARAB_R_OPTION_OPTION values (0,70,1,9);
insert into SCARAB_R_OPTION_OPTION values (0,71,1,10);
insert into SCARAB_R_OPTION_OPTION values (0,72,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,73,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,74,1,3);
insert into SCARAB_R_OPTION_OPTION values (0,88,1,1);
insert into SCARAB_R_OPTION_OPTION values (0,89,1,2);
insert into SCARAB_R_OPTION_OPTION values (0,90,1,3);

insert into SCARAB_R_OPTION_OPTION values (0,24,1,1);
insert into SCARAB_R_OPTION_OPTION values (24,75,1,2);
insert into SCARAB_R_OPTION_OPTION values (24,76,1,9);
insert into SCARAB_R_OPTION_OPTION values (24,87,1,18);
insert into SCARAB_R_OPTION_OPTION values (24,53,1,38);
insert into SCARAB_R_OPTION_OPTION values (24,84,1,41);
insert into SCARAB_R_OPTION_OPTION values (24,85,1,43);
insert into SCARAB_R_OPTION_OPTION values (75,25,1,3);
insert into SCARAB_R_OPTION_OPTION values (75,26,1,4);
insert into SCARAB_R_OPTION_OPTION values (75,27,1,5);
insert into SCARAB_R_OPTION_OPTION values (75,28,1,6);
insert into SCARAB_R_OPTION_OPTION values (75,29,1,7);
insert into SCARAB_R_OPTION_OPTION values (75,30,1,8);
insert into SCARAB_R_OPTION_OPTION values (76,31,1,9);
insert into SCARAB_R_OPTION_OPTION values (76,32,1,11);
insert into SCARAB_R_OPTION_OPTION values (76,33,1,12);
insert into SCARAB_R_OPTION_OPTION values (76,34,1,13);
insert into SCARAB_R_OPTION_OPTION values (76,35,1,14);
insert into SCARAB_R_OPTION_OPTION values (76,36,1,15);
insert into SCARAB_R_OPTION_OPTION values (76,37,1,16);
insert into SCARAB_R_OPTION_OPTION values (76,77,1,17);
insert into SCARAB_R_OPTION_OPTION values (38,78,1,25);
insert into SCARAB_R_OPTION_OPTION values (38,79,1,26);
insert into SCARAB_R_OPTION_OPTION values (38,80,1,27);
insert into SCARAB_R_OPTION_OPTION values (38,81,1,28);
insert into SCARAB_R_OPTION_OPTION values (82,40,1,20);
insert into SCARAB_R_OPTION_OPTION values (82,41,1,21);
insert into SCARAB_R_OPTION_OPTION values (82,42,1,22);
insert into SCARAB_R_OPTION_OPTION values (82,77,1,23);
insert into SCARAB_R_OPTION_OPTION values (83,39,1,30);
insert into SCARAB_R_OPTION_OPTION values (83,43,1,31);
insert into SCARAB_R_OPTION_OPTION values (83,44,1,32);
insert into SCARAB_R_OPTION_OPTION values (83,45,1,33);
insert into SCARAB_R_OPTION_OPTION values (83,46,1,34);
insert into SCARAB_R_OPTION_OPTION values (83,50,1,35);
insert into SCARAB_R_OPTION_OPTION values (83,51,1,36);
insert into SCARAB_R_OPTION_OPTION values (83,52,1,37);
insert into SCARAB_R_OPTION_OPTION values (53,48,1,39);
insert into SCARAB_R_OPTION_OPTION values (53,49,1,40);
insert into SCARAB_R_OPTION_OPTION values (84,47,1,42);
insert into SCARAB_R_OPTION_OPTION values (85,86,1,44);
insert into SCARAB_R_OPTION_OPTION values (87,38,1,24);
insert into SCARAB_R_OPTION_OPTION values (87,82,1,19);
insert into SCARAB_R_OPTION_OPTION values (87,83,1,29);

/*
 * default attributes to appear on the IssueList screen
 * if the user is not logged in or has not selected attributes,
 * these attribute for user id=0 are used.
 */
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,11,1);
INSERT INTO SCARAB_R_MODULE_USER_ATTRIBUTE VALUES (0,0,9,2);

/*
 * Issue Dependencies
 */
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(1, 'blocking');
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(2, 'duplicate');
insert into SCARAB_DEPEND_TYPE(DEPEND_TYPE_ID, DEPEND_TYPE_NAME)
        values(3, 'non-blocking');

/*
 * Types of saved queries.
 */
insert into SCARAB_QUERY_TYPE(QUERY_TYPE_ID, QUERY_TYPE_NAME)
        values(1, 'Personal profile');
insert into SCARAB_QUERY_TYPE(QUERY_TYPE_ID, QUERY_TYPE_NAME)
        values(2, 'All users');

/*
 * Types of saved issue templates.
 */
insert into SCARAB_ISSUE_TEMPLATE_TYPE(TEMPLATE_TYPE_ID, TEMPLATE_TYPE_NAME)
        values(1, 'Personal profile');
insert into SCARAB_ISSUE_TEMPLATE_TYPE(TEMPLATE_TYPE_ID, TEMPLATE_TYPE_NAME)
        values(2, 'All users');


/*
 * Attachment types
 */
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(1, 'ATTACHMENT');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(2, 'COMMENT');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(3, 'URL');
insert into SCARAB_ATTACHMENT_TYPE(ATTACHMENT_TYPE_ID, ATTACHMENT_TYPE_NAME)
        values(4, 'MODIFICATION');

/*
 * Transaction types
 */
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(1, 'Create Issue');
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(2, 'Edit Issue');
insert into SCARAB_TRANSACTION_TYPE(TYPE_ID, NAME)
        values(3, 'Move Issue');


/*
 * Frequency values
 */
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(1, 'every half hour');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(2, 'hourly');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(3, 'twice daily');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(4, 'daily');
insert into SCARAB_FREQUENCY(FREQUENCY_ID, FREQUENCY_NAME)
        values(5, 'weekly');


/*
 * root module
 */
insert into SCARAB_MODULE(MODULE_ID, MODULE_NAME, MODULE_DESCRIPTION, MODULE_URL)
        values(0, "global", "Built-in root module, parent for all top-level modules(projects)", "/");

/*
 * populate the root module with all attributes.
 * module_id, attr_id, display_value, active, required, preferred order, dedupe, quick search
 */
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,1,'Description',1,1,100,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,2,'Assigned To',0,0,200,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,3,'Status',1,0,300,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,4,'Resolution',1,0,400,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,5,'Platform',1,1,2,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES 
    (0,6,'Operating System',1,1,3,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,7,'Priority',1,0,500,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,8,'Vote',1,0,600,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,9,'Severity',1,0,700,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,10,'Tracking',0,0,800,0,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,11,'Summary',1,1,1,1,0);
INSERT INTO SCARAB_R_MODULE_ATTRIBUTE VALUES (0,12,'Functional Area',1,0,1000,0,0);

/*
 * populate the root module with all options.
 * module_id, option_id, display_value, active, preferred order
 */
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,1,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,2,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,3,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,4,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,5,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,6,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,7,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,8,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,9,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,10,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,11,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,12,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,13,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,14,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,15,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,16,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,17,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,18,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,19,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,20,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,21,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,22,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,23,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,24,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,25,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,26,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,27,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,28,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,29,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,30,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,31,NULL,1,10);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,32,NULL,1,11);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,33,NULL,1,12);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,34,NULL,1,13);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,35,NULL,1,14);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,36,NULL,1,15);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,37,NULL,1,16);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,38,NULL,1,18);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,39,NULL,1,28);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,40,NULL,1,24);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,41,NULL,1,25);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,42,NULL,1,26);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,43,NULL,1,29);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,44,NULL,1,30);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,45,NULL,1,31);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,46,NULL,1,34);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,47,NULL,0,39);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,48,NULL,0,42);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,49,NULL,0,41);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,50,NULL,1,35);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,51,NULL,1,32);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,52,NULL,1,33);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,53,NULL,1,40);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,54,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,55,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,56,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,57,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,58,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,59,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,60,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,61,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,62,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,63,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,64,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,65,NULL,1,4);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,66,NULL,1,5);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,67,NULL,1,6);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,68,NULL,1,7);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,69,NULL,1,8);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,70,NULL,1,9);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,71,NULL,1,10);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,72,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,73,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,74,NULL,1,3);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,75,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,76,NULL,1,9);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,77,NULL,1,17);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,78,NULL,0,19);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,79,NULL,0,20);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,80,NULL,0,21);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,81,NULL,0,22);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,82,NULL,1,23);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,83,NULL,1,27);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,84,NULL,1,38);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,85,NULL,1,36);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,86,NULL,0,37);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,87,NULL,1,17);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,88,NULL,1,1);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,89,NULL,1,2);
INSERT INTO SCARAB_R_MODULE_OPTION VALUES (0,90,NULL,1,3);
