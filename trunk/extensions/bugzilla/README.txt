Some tools to help convert from Bugzilla to Scarab.

Copyright (c) 2006 Steve James. All rights reserved. ste@cpan.org
This script is released under the same terms as Scarab itself.
See http://scarab.tigris.org/

------------------------------------------------------------------------------
References

[1] About Bugzilla: http://www.bugzilla.org/about/
[2] About Scarab: http://www.solitone.org/scarab_doc/
[3] Development mailing list: dev@scarab.tigris.org
[4] Perl: www.perl.org
[5] Saxon XSLT processor: http://saxon.sourceforge.net/
[6] XML Importing: http://www.solitone.org/scarab_doc/xmlimport/index.html

------------------------------------------------------------------------------
STATUS
A previous issue of the files in this directory has been attached to issue
SCB1668 as a patch to 1.0-b20. If you want to transfer issues from Bugzilla to
a Scarab 1.0-b20 installation, use those files.
See http://www.solitone.org/scarab/issues/id/SCB1668.

These files have since been updated to reflect current development. This
integrates the XSL transform process into the Scarab web interface.

This README will be maintained in the Scarab Wiki. Look there for the latest
revision.

------------------------------------------------------------------------------
INTRODUCTION
The tools in this directory will assist you in migrating from the Bugzilla [1]
bug tracking system to the Scarab [2] issue management system. The purpose of
these scripts is to provide you with the means to extract the contents of your
Bugzilla database and insert it into a new Scarab installation, making this as
hands-free an exercise as possible.

Bugzilla can export bugs into XML form, and Scarab can import [6] into its
database from data in XML form. The roll of these scripts is to communicate
with your Bugzilla server to extract bugs from the database, and then to
transform the resulting XML into a form that Scarab can import. The first stage
is performed by a Perl script. You must tell it where your Bugzilla server is,
and which bugs to download, and it takes care of the detail. The second stage
is achieved with the use of an XSL transform. The transform is applied to the
Bugzilla XML to produce Scarab XML. During the transform, attribute names and
values are adjusted as necessary.

The Bugzilla schema is a given, but as for Scarab, you are free to choose what
information you store and how you arrange it. In defining your schema for
Scarab, you will make choices about what types of issues you will have, which
modules they will appear in and what attributes these issue types have. In the
simplest case, you will reflect the Bugzilla schema almost one-to-one in
'defect' artefacts, and reflect the Bugzilla 'bugs' with severity "enhancement"
as Scarab 'enhancement' artefacts. You will find that the example Scarab defect
and enhancement artefacts are much like Bugzilla's bugs with respect to the
attributes they have. Some attributes are missing from Scarab, however, so you
must add these using the administrative interface if you wish to use the full
transform.

The transform script as you find it, assumes the simple case where you preserve
the Bugzilla data with minimal changes. The transform can be as direct or
indirect as you choose but of course you will have to extend the transform as
necessary for more sophisticated modifications to the data. You might, for
example, want to peel off all bugs with a certain milestone and treat them as
a special kind of artefact, or you might wish to rearrange the components into
different functional areas.

In the least, you will need to adjust the transform to configure the mappings
of some of your attributes, particularly 'component', 'milestone' and 'version'
where your Bugzilla data will have values unknown to the transform.

------------------------------------------------------------------------------
COMPATIBILITY
These scripts have been created and tested using Bugzilla 2.16.1 and
Scarab 1.0-b21-dev at Subversion revision 10042. Use with newer versions of
either of these systems will no doubt be possible, but some modifications may
be necessary. Likely changes include new or renamed Bugzilla attributes and
changes to the Scarab import XML schema. In these cases, the XSLT will need
appropriate adjustments.

No doubt the Bugzilla schema has changed since 2.16.1. If attributes have been
added, these will be quietly ignored by the transform. You may want to add
appropriate mappings for new attributes. If you do this in conditional sections
(enclosed by <xsl:if test="new_attribute">) then the new transform will be
backwardly compatible. In the case where backward compatibility is impossible,
it may make sense to rename the current menu entry and transform file to, say,
"Bugzilla 2.16.1" and then create additional names for the newer version.

If you undertake to make such modernisations, please tell us! [3]

------------------------------------------------------------------------------
PREREQUISITES
1. A live Bugzilla installation. This is interrogated for a given set of bugs
to be downloaded using its xml.cgi service. You need to know the URL for the
Bugzilla server, eg http://bugzilla.example.org/

2, A live Scarab installation. You will be configuring this with administrative
privileges.

3. Perl. Any modern Perl distribution should do

4. An XSLT 2.0 processor. I used Saxon 7.8 [5].

------------------------------------------------------------------------------
INSTRUCTIONS
In brief, these are the steps you must carry out:

   * Download bugs from Bugzilla
   * Configure the XSL transform
   * Transform into Scarab import XML
   * Import into Scarab

You must repeat the transform and import steps for each Bugzilla "product" for
which you have a corresponding Scarab "module".

SAMPLE DATA
-----------
For use when testing the import process, you will find the file
bugzilla-sample-bug.xml in this directory useful. This file contains several
bugs in Bugzilla XML format. All the usual attributes are represented, except
for attachments. This data has come from our local Bugzilla server, but with
all the names changed to my own, to protect the guilty.

An alternative way to obtain sample data is to use Bugzilla's self-host at 
the Mozilla site. In the download section below, use
http://bugzilla.mozilla.org as the Bugzilla server address. Valid IDs in
this database seem to start at 507. Don't try to dump everything (please!).

