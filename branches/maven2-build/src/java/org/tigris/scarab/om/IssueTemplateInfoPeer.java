//  ================================================================
//  Copyright (c) 2000-2005 CollabNet.  All rights reserved.
//  
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//  
//  1. Redistributions of source code must retain the above copyright
//  notice, this list of conditions and the following disclaimer.
// 
//  2. Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  
//  3. The end-user documentation included with the redistribution, if
//  any, must include the following acknowlegement: "This product includes
//  software developed by Collab.Net <http://www.Collab.Net/>."
//  Alternately, this acknowlegement may appear in the software itself, if
//  and wherever such third-party acknowlegements normally appear.
//  
//  4. The hosted project names must not be used to endorse or promote
//  products derived from this software without prior written
//  permission. For written permission, please contact info@collab.net.
//  
//  5. Products derived from this software may not use the "Tigris" or 
//  "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
//  prior written permission of Collab.Net.
//  
//  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
//  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
//  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
//  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
//  IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 
//  ====================================================================
//  
//  This software consists of voluntary contributions made by many
//  individuals on behalf of Collab.Net.
package org.tigris.scarab.om;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.io.Serializable;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.cache.ScarabCache;

// Local classes
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabException;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class IssueTemplateInfoPeer 
    extends BaseIssueTemplateInfoPeer
{

    private static final String GET_TEMPLATES = 
        "getTemplates";
    static final String GET_USER_TEMPLATES = 
        "getUserTemplates";
    static final String GET_MODULE_TEMPLATES = 
        "getModuleTemplates";
    static final String TEMPLATE_PEER = 
        "IssueTemplateInfoPeer";

    // query types
    public static final String TYPE_PRIVATE = "private";
    public static final String TYPE_GLOBAL = "global";
    public static final String TYPE_ALL = "all";

    /**
     * List of Issue Template objects associated with this module.
     * And issue type, either created by current user, or project-scoped.
     * i.e., all templates this user has permission to see.
     */
    public static List getTemplates(Module module, IssueType issueType,
                                    ScarabUser user, String sortColumn, 
                                    String sortPolarity, String type)
        throws TorqueException, ScarabException
    {
        List templates = null;
        Serializable[] key = {TEMPLATE_PEER, GET_TEMPLATES, module,
            issueType, user, sortColumn, sortPolarity, type};
        Object obj = ScarabCache.get(key);
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(IssuePeer.MODULE_ID, module.getModuleId())
                .add(IssuePeer.DELETED, 0)
                .add(IssuePeer.MOVED, 0)
                .addJoin(ActivitySetPeer.TRANSACTION_ID, 
                         ActivityPeer.TRANSACTION_ID) 
                .addJoin(IssuePeer.ISSUE_ID, 
                         ActivityPeer.ISSUE_ID) 
                .add(IssuePeer.TYPE_ID, issueType.getTemplateId())
                .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                         IssuePeer.ISSUE_ID);
            crit.setDistinct();

            Criteria.Criterion cGlob = crit.getNewCriterion(
                IssueTemplateInfoPeer.SCOPE_ID, Scope.MODULE__PK,
                Criteria.EQUAL);
            cGlob.and(crit.getNewCriterion(IssueTemplateInfoPeer.APPROVED, 
                                           Boolean.TRUE, Criteria.EQUAL));
            Criteria.Criterion cPriv = crit.getNewCriterion(
                ActivitySetPeer.CREATED_BY, user.getUserId(),  
                Criteria.EQUAL);
            cPriv.and(crit.getNewCriterion(
                IssueTemplateInfoPeer.SCOPE_ID, Scope.PERSONAL__PK, 
                Criteria.EQUAL));

            if (TYPE_PRIVATE.equals(type))
            {
                crit.add(cPriv);
            }
            else if (TYPE_GLOBAL.equals(type))
            {
                crit.add(cGlob);
            }
            else
            {
                // All templates
                cGlob.or(cPriv);
                crit.add(cGlob);
            }

            // Add sort criteria
            if (sortColumn.equals("desc"))
            {
                addSortOrder(crit, IssueTemplateInfoPeer.DESCRIPTION, 
                             sortPolarity);
            }
            else if (sortColumn.equals("avail"))
            {
                crit.addJoin(IssueTemplateInfoPeer.SCOPE_ID,
                             ScopePeer.SCOPE_ID);
                addSortOrder(crit, ScopePeer.SCOPE_NAME, sortPolarity);
            }
            else if (!sortColumn.equals("user"))
            {
                // sort by name
                addSortOrder(crit, IssueTemplateInfoPeer.NAME, sortPolarity);
            }
            templates = IssueTemplateInfoPeer.doSelect(crit);
            ScarabCache.put(templates, key);
        }
        else 
        {
            templates = (List)obj;
        }
        if (sortColumn.equals("user"))
        {
            templates = sortByCreatingUser(templates, sortPolarity);
        }
        return templates;
    }

    public static List getUserTemplates(ScarabUser user, Module module,IssueType issueType)
        throws TorqueException, ScarabException
    {
        List templates = null;
        Object obj = ScarabCache.get(TEMPLATE_PEER, GET_USER_TEMPLATES, user);
        if (obj == null) 
        {
            Criteria crit = new Criteria()
            .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                         IssuePeer.ISSUE_ID)
            .add(IssuePeer.DELETED, 0)
                .add(IssuePeer.MODULE_ID, module.getModuleId())
            .addJoin(ActivitySetPeer.TRANSACTION_ID, 
                     ActivityPeer.TRANSACTION_ID) 
                .addJoin(IssuePeer.ISSUE_ID, 
                         ActivityPeer.ISSUE_ID) 
            .add(IssuePeer.TYPE_ID, issueType.getTemplateId())
            .add(ActivitySetPeer.CREATED_BY, user.getUserId());
            templates = IssueTemplateInfoPeer.doSelect(crit);
            ScarabCache.put(templates, TEMPLATE_PEER, GET_USER_TEMPLATES, user);
        }
        else 
        {
            templates = (List)obj;
        }
        return templates;
    }


    public static List getModuleTemplates(Module module)
        throws TorqueException
    {
        List templates = null;
        Object obj = ScarabCache.get(TEMPLATE_PEER, GET_MODULE_TEMPLATES, module);
        if (obj == null) 
        {
            Criteria crit = new Criteria()
            .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                         IssuePeer.ISSUE_ID)
            .add(IssuePeer.DELETED, 0)
            .add(IssuePeer.MODULE_ID, module.getModuleId())
            .add(IssueTemplateInfoPeer.SCOPE_ID, Scope.MODULE__PK);
            templates = IssueTemplateInfoPeer.doSelect(crit);
            ScarabCache.put(templates, TEMPLATE_PEER, GET_USER_TEMPLATES, module);
        }
        else 
        {
            templates = (List)obj;
        }
        return templates;
    }

    private static Criteria addSortOrder(Criteria crit, 
                       String sortColumn, String sortPolarity)
    {
        if (sortPolarity.equals("desc"))
        {
            crit.addDescendingOrderByColumn(sortColumn);
        }
        else
        {
            crit.addAscendingOrderByColumn(sortColumn);
        }
        return crit;
    }

    private static List sortByCreatingUser(List result,
                                           String sortPolarity)
        throws TorqueException
    {
        final int polarity = ("asc".equals(sortPolarity)) ? 1 : -1;   
        Comparator c = new Comparator() 
        {
            public int compare(Object o1, Object o2) 
            {
                int i = 0;
                try
                {
                    i = polarity * 
                        ((Issue)o1).getCreatedBy().getFirstName()
                         .compareTo(((Issue)o2).getCreatedBy().getFirstName());
                }
                catch (Exception e)
                {
                    //
                }
                return i;
             }
        };
        Collections.sort(result, c);
        return result;
    }

}
