#!/usr/bin/perl -w

use strict;
use Getopt::Long;  # Command line argument parsing
use Net::HTTP;
use File::Spec::Functions;
my $ident = q$Id$;

###############################################################################

=pod

=head1 NAME

bugzilla-xml-dump.pl - Dumps Bugzilla bugs to local files for import to Scarab

Copyright (c) 2006 Steve James. All rights reserved. ste@cpan.org
This script is released under the same terms as Scarab itself.
See http://scarab.tigris.org/

=head1 SYNOPSIS

The Bugzilla bug tracking system includes a CGI tool to export given bugs to
an XML form. It's invoked like this:

   http://bugzilla/xml.cgi
or
   http://bugzilla/xml.cgi?id=10%21C2%2C32
   
Bugzilla also associates attachments with bugs and stores them in its database.
The attachments can be obtained like this:

   http://bugzilla/attachment.cgi?id=26
or
   http://bugzilla/attachment.cgi?id=26&action=edit

This script uses the XML export service to extract a given set of bugs into
XML form, and to collect any associated attachments into a local directory.

 bugzilla-xml-dump.pl [<options>] <URL>
    [--version]           Print version number of this script
    [--help]              Print command line help
    [--from]              Start of a range of bug numbers
    [--to]                End of a range of bug numbers
    [--list]              A list of bug numbers
    URL                   The base URL to the Bugzilla server

=cut

sub usage
    {
    print STDERR "@_\n" if @_;
    
    die <<"EOT";

Dumps Bugzilla bugs to local files for import to Scarab

Usage: bugzilla-xml-dump.pl [<options>] <URL>
    [--version]           Print version number of this script
    [--help]              Print command line help
    [--from]              Start of a range of bug numbers
    [--to]                End of a range of bug numbers
    [--list]              A list of bug numbers
    URL                   The base URL to the Bugzilla server

Use 'perldoc bugzilla-xml-dump.pl' for full documentation

EOT
    }

###############################################################################

use vars qw/ $opt_version $opt_help $opt_from $opt_to $opt_list/;

GetOptions("--version"     => \$opt_version,
           "--help",       => \$opt_help,
           "--from=i",     => \$opt_from,
           "--to=i",       => \$opt_to,
           "--list=s",     => \$opt_list,
          ) or usage();

# Print version information if requested
die $ident if $opt_version;

usage() if $opt_help;

my $opt_output      = catfile(curdir(), 'bugzilla.xml');
my $opt_attachments = catfile(curdir(), 'attachments');
my $opt_mime_types  = catfile($opt_attachments, 'mime_types.xsl');

usage('Specify a range or a list of bugs')
  unless ($opt_from && $opt_to) || $opt_list;
usage('Range is not valid')
  if ($opt_from && (($opt_from < 1) || ($opt_to < 1) || ($opt_from > $opt_to)));
usage('Cannot use list and range options together')
  if ($opt_list && $opt_from);
  
# Validate the Bugzilla server URL
my $opt_url = shift or usage('Specify the URL to the Bugzilla server');
$opt_url =~ s|http://||;
print STDERR "Bugzilla server is: http://$opt_url\n";

print STDERR "Writing scarab import XML to file: $opt_output\n";
print STDERR "Writing attachments mime-types to file: $opt_mime_types\n";
print STDERR "Copying attachments to directory: $opt_attachments\n";

###############################################################################

open OUT, ">$opt_output" or die "Failed to open output: $!";

# Build a list of the bug ids to fetch
my @list;
if ($opt_list) {
  @list = split ',', $opt_list;
  print STDERR "Bug IDs to fetch are: ", join ',',@list, "\n";
} else {
  @list = ($opt_from .. $opt_to);
  print STDERR "Bug IDs to fetch are from $opt_from to $opt_to\n";
}

# A global list of all bug attachment ids
my @attachments;

# A global hash of attachment mime types
my %mime_type;