BUGZILLA DOWNLOAD
-----------------
In this directory you will find the Perl script bugzilla-xml-dump.pl. Use
perldoc or just look inside for details of its operation. Hopefully you can
just run it, specifying the URL to your Bugzilla server and a selection of bugs,
either as a numbered list or a numbered range. For example:

  ./bugzilla-xml-dump.pl --list 2484,2485 http://bugzilla:8080
or
  ./bugzilla-xml-dump.pl --from 1 --to 2000 http://bugzilla:8080

You will end up with these files:

   * bugzilla.xml               - the XML export data from Bugzilla
   * attachments/*              - a file for each attachment
   * attachments/mime_types.xsl - a look-up table used by the XSLT

The mime-type look-up table records the mime-type for each attachment that is
associated with the specified Bugzilla bugs. The attachments themselves are
stored in the attachments/ directory, named after the ID that Bugzilla has
assigned to them.

CONFIGURE
---------
The XSL transform bugzilla.xsl is not in this directory but rather in 
../../src/conf/classes/org/tigris/scarab/util/xmlissues/xsls/bugzilla.xsl.

This main script expects to find a second script (in extensions/bugzilla)
called mappings.xsl, containing the variable 'bz_to_scarab'. This table contains
mappings between Bugzilla and Scarab attribute values for account, status,
severity, platform, os, resolution, priority, component, milestone and version.
Copy mappings.xsl.sample to mappings.xsl. You will need to extend some of these
lists, particularly component, milestone and version according to your data.
You may also want to make other changes to the data while you have the
opportunity.

Also in this directory is a script called parameters.xsl.sample. Here you
will find several configuration variables. Copy this to parameters.xsl and
adjust as necessary.

o The variable 'bz_product' selects which bugs are chosen by the transform.
  Use this to select just the bugs appropriate to the target Scarab module.

o The variable 'bz_timezone' must be set to the timezone that the Bugzilla
  server is running in. This is necessary because the Bugzilla XML export
  doesn't specify the timezone.

In the bugzilla.xsl template, each bug is considered for transformation.
The selection of bugs is qualified by several conditions. You may want to adjust
these. By default, a bug is dropped (ie not transformed to a Scarab issue) if it
has a status of CLOSED or a resolution of INVALID, DUPLICATE or MOVED.

TRANSFORM
---------
The transform process is integrated into the Scarab import facility, so no
special action is necessary to execute the transform.

You will get a diagnostic if your Bugzilla data contains an attribute value for
which there is no look-up table entry.

IMPORT
------
First you must choose a module to import into, and then navigate to the XML
import page. To import, select your target module then select the XML import
page. Navigate to the Bugzilla import XML file 'bugzilla.xml' in this directory
and be sure to select 'Bugzilla' as the XML Format. Click 'Import file'.

Note that the target module must contain the artefacts and attributes mentioned
in your import XML. If something is missing, the importer will complain. I
suggest you pick an existing example module for testing purposes. You will only
have to add a few attributes to the example artefacts to get going, or you can
comment them out in the transform. Try just one or two bugs first, adjust the
transform, then test some more. Make sure you visit all the Bugzilla attributes
that you make use of in your data.

Watch the Tomcat log in tomcat/logs/catalina.out for progress and diagnostics.
Missing entries in mappings.xsl will be reported here.

The importer reports the new id numbers of the new artefacts it has created.
Review the new artefacts to check that the transform has worked correctly.

You will notice that Scarab may auto-assign issue IDs to the bugs you import: it
does not necessarily honour the original Bugzilla ID numbers. The XSL
transform emits Scarab import XML with bug ID numbers equal to the original
Bugzilla IDs, but those numbers may already be in use in the Scarab database.
Scarab will not replace an existing issue of the same ID with a new one. This
is due to a limitation of the database import type (see the <import-type> tag)
in this version of Scarab. If, however, the nominated ID is available, Scarab 
will use it. When re-importing the same Bugzilla issues for a second time
therefore, new issue IDs will be allocated to the Scarab database. The current
workaround is to start over with an empty database once you know your import
XML is correct. If you don't like the sound of that (I wouldn't) then see the
BACKUP-RESTORE section.

Once you are content with the results, re-create your modules from scratch and
re-import for the last time.

ATTACHMENTS
-----------
Note that the GUI declines to import the attachments, and instead adds a comment
to the artefacts to report this. This is a security measure.
Temporarily relax the security constraint in your 1.0b21-dev sources as follows.

In ../../src/java/org/tigris/scarab/util/xmlissues/ImportIssues.java at line
212, change this:

    public ImportIssues()
    {
        this(false);
    }

to this
    
    public ImportIssues()
    {
        this(true);
    }
    
and re-build and re-start your Scarab installation. This will allow attachments
to be imported using the web interface.

BACKUP-RESTORE
--------------
In the process of refining your Bugzilla to Scarab transform, you will want to
go back to a 'bare' Scarab database at least once, and more likely many times.
To aid in this I created the Perl script scarab-backup.pl. This is a simple
backup and restore tool for your Scarab MySQL table, attachments and
configuration. Use it to backup your Scarab installation in its 'bare' state,
before doing an import of Bugzilla data. When you want to go back and start all
over again, use the backup script in the restore mode. You may want to backup
in several steps as you approach your final set-up.

You should find the script in extensions/backup/.

CREATE AN EMPTY DATABASE
------------------------
The following recipe can be used to create a completely clean Scarab database,
without the example records.

    cd tomcat/bin
    ./shutdown.sh
    cd ../extensions/backup
    [use backup script to purge database]
    cd ../../build
    ant create-db -Dskip.seed.data=1
    cd ../tomcat/bin
    ./startup.sh
