#!/usr/bin/perl -w

use strict;
use Getopt::Long;
use File::Spec::Functions;
use FindBin;

my $ident = q$Id$;

# Root node of Scarab working copy is assumed to be ../../ relative to
# where this script is (in extensions/backup).
my $scarab_root = catfile($FindBin::Bin, '..', '..');

###############################################################################

my $mailto = join '', qw(mailto: ste @ cpan . org);

sub usage
    {
    print STDERR "@_\n" if @_;
    
    die <<"EOT";

Backup, restore and purge Scarab database

Copyright (c) 2006 Steve James. All rights reserved. $mailto
This script is released under the same terms as Scarab itself.
See http://scarab.tigris.org/

This simple Perl script will assist in making a backup of a Scarab database. It
can also purge (ie completely delete) a database and restore a previous backup.

The backup takes the form of a pair of compressed files, one containing an SQL
dump of the database, the other containing all filesystem data, such as
attachments, indexes and local properties.

Usage: scarab-backup.pl [<options>]
    [--version]           Print version number of this script
    [--help]              Print command line help
    --backup | --restore | --purge
                          Select backup, restore or purge operation
    [--server]            Specifies the database server hostname [localhost]
    [--user]              Specifies the database server username [scarab]
    [--password]          Specifies a database password; [<prompt>]
    [--database]          Names the Scarab database [scarab]
    [--basename]          Names the backup archive file names [scarab]
EOT
    }

###############################################################################

my $opt_server   = 'localhost';		# Default database server
my $opt_user     = 'scarab';		# Default database user
my $opt_database = 'scarab';		# Default Scarab database name
my $opt_password = '';
my $opt_basename = 'scarab';

use vars qw/ $opt_version $opt_help
             $opt_backup $opt_restore $opt_basename $opt_purge
           /;

GetOptions("--version"        => \$opt_version,
           "--help"           => \$opt_help,
           "--backup"         => \$opt_backup,
           "--restore"        => \$opt_restore,
           "--purge"          => \$opt_purge,
           "--server=s"       => \$opt_server,
           "--user=s"         => \$opt_user,
           "--password=s"     => \$opt_password,
           "--database=s"     => \$opt_database,
           "--basename=s",    => \$opt_basename,
          ) or usage();

# Print version information if requested
die $ident if $opt_version;

usage() if $opt_help;

usage('Select backup, restore or purge mode')
  unless ($opt_backup || $opt_restore || $opt_purge);
usage('Cannot select more than one of backup, restore or purge!')
  if (  ($opt_backup && $opt_restore)
     || ($opt_backup && $opt_purge)
     || ($opt_restore && $opt_purge)
     );

print STDERR "Database server is: $opt_server\n";
print STDERR "Database name is: $opt_database\n";
print STDERR "Database user is: $opt_user\n";
print STDERR "Database password is: ",
               $opt_password ? '*' x length($opt_password)
                             : '<interactive>', "\n";
print STDERR "Backup files basename is: $opt_basename\n";
print STDERR "\n";

###############################################################################
my $scarab_tomcat = catfile('tomcat', 'work', 'Standalone', 'localhost');
my $scarab_webinf = catfile('target', 'scarab', 'WEB-INF');
my $sql_dump_file = "$opt_basename.sql.gz";
my $tar_dump_file = "$opt_basename.tar.gz";

my %scarab_data = (
  scarab_attachments => catfile($scarab_webinf, 'attachments'),
  scarab_index       => catfile($scarab_webinf, 'index'),
  scarab_custprops   => catfile($scarab_webinf, 'conf', 'custom.properties'),
  scarab_intakeser   => catfile($scarab_webinf, 'conf', 'intake-xml.ser'),
#  scarab_tomcatser   => catfile($scarab_tomcat, 'scarab', 'SESSIONS.ser'),
);

if ($opt_backup)
{
  print STDERR "Backing up Scarab database '$opt_database' from $opt_server...\n";
  do_cmd ('mysqldump', $opt_database,
          '--opt',
          "--host=$opt_server",
          "--user=$opt_user",
          $opt_password ? "--password=$opt_password" : '--password',
          '|', 'gzip',
          '>', $sql_dump_file,
          );
  print STDERR "Done\n";
  
  print STDERR "Backing up Scarab data files from $scarab_webinf...\n";
  do_cmd ('tar',
          '--directory', $scarab_root,
          '-cvzf', $tar_dump_file,
          values(%scarab_data),
          );
  print STDERR "Done\n";

}
elsif ($opt_restore)
{
  print STDERR "Creating Scarab table...\n";
  do_cmd ('mysql',
          "--host=$opt_server",
          "--user=$opt_user",
          $opt_password ? "--password=$opt_password" : '--password',
          '-e', "'create database $opt_database'",
          );
  print STDERR "Done\n";
  
  print STDERR "Restoring Scarab database from '$sql_dump_file'...\n";
  do_cmd ('zcat', $sql_dump_file,
          '|',
          'mysql',
          $opt_database,
          "--host=$opt_server",
          "--user=$opt_user",
          $opt_password ? "--password=$opt_password" : '--password',
          );
  print STDERR "Done\n";
  
  print STDERR "Restoring Scarab data files from $tar_dump_file...\n";
  do_cmd ('tar',
          '--directory', $scarab_root,
          '-xvzf', $tar_dump_file,
          values(%scarab_data),
          );
  print STDERR "Done\n";
}
elsif ($opt_purge) {
  print STDERR "Purging Scarab database...\n"; 
  do_cmd ('mysql',
          "--host=$opt_server",
          "--user=$opt_user",
          $opt_password ? "--password=$opt_password" : '--password',
          '-e', "'drop database $opt_database'",
          );
  print STDERR "Done\n";
  
  print STDERR "Purging Scarab data files...\n";
  foreach (values(%scarab_data)) {
    do_cmd ('rm', '-Rf', catfile($scarab_root, $_));
  }
}

##############################################################################
sub do_cmd {
  system("@_") == 0 or die "system call '@_' failed: $?";
}