# Get the first bug
my $xml = get_bugs(shift @list);

# Check the version of Bugzilla
$xml =~ m|bugzilla version="([0-9.]+)"|;
print STDERR "WARNING: This script was tested with Bugzilla 2.16.1 but this is $1!\n"
  unless ($1 eq '2.16.1');

# Insert missing character encoding
$xml =~ s|^<\?xml version=(.*) (.*)\?>\n|<?xml version=$1 encoding="iso-8859-1" $2?>\n|;
  
# Drop the closing </bugzilla> and emit the rest
$xml =~ s|</bugzilla>$||;
print OUT $xml;

# Get all the remaining bugs.
while (@list) {
  my @sublist;
  foreach (0..30) { $_ = shift @list; push @sublist, $_ if $_;};
  print STDERR "Fetching these bugs: ", (join ',', @sublist), "\n";

  $xml = get_bugs(@sublist);

  $xml =~ s|^<\?xml.*\?>\n<!DOCTYPE.*>\n<bugzilla.*>||;
  $xml =~ s|</bugzilla>$||;
  print OUT $xml;
}

# Terminate the output
print OUT "</bugzilla>\n";

@attachments = sort @attachments;
print STDERR "Attachments are: ", join ', ',@attachments,"\n";

# Get each attachment into an attachment file
mkdir $opt_attachments unless (-d $opt_attachments);
foreach (@attachments) {
  print STDERR "Getting attachment: $_\n";
  my $filename = catfile($opt_attachments, $_);
  open ATTACHMENT, ">$filename"
    or die "Failed to write attachment file $filename: $!";
  print ATTACHMENT get_attachment($_);
  close ATTACHMENT;
}

# Write mime types look-up table
print STDERR "Writing mime types look-up table\n";
open MIMETYPES, ">$opt_mime_types" or die "Cannot write $opt_mime_types: $!";
print MIMETYPES qq{<?xml version="1.0"?>\n},
                qq{<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
>\n\n},
                qq{<xsl:variable name="bz_mime_types">\n},
                qq{<mime_type id="dummy"></mime_type>\n},
                ;
foreach (keys %mime_type) {
  print MIMETYPES qq{    <mime_type id="$_">$mime_type{$_}</mime_type>\n};
}
print MIMETYPES "</xsl:variable>\n\n",
                "</xsl:stylesheet>\n",
                ;
close MIMETYPES;

##############################################################################
# Get a list of bugs from the Bugzilla server
sub get_bugs {
  my @list = @_;
  
  # Connect to the server
  my $s = Net::HTTP->new(Host => $opt_url) || die $@;
  
  $s->write_request(GET => "/xml.cgi?id=". (join '%2C', @list));
  
  my($code, $mess, %h) = $s->read_response_headers;
  
  my $xml;
  while (1) {
      my $buf;
      my $n = $s->read_entity_body($buf, 1024);
      die "read failed: $!" unless defined $n;
      last unless $n;
      $xml .= $buf;
  }
  
  while ($xml =~ m|<attachid>(\d+)</attachid>|g) {push @attachments, $1}
  
  $xml;
}

##############################################################################
# Get a list of bugs from the Bugzilla server
sub get_attachment {
  my $id = shift;
  
  # Connect to the server
  my $s = Net::HTTP->new(Host => $opt_url) || die $@;
  
  $s->write_request(GET => "/attachment.cgi?id=$id");
  
  my($code, $mess, %h) = $s->read_response_headers;
  
  my $mime_type = $h{'Content-Type'};
  print STDERR "Mime type of attachment $id is $mime_type\n";
  $mime_type{$id} = $mime_type;
  
  my $data;
  while (1) {
      my $buf;
      my $n = $s->read_entity_body($buf, 1024);
      die "read failed: $!" unless defined $n;
      last unless $n;
      $data .= $buf;
  }
  
  $data;
}
