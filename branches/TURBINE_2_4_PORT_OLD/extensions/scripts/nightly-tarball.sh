#!/bin/sh

# Setup the PATH so that all of the utilities are found
PATH=/usr/local/bin:/bin:/usr/bin
export PATH

# Serial number
SERIAL=`date +%Y%m%d`

# CVS Module name
MODULE=scarab

# Name for distribution
TAG=${MODULE}-${SERIAL}

# Path to public ftp/http directory
PUBDIR=${HOME}/public_html/${MODULE}/nightly

# make PUBDIR if needed
mkdir -p ${PUBDIR}

# change to the right directory
cd ${PUBDIR}

# Create an exportable directory of the SVN module
svn export http://svn.collab.net/repos/${MODULE}/trunk ${TAG}

# Create tarball
tar -c -z --exclude .svn -f ${PUBDIR}/${TAG}.tar.gz ${TAG}

# cleanup the export directory
rm -r ${PUBDIR}/${TAG}

# Remove those older than 7 days.
find ${PUBDIR} -name "${MODULE}*.gz" -mtime +7 | xargs rm -f

# End of script 