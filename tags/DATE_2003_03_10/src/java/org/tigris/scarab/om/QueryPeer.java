package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.cache.ScarabCache;

// Local classes
import org.tigris.scarab.om.Module;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class QueryPeer 
    extends org.tigris.scarab.om.BaseQueryPeer
{

    static final String GET_QUERIES = 
        "getQueries";
    static final String GET_USER_QUERIES = 
        "getUserQueries";
    static final String GET_MODULE_QUERIES = 
        "getModuleQueries";
    static final String QUERY_PEER = 
        "QueryPeer";

    // query types
    public static final String TYPE_PRIVATE = "private";
    public static final String TYPE_GLOBAL = "global";
    public static final String TYPE_ALL = "all";

    // sort columns
    public static final String SORT_NAME = "name";
    public static final String SORT_DESCRIPTION = "desc";
    public static final String SORT_AVAILABILITY = "avail";
    public static final String SORT_USER = "user";

    /**
     * List of private queries associated with this module.
     * Created by this user.
     */
    public static List getQueries(Module module, IssueType issueType,
                                  ScarabUser user, String sortColumn,   
                                  String sortPolarity, String type)
        throws Exception
    {
        List queries = null;
        if (module == null || issueType == null)
        {
            queries = new ArrayList();
        }
        else 
        {
            // 4th element is ignored due to bug in torque
            Serializable[] key = {QUERY_PEER, GET_QUERIES, module, null, 
                issueType, user, sortColumn, sortPolarity, type};
            Object obj = QueryManager.getMethodResult().get(key);
            if (obj == null) 
            {
            Criteria crit = new Criteria()
                .add(QueryPeer.DELETED, 0);

            Criteria.Criterion moduleCrit = crit.getNewCriterion(
                QueryPeer.MODULE_ID, module.getModuleId(), Criteria.EQUAL);
            Criteria.Criterion issueTypeCrit = crit.getNewCriterion(
                QueryPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId(), 
                Criteria.EQUAL);
            moduleCrit.and(issueTypeCrit);
            
            Criteria.Criterion listCrit = crit.getNewCriterion(
                QueryPeer.LIST_ID, null, Criteria.NOT_EQUAL);
            listCrit.or(moduleCrit);
            crit.add(listCrit);

            Criteria.Criterion cGlob = crit.getNewCriterion(
                QueryPeer.SCOPE_ID, Scope.MODULE__PK, 
                Criteria.EQUAL);
            Criteria.Criterion cPriv = crit.getNewCriterion(
                QueryPeer.USER_ID, user.getUserId(), Criteria.EQUAL);
            cPriv.and(crit.getNewCriterion(
                QueryPeer.SCOPE_ID, Scope.PERSONAL__PK, 
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
                // All queries
                cGlob.or(cPriv);
                crit.add(cGlob);
            }
            crit.setDistinct();

            // Add sort criteria
            if (SORT_DESCRIPTION.equals(sortColumn))
            {
                addSortOrder(crit, QueryPeer.DESCRIPTION, 
                             sortPolarity);
            }
            else if (SORT_AVAILABILITY.equals(sortColumn))
            {
                crit.addJoin(QueryPeer.SCOPE_ID,
                             ScopePeer.SCOPE_ID);
                addSortOrder(crit, ScopePeer.SCOPE_NAME, sortPolarity);
            }
            else if (SORT_USER.equals(sortColumn))
            {
                addSortOrder(crit, QueryPeer.USER_ID, sortPolarity);
            }
            else
            {
                // sort by name
                addSortOrder(crit, QueryPeer.NAME, sortPolarity);
            }
            queries = QueryPeer.doSelect(crit);
            QueryManager.getMethodResult().put(queries, key);
        }
        else 
        {
            queries = (List)obj;
        }
        }
        return queries;
    }

    public static List getUserQueries(ScarabUser user)
        throws Exception
    {
        List queries = null;
        Object obj = QueryManager.getMethodResult()
            .get(QUERY_PEER, GET_USER_QUERIES, user);
        if (obj == null) 
        {
            Criteria crit = new Criteria()
                .add(QueryPeer.DELETED, 0);
            crit.add(QueryPeer.USER_ID, user.getUserId());
            queries = QueryPeer.doSelect(crit);
            QueryManager.getMethodResult()
                .put(queries, QUERY_PEER, GET_USER_QUERIES, user);
        }
        else 
        {
            queries = (List)obj;
        }
        return queries;
    }

    public static List getModuleQueries(Module module)
        throws Exception
    {
        List queries = null;
        Object obj = QueryManager.getMethodResult()
            .get(QUERY_PEER, GET_MODULE_QUERIES, module);
        if (obj == null) 
        {
            Criteria crit = new Criteria()
                .add(QueryPeer.DELETED, 0);
            crit.add(QueryPeer.MODULE_ID, module.getModuleId());
            crit.add(QueryPeer.SCOPE_ID, Scope.MODULE__PK);
            queries = QueryPeer.doSelect(crit);
            QueryManager.getMethodResult()
                .put(queries, QUERY_PEER, GET_MODULE_QUERIES, module);
         }
        else 
        {
            queries = (List)obj;
        }
        return queries;
    }

    public static List getQueries(Module module, IssueType issueType,
                                     ScarabUser user)
        throws Exception
    {
        return getQueries(module, issueType, user, SORT_AVAILABILITY, "asc", 
                          TYPE_ALL);
    }

    public static List getQueries(Module module, IssueType issueType,
                                     ScarabUser user, String sortColumn,   
                                     String sortPolarity)
        throws Exception
    {
        return getQueries(module, issueType, user, sortColumn, 
                          sortPolarity, TYPE_ALL);
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

}
