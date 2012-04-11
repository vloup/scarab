A tool to help backup and restore a Scarab database

Copyright (c) 2006 Steve James. All rights reserved. ste@cpan.org
This script is released under the same terms as Scarab itself.
See http://scarab.tigris.org/

------------------------------------------------------------------------------
INTRODUCTION
This simple Perl script will assist in making a backup of a Scarab database. It can also purge (ie completely delete) a database and restore a previous backup.

The backup takes the form of a pair of compressed files, one containing an SQL dump of the database, the other containing all filesystem data, such as attachments, indexes and local properties.

------------------------------------------------------------------------------
LIMITATIONS
This script assumes the database is on a MySQL server. Conversion to other servers should be simple. The script assumes mysql and mysqldump are on the PATH.

This script has been developed and tested on Linux. Any necessary porting to Windows should be simple.

The script directly access the attachment and index files: it must be run locally on the Scarab server host. You cannot run the backup remotely.

The script assumes gzip, zcat and tar are on the PATH.

------------------------------------------------------------------------------
OPERATION

Run the script with --help for a list of the options. The examples below assume that you are running the script from the extensions/backup directory.

BACKUP

To create a backup of the 'scarab' database on server 'cobelz' to today.tar.gz and today.sql.gz:

./scarab-backup.pl --backup --server cobelz --password mypass --basename today

PURGE

Before purging a database, you should shutdown Scarab! To completely obliterate the 'scarab' database on cobelz:

../../tomcat/bin/shutdown.sh && ./scarab-backup.pl --purge --server cobelz --password mypass

RESTORE

To restore a database, you should shutdown Scarab first! To restore the 'scarab' database on cobelz from yesterday's backup:

../../tomcat/bin/shutdown.sh \
	&& ./scarab-backup.pl --purge --server cobelz --password mypas \
        && ../../tomcat/bin/startup.sh \
        && ./scarab-backup.pl --restore --server cobelz --password mypas --basename yesterday
