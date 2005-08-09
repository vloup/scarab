#!/usr/bin/env python
 # ================================================================
 # Copyright (c) 2000-2005 CollabNet.  All rights reserved.
 # 
 # Redistribution and use in source and binary forms, with or without
 # modification, are permitted provided that the following conditions are
 # met:
 # 
 # 1. Redistributions of source code must retain the above copyright
 # notice, this list of conditions and the following disclaimer.
 # 
 # 2. Redistributions in binary form must reproduce the above copyright
 # notice, this list of conditions and the following disclaimer in the
 # documentation and/or other materials provided with the distribution.
 # 
 # 3. The end-user documentation included with the redistribution, if
 # any, must include the following acknowlegement: "This product includes
 # software developed by Collab.Net <http://www.Collab.Net/>."
 # Alternately, this acknowlegement may appear in the software itself, if
 # and wherever such third-party acknowlegements normally appear.
 # 
 # 4. The hosted project names must not be used to endorse or promote
 # products derived from this software without prior written
 # permission. For written permission, please contact info@collab.net.
 # 
 # 5. Products derived from this software may not use the "Tigris" or 
 # "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 # prior written permission of Collab.Net.
 # 
 # THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 # WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 # MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 # IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 # DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 # DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 # GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 # INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 # IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 # OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 # ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 #
 # ====================================================================
 # 
 # This software consists of voluntary contributions made by many
 # individuals on behalf of Collab.Net.
 #

##############################################################################
#  CONFIGURATION 
##############################################################################
#
# basic configuration
VIEWCVS_URL = "http://svn.collab.net/viewcvs/scarab/"
SCARAB_XMLRPC_URL = "http://scarab.tigris.org:12345/scarab/issues"
DISABLE_EMAILS = True
# i18n strings
MSG_MUST_HAVE_COMMENT = "Must have a log message"
MSG_SUBVERSION_COMMIT = "Subversion commit"
MSG_ACTION_ADDED = "Added"
MSG_ACTION_DELETED = "Deleted"
MSG_ACTION_COPIED = "Copied"
MSG_ACTION_MODIFIED = "Modified"
#
##############################################################################

import sys
import urllib
from svn import core, repos, fs, delta
from xmlrpclib import Server


def main(pool, repos_dir, rev, config_fp):
  # Construct a ChangeCollector to fetch our changes.
  fs_ptr = repos.svn_repos_fs(repos.svn_repos_open(repos_dir, pool))
  root = fs.revision_root(fs_ptr, rev, pool)

  # Get revision properties we need. (Subversion 1.2)
  # cc = repos.ChangeCollector(fs_ptr, root, pool)
  # props = cc.get_root_props()
  # author = str(props.get(core.SVN_PROP_REVISION_AUTHOR, ''))
  # log = str(props.get(core.SVN_PROP_REVISION_LOG, ''))

  # Get revision properties we need. (Subversion 1.1)
  cc = repos.RevisionChangeCollector(fs_ptr, rev, pool)
  author = fs.revision_prop(fs_ptr, rev, core.SVN_PROP_REVISION_AUTHOR, pool)
  log = fs.revision_prop(fs_ptr, rev, core.SVN_PROP_REVISION_LOG, pool)

  ### Do any Subversion-to-Scarab author mappings here ###

# Example:
#  _authors = {
#   'miw':'mick',
#   'lif':'lisefr',
#   'gni':'gunleik',
#   'man':'Maja',
#   'ako':'konand',
#   'hho':'helga',
#   'hba':'hilde',
#   'rgc':'rgc'
#   }
#  author = _authors.get(author, author)

  # Now build the comment.  First we start with some header
  # information about the revision, and a link to the ViewCVS revision
  # view.
  e_ptr, e_baton = delta.make_editor(cc, pool)
  repos.svn_repos_replay(root, e_ptr, e_baton, pool)
  comment = "%s %d:    %s/?view=rev&rev=%d\n" \
            % (MSG_SUBVERSION_COMMIT, rev, VIEWCVS_URL, rev)
  comment = comment + \
"""-------------------------------------------------------------------------
%s
-------------------------------------------------------------------------
""" % log
  
  # Next, we'll figure out which paths changed and use that
  # information to generate ViewCVS links.
#  changes = cc.get_changes() # Subversion 1.2
  changes = cc.changes # Subversion 1.1
  paths = changes.keys()
#  paths.sort(lambda a, b: core.svn_path_compare_paths(a, b)) # Subversion 1.2
  for path in paths:
    change = changes[path]
    diff_url = ''
    action = None
    if not change.path: ### Deleted (show the last living version)
      action = MSG_ACTION_DELETED
      diff_url = '%s/%s?view=auto&rev=%d' \
                 % (VIEWCVS_URL,
                    urllib.quote(change.base_path[1:]), change.base_rev)
    elif change.added: ### Added
      if change.base_path and (change.base_rev != -1): ### (with history)
        action = MSG_ACTION_COPIED
        diff_url = '%s/%s?view=diff&rev=%d&p1=%s&r1=%d&p2=%s&r2=%d' \
                   % (VIEWCVS_URL,
                      urllib.quote(change.path), rev,
                      urllib.quote(change.base_path[1:]), change.base_rev,
                      urllib.quote(change.path), rev)
      else: ### (without history, show new file)
        action = MSG_ACTION_ADDED
        diff_url = '%s/%s?view=auto&rev=%d' \
                   % (VIEWCVS_URL,
                      urllib.quote(change.path), rev)
    elif change.text_changed: ### Modified
      action = MSG_ACTION_MODIFIED
      diff_url = '%s/%s?view=diff&rev=%d&p1=%s&r1=%d&p2=%s&r2=%d' \
                 % (VIEWCVS_URL,
                    urllib.quote(change.path), rev,
                    urllib.quote(change.base_path[1:]), change.base_rev,
                    urllib.quote(change.path), rev)
    if action:
      comment = comment + "%s: %s\n    %s\n" % (action, path, diff_url)

  # Connect to the xmlrpc server, and transmit our data.
  Server(SCARAB_XMLRPC_URL).simple.addComment(log, author, comment, DISABLE_EMAILS)



if __name__ == '__main__':
  if len(sys.argv) < 3:
    sys.stderr.write("Publish Subversion commits into to Scarab.")
    sys.stderr.write("\nUSAGE: %s REPOS-DIR REVISION\n" % (sys.argv[0]))
    sys.exit(1)
  sys.exit(core.run_app(main, sys.argv[1], int(sys.argv[2]), None))
