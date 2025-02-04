package org.tigris.scarab.actions;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.parser.StringValueParser;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.Turbine;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeType;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.IteratorWithSize;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabUtil;
import org.tigris.scarab.util.export.ExportFormat;
import org.tigris.scarab.util.word.IssueSearch;

/**
 *  This class is responsible for searching.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class Search extends RequireLoginFirstAction
{
    private static final String ADD_USER = "add_user";
    private static final String ADD_USER_BY_USERNAME = "add_user_by_username";
    private static final String SELECTED_USER = "select_user";
    private static final String USER_LIST = "user_list";
    private static final String ANY = "any";
    private static final String CREATED_BY = "created_by";

    ScarabLocalizationTool l10n;
    ScarabRequestTool scarabR;
    Intake intake;
    ParameterParser params;
    ScarabUser user;

    /**
     *
     */
    public void doPerform(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        IteratorWithSize queryResults = scarabR.getCurrentSearchResults();
        if (queryResults != null && queryResults.hasNext())
        {
            context.put("queryResults", queryResults);
            String next = ScarabUtil.findValue(data, "next");
            if (StringUtils.isNotEmpty
                (ScarabUtil.findValue(data, ExportFormat.KEY_NAME)))
            {
                // Send to the IssueListExport screen (which actually
                // has no corresponding Velocity template).
                setTarget(data, "IssueListExport.vm");
            }
            else if (StringUtils.isNotEmpty(next))
            {
                // Redirect to View, Assign, or Move/Copy
                List issueIds = null;
                ScarabUser user = (ScarabUser) data.getUser();
                if (next.indexOf("All") > -1)
                {
                    // all issues are selected
                    issueIds = getAllIssueIds(data);
                }
                else
                {
                    // get issues select by user
                    issueIds = getSelected(data);
                }


                if (issueIds.size() < 1)
                {
                    scarabR.setAlertMessage(L10NKeySet.SelectIssues);
                    return;
                }

                List modules = ModuleManager.getInstancesFromIssueList(
                    scarabR.getIssues(issueIds));
                if (next.indexOf("assign") > -1)
                {
                    if (user.hasPermission(ScarabSecurity.ISSUE__ASSIGN, modules))
                    {
                        if (issueIds.size() <= ScarabConstants.ISSUE_MAX_ASSIGN)
                        {
                            scarabR.resetAssociatedUsers();
                            setTarget(data, "AssignIssue.vm");
                        }
                        else
                        {
                            L10NMessage msg = new L10NMessage
                            (
                                L10NKeySet.IssueLimitExceeded,
                                String.valueOf(ScarabConstants.ISSUE_MAX_ASSIGN)
                            );
                            scarabR.setAlertMessage(msg);
                            return;
                        }
                    }
                    else
                    {
                        scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                        return;
                    }
                }
                else if (next.indexOf("view") > -1)
                {
                    if (user.hasPermission(ScarabSecurity.ISSUE__VIEW, modules))
                    {
                        if (issueIds.size() <= ScarabConstants.ISSUE_MAX_VIEW)
                        {
                            setTarget(data, "ViewIssueLong.vm");
                        }
                        else
                        {
                            L10NMessage msg = new L10NMessage
                            (
                                L10NKeySet.IssueLimitExceeded,
                                String.valueOf(ScarabConstants.ISSUE_MAX_VIEW)
                            );
                            scarabR.setAlertMessage(msg);
                            return;
                        }
                    }
                    else
                    {
                        scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                        return;
                    }
                }
                else if (next.indexOf("copy") > -1)
                {
                    if (user.hasPermission(ScarabSecurity.ISSUE__ENTER, modules))
                    {
                        if (issueIds.size() <= ScarabConstants.ISSUE_MAX_COPY)
                        {
                            data.getParameters().add("mv_0rb", "copy");
                            setTarget(data, "MoveIssue.vm");
                        }
                        else
                        {
                            L10NMessage msg = new L10NMessage
                            (
                                L10NKeySet.IssueLimitExceeded,
                                String.valueOf(ScarabConstants.ISSUE_MAX_COPY)
                            );
                            scarabR.setAlertMessage(msg);
                            return;
                        }
                    }
                    else
                    {
                        scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                    }
                }
                else if (next.indexOf("move") > -1)
                {
                    if (user.hasPermission(ScarabSecurity.ISSUE__MOVE, modules))
                    {
                        if (issueIds.size() <= ScarabConstants.ISSUE_MAX_MOVE)
                        {
                            data.getParameters().add("mv_0rb", "move");
                            setTarget(data, "MoveIssue.vm");
                        }
                        else
                        {
                            L10NMessage msg = new L10NMessage
                            (
                                L10NKeySet.IssueLimitExceeded,
                                String.valueOf(ScarabConstants.ISSUE_MAX_MOVE)
                            );
                            scarabR.setAlertMessage(msg);
                            return;
                        }
                    }
                    else
                    {
                        scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                    }
                }
            }
            else
            {
                String template = data.getParameters()
                    .getString(ScarabConstants.NEXT_TEMPLATE,
                               "IssueList.vm");
                setTarget(data, template);
            }
        }
    }

    /**
     * Saves the query string for the logged-in user, and performs the
     * default action of {@link #doPerform}.
     */
    public void doSearch(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        String queryString = getQueryString(data);        
        user.setMostRecentQuery(queryString);

        doPerform(data, context);
    }

    /**
        Redirects to form to save the query. May redirect to Login page.
    */
    public void doRedirecttosavequery(RunData data, TemplateContext context)
         throws Exception
    {
        setup(data, context);
        if (data.getParameters().getString("refine") != null)
        {
            user.setMostRecentQuery(getQueryString(data));
        }

        if (scarabR.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES))
        {
            setTarget(data, "SaveQuery.vm");
        }
        else 
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
        }
    }

    public void doRedirecttocrossmodulelist(RunData data, TemplateContext context)
         throws Exception
    {
        data.getParameters().setString("queryString", getQueryString(data));
        setTarget(data, "IssueTypeList.vm");
    }

    /**
        Saves query.
    */
    public void doSavequery(RunData data, TemplateContext context)
         throws Exception
    {
        setup(data, context);
        
        if (!scarabR.hasPermission(ScarabSecurity.USER__EDIT_PREFERENCES))
        {
            scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
            return;
        }

               
        Module module = scarabR.getCurrentModule();
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query",
                                      query.getQueryKey());

        Field name = queryGroup.get("Name");
        name.setRequired(true);
        data.getParameters().setString("queryString", getQueryString(data));

        if (intake.isAllValid())
        {
            queryGroup.setProperties(query);
            query.setScarabUser(user);
            MITList currentList = user.getCurrentMITList();
            if (currentList == null)
            {
                scarabR.setAlertMessage(L10NKeySet.NoIssueTypeList);
                return;
            }
            else
            {
                //
                // Get hold of all the attributes that apply to
                // this list. We will have to copy them manually
                // to the new list because MITList.copy() won't
                // do it for us.
                //
                List commonAttributes = 
                    currentList.getCommonRModuleUserAttributes();
                
                // associate the query with a new list, the current
                // implementation does not allow for multiple queries to
                // work from the same MITList and this guarantees they
                // will not accidently be linked.
                currentList = currentList.copy();
                if (currentList.isModifiable())
                {
                    currentList.setName(null);
                }
                
                //
                // Copy the attributes from the original list
                // to the new one.
                //
                for (Iterator iter = commonAttributes.iterator();
                              iter.hasNext(); )
                {
                    RModuleUserAttribute attr =
                            (RModuleUserAttribute) iter.next();
                    
                    //
                    // When we copy the attribute, we don't actually
                    // want to keep the list ID because that refers
                    // to the old list rather than the new one. So
                    // we just clear it - when the list is saved the
                    // correct list ID will automatically be set for
                    // the attribute.
                    //
                    RModuleUserAttribute newAttr = attr.copy();
                    newAttr.setListId(null);
                    currentList.addRModuleUserAttribute(newAttr);
                }
                
                //
                // Update the query to use the new MIT list.
                //
                query.setMITList(currentList);
                if (!currentList.isSingleModule())
                {
                    query.setModule(null);
                    query.setScopeId(Scope.PERSONAL__PK);
                }
            }

            ScarabUser[] userList =
                module.getUsers(ScarabSecurity.ITEM__APPROVE);
            if (checkForDupes(query, user, module))
            {
                scarabR.setAlertMessage(L10NKeySet.DuplicateQueryName);
            }
            else if (Scope.MODULE__PK.equals(query.getScopeId())
                 && user.hasPermission(ScarabSecurity.ITEM__APPROVE, module)
                 && (userList == null || userList.length == 0))
            {
                L10NMessage msg = new L10NMessage(L10NKeySet.NoApproverAvailable, module.getName());
                scarabR.setAlertMessage(msg);
            }
            else
            {
                try
                {
                    query.saveAndSendEmail(user, module, context);
                }
                catch(Exception e)
                {
                    L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneral,e);
                    scarabR.setAlertMessage(msg);
                }
                scarabR.resetSelectedUsers();
                if (query.canEdit(user))
                {
                    scarabR.setConfirmMessage(DEFAULT_MSG);
                }
                else
                {
                    L10NMessage msg = new L10NMessage(L10NKeySet.NotifyPendingApproval,L10NKeySet.Query);
                    scarabR.setInfoMessage(msg);
                    //scarabR.setInfoMessage(
                    //        l10n.format("NotifyPendingApproval",
                    //        l10n.get("Query").toLowerCase()));
                }
                setTarget(data, "QueryList.vm");
            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
    }

    public boolean doEditqueryinfo(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        boolean success = false;

        Module module = scarabR.getCurrentModule();
        Query query = scarabR.getQuery();
        Group queryGroup = intake.get("Query",
                                      query.getQueryKey());
        queryGroup.get("Name").setRequired(true);
        if (intake.isAllValid())
        {
            queryGroup.setProperties(query);
            if (checkForDupes(query, user, module))
            {
                scarabR.setAlertMessage(L10NKeySet.DuplicateQueryName);
            }
            else
            {
                query.saveAndSendEmail(user, module, context);
                success = true;
                if (query.canEdit(user))
                {
                    scarabR.setConfirmMessage(L10NKeySet.QueryModified);
                }
                else
                {
                    L10NMessage msg = new L10NMessage(L10NKeySet.NotifyPendingApproval,L10NKeySet.Query);
                    scarabR.setInfoMessage(msg);
                    //scarabR.setInfoMessage(
                    //                l10n.format("NotifyPendingApproval",
                    //                l10n.get("Query").toLowerCase()));
                    setTarget(data, data.getParameters().getString(
                                    ScarabConstants.CANCEL_TEMPLATE));
                }

            }
        }
        else
        {
            scarabR.setAlertMessage(ERROR_MESSAGE);
        }
        return success;
    }

    public void doPreparequery(RunData data, TemplateContext context)
         throws Exception
    {        
        setup(data, context);
        Query query = scarabR.getQuery();
        user.setCurrentMITList(query.getMITList());
        /* TODO! It would be better if the query could be viewed or
           edited without having to pass the query data via request parameters
           as would be done with the code below, but it caused a few bugs like
           losing users and maybe the mitlist, so revisit this later.
        user.setMostRecentQuery(query.getValue());
        */
        user.setMostRecentQuery(getQueryString(data));
        scarabR.resetSelectedUsers();
    }

    /**
        Edits the stored query.
    */
    public void doEditstoredquery(RunData data, TemplateContext context)
         throws Exception
    {        
        setup(data, context);        

        Query query = scarabR.getQuery();
        String newValue = getQueryString(data);
        query.setValue(newValue);
        query.saveAndSendEmail((ScarabUser)data.getUser(), 
                 scarabR.getCurrentModule(), context);
        scarabR.resetSelectedUsers();
        user.setMostRecentQuery(newValue);
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredquery(RunData data, TemplateContext context)
         throws Exception
    {
        setup(data, context);
        // Set current query to the stored query
        Query query = scarabR.getQuery();
        MITList mitList = query.getMITList();
        user.setCurrentMITList(mitList);
        if (mitList != null)
        {
            mitList.setScarabUser(user);
        }
        user.setMostRecentQuery(query.getValue());
        
        //
        // Add 'sortColumn', 'sortPolarity' and 'resultsPerPage'
        // to the RunData parameters. This ensures that when the
        // user runs a saved query, the resulting issue list is
        // displayed with that query's settings. 
        //
        StringValueParser parser = new StringValueParser();
        parser.parse(query.getValue(), '&', '=', true);
        
        if (parser.containsKey("resultsperpage")) {
            data.getParameters().add("resultsperpage",
                                     parser.getInt("resultsperpage"));
        }
        
        if (parser.containsKey("searchsai")) {
            data.getParameters().add("sortColumn",
                                     parser.getInt("searchsai"));
        }
        
        if (parser.containsKey("searchsp")) {
            data.getParameters().add("sortPolarity",
                                     parser.getString("searchsp"));
        }

        setTarget(data, "IssueList.vm");
    }


    /** 
     * This method handles clicking the Go button in the SearchNav.vm
     * file. First it checks to see if the select box passed in a number
     * or a string. If it is a number, then we run the stored query
     * assuming the number is the query id. Else, we assume it is a
     * string and that is our template to redirect to.
     */
    public void doSelectquery(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        String go = data.getParameters().getString("go");
        

        if (go != null && go.length() > 0)
        {
            // if the string is a number, then execute
            // doRunstoredquery()
            if (StringUtils.isNumeric(go))
            {
                data.getParameters().setString("queryId", go);
                doRunstoredquery(data, context);
            }
            else if (go.startsWith("newQuery"))
            {
                // do this to load last mitlist
                user.getMostRecentQuery();
                if (go.endsWith("IT")) 
                {
                    user.setCurrentMITList(null);
                }
                // reset selected users map
                scarabR.resetSelectedUsers();                
                setTarget(data, user.getQueryTarget());
            }
            else if (go.equals("mostRecent"))
            {
                setTarget(data, "IssueList.vm");
            }
            else if (go.equals("myIssuesThisModule"))
            {
                Module module = user.getCurrentModule();
                user.setCurrentMITList(MITListManager
                    .getSingleModuleAllIssueTypesList(module, user));

                String userId = user.getQueryKey();
                StringBuffer sb = new StringBuffer(26 + 2*userId.length());
                String query = sb.append("&user_list=").append(userId)
                    .append("&user_attr_").append(userId).append("=any")
                    .toString();
                user.setMostRecentQuery(query);
                setTarget(data, "IssueList.vm");
            }
            else if (go.equals("myIssuesAllModules"))
            {
                user.setCurrentMITList(MITListManager.getAllModulesAllIssueTypesList(user));

                String userId = user.getQueryKey();
                StringBuffer sb = new StringBuffer(26 + 2*userId.length());
                String query = sb.append("&user_list=").append(userId)
                    .append("&user_attr_").append(userId).append("=any")
                    .toString();
                user.setMostRecentQuery(query);
                setTarget(data, "IssueList.vm");
            }
            else if (go.equals("quickSearch"))
            {
                String searchString = data.getParameters().getString("searchString");
                if(searchString==null || searchString.equals(""))
                {
                    scarabR.setAlertMessage(L10NKeySet.QueryParserError);
                }
                else
                {
                    Module module = user.getCurrentModule();
                    MITList mitList = MITListManager.getSingleModuleAllIssueTypesList(module,user);
                    user.setCurrentMITList(mitList); 
                               
                    Map attributeMap = new Hashtable();
                    //List modules = mitList.getModules();
                    //for (int index=0; index < modules.size(); index++)
                    //{
                    //    Module module = (Module)modules.get(index);
                    String moduleName = module.getName();
                    List attributes = module.getAllAttributes();
                    for( int aindex = 0; aindex < attributes.size(); aindex++)
                    {
                        Attribute attribute = (Attribute) attributes.get(aindex);
                        AttributeType type = attribute.getAttributeType();
                        String typeName = type.getName();
                        String attributeName = attribute.getName();
                        if(typeName.equals("string") || typeName.equals("long-string"))
                        {
                            if (attribute.isTextAttribute())
                            {
                                Integer id = attribute.getAttributeId();
                                if(attributeMap.get(id) == null)
                                {
                                    attributeMap.put(id,attribute);
                                }
                            }
                        }
                    }
                    //}

                    quickSearch(searchString, attributeMap, user, context);                
                    user.setMostRecentQuery(getQueryString(data));
                }
                setTarget(data, "IssueList.vm");
            }
            else if (go.equals("privateQueries")
                   ||go.equals("publicQueries"))
            {
                setTarget(data,"QueryList.vm");
            }
            else
            {
                setTarget(data, go);
            }
            if (go.equals("myIssues") || go.equals("mostRecent"))
            {
                IteratorWithSize searchResults = null;
                try
                {
                    searchResults = scarabR.getCurrentSearchResults();
                }
                catch (java.lang.IllegalArgumentException e)
                {
                    // Swallow this exception.
                    Log.get().debug("", e);
                }
                if (searchResults != null && searchResults.size() > 0)
                {
                    context.put("issueList", searchResults);
                }
            }
        }
        else
        {
            // set the next template
            String nextTemplate = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                Turbine.getConfiguration()
                           .getString("template.homepage", "Index.vm"));
            setTarget(data, nextTemplate);
        }
    }

    /**
     * @param attributeMap
     * @return
     */
    private void quickSearch(String searchString, Map attributeMap, ScarabUser user, TemplateContext context)
    {
        String query;

        String userId = user.getQueryKey();

        String queryStart = "&user_attr_"+userId+"=any"
                         + "&intake-grp=attv"
                         + "&intake-grp=search"
                         + "&searchsp=asc"
                         + "&searchtype=advanced";

        final String queryEnd = "&searchsctoi=0"
                              +  "&resultsperpage=25"
                              +  "&searchscfoi=0";

        Iterator iter = attributeMap.keySet().iterator();
        query = queryStart;
        while(iter.hasNext())
        {
            Integer id = (Integer)iter.next();
            query += "&attv__"+id+"val="+searchString;
        }
        
        query += queryEnd;

        user.setMostRecentQuery(query);
        
        IteratorWithSize searchResults = null;
        try
        {
            searchResults = scarabR.getCurrentSearchResults(true);
        }
        catch (java.lang.IllegalArgumentException e)
        {
            // Swallow this exception.
            Log.get().debug("", e);
        }
        if (searchResults != null && searchResults.size() > 0)
        {
            context.put("issueList", searchResults);
        }
    }

    /**
        redirects to AdvancedQuery.
    */
    public void doRefinequery(RunData data, TemplateContext context)
         throws Exception
    {        
        context.put("refine", "true");
        setTarget(data, "AdvancedQuery.vm");            
    }


    /**
        Overrides base class.
    */
    public void doDone(RunData data, TemplateContext context)
        throws Exception
    {
        boolean success = doEditqueryinfo(data, context);
        if (success)
        {
            doEditstoredquery(data, context);
            doCancel(data, context);
        }
    }

    /**
     * @return The search string used to perform the query.  (Does
     * <i>not</i> refer to the CGI context of the term "query
     * string".)
     */
    public static String getQueryString(RunData data) throws Exception
    {
        String queryString = data.getParameters().getString("queryString");
        if (queryString == null) 
        {
            StringBuffer buf = new StringBuffer();
            Object[] keys =  data.getParameters().getKeys();
            for (int i =0; i<keys.length; i++)
            {
                String key = keys[i].toString();
                if (key.startsWith("attv") || key.startsWith("search")
                    || key.startsWith("intake") || key.startsWith("user_attr")
                    || key.startsWith("user_list") || key.startsWith("results")
                    || "format".equalsIgnoreCase(key))
                {
                    String[] values = data.getParameters().getStrings(key);
                    for (int j=0; j<values.length; j++)
                    {
                        String value = values[j];
                        if (StringUtils.isNotEmpty(value))
                        {
                            buf.append('&').append(key);
                            buf.append('=').append(ScarabUtil.urlEncode(value));
                        }
                    }
                }
            }

            queryString = (buf.length() == 0
                           ? ((ScarabUser)data.getUser()).getMostRecentQuery()
                           : buf.toString());
        }
        return queryString;
    }
        
    /**
       Check for duplicate query names. 
       A user cannot create a personal query with the same name as another one 
       of their personal queries.
       A user cannot create a module-level query with the same name 
       as another module-level query in the same module.
    */
    private boolean checkForDupes(Query query, ScarabUser user, Module module)
        throws Exception
    {
        boolean areThereDupes = false;
        List prevQueries = new ArrayList();
        if (query.getScopeId().equals(Scope.MODULE__PK))
        {
            prevQueries.addAll(QueryPeer.getModuleQueries(module));
        }
        else
        {
            // Add personal queries only, not all module-level queries created
            // by this user.
            for (Iterator i = QueryPeer.getUserQueries(user).iterator(); i.hasNext(); ) 
            {
                Query q = (Query)i.next();
                if (q.getModule() == null)
                {
                    prevQueries.add(q);
                }
            }
        }
        if (prevQueries != null && !prevQueries.isEmpty())
        {
            Long pk = query.getQueryId();
            String name = query.getName();
            for (Iterator i = prevQueries.iterator(); 
                 i.hasNext() && !areThereDupes;)
            {
                Query q = (Query)i.next();
                areThereDupes = (pk == null || !pk.equals(q.getQueryId())) &&
                    name.trim().toLowerCase().equals(
                        q.getName().trim().toLowerCase());
            }
        }
        return areThereDupes;
    }

    /**
        Retrieves list of all issue id's and puts in the context.
    */
    private List getAllIssueIds(RunData data)
    {
        List newIssueIdList = new ArrayList();
        ParameterParser pp = data.getParameters();
        String[] allIssueIds = pp.getStrings("all_issue_ids");
        if (allIssueIds != null)
        {
            while(pp.containsKey("issue_ids"))
            {
                pp.remove("issue_ids");
            }
            for (int i =0; i< allIssueIds.length; i++)
            {
                pp.add("issue_ids", allIssueIds[i]);
                newIssueIdList.add(allIssueIds[i]);
            }
        }
        return newIssueIdList;
    }

    /**
        Retrieves list of selected issue id's and puts in the context.
    */
    private List getSelected(RunData data)
    {
        List newIssueIdList = new ArrayList();
        ParameterParser pp = data.getParameters();
        String[] selectedIds = pp.getStrings("issue_ids");
        if (selectedIds != null) 
        {
            while(pp.containsKey("issue_ids"))
            {
                pp.remove("issue_ids");
            }
            for (int i=0; i<selectedIds.length; i++) 
            {
                pp.add("issue_ids", selectedIds[i]);
                newIssueIdList.add(selectedIds[i]);
            }
        }        
        return newIssueIdList;
    }

    public void doGotoeditlist(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }
        data.getParameters().setString(ScarabConstants.CANCEL_TEMPLATE,
                                       getCurrentTemplate(data));
        user.setMostRecentQuery(getQueryString(data));
        IssueSearch search = scarabR.getPopulatedSearch();
        if (search != null)
        {
            setTarget(data, "UserList.vm");            
        }
    } 

    /**
     * Adds users from temporary working list.
     */
    public void doAddusers(RunData data, TemplateContext context) 
        throws Exception
    {
        setup(data, context);
        
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null)
        {
            userMap = new HashMap();
        }        
        String[] userIds = params.getStrings(ADD_USER);
        if (userIds != null && userIds.length > 0) 
        {
            for (int i =0; i<userIds.length; i++)
            {
                String userId = userIds[i];
                String[] attrIds = params.getStrings("user_attr_" + userId);
                if (attrIds != null) 
                {
                    for (int j=0; j<attrIds.length; j++) 
                    {
                        addAttributeToMap(userMap, userId, attrIds[j], context);
                    }
                }
            } 
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereAdded);
        }
        else 
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }


    /**
        Adds user to the search form.
    */
    public void doAdduserbyusername(RunData data, TemplateContext context)  
        throws Exception
    {
        setup(data, context);

        String userName = params.getString(ADD_USER_BY_USERNAME);
        String attrId = params.getString("add_user_attr");

        Map userMap = user.getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }

       ScarabUser newUser = scarabR.getUserByUserName(userName);        
       boolean success = false;
       // we are only interested in users that can be assignees
       if (newUser != null)
       {
           MITList mitList = user.getCurrentMITList();
           List modules = mitList.getModules();
           if (ANY.equals(attrId))
           {
               success = false;
               // check that the user has at least one applicable attribute
               for (Iterator i = mitList.getCommonUserAttributes().iterator(); 
                    i.hasNext() && !success;) 
               {
                   success = newUser.hasPermission(
                       ((Attribute)i.next()).getPermission(), modules);
               }
               if (!success) 
               {
                   // check created by
                   success = newUser.hasPermission(ScarabSecurity.ISSUE__ENTER,
                                                   modules);
               }
           }
           else if (CREATED_BY.equals(attrId))
           {
               success = newUser.hasPermission(ScarabSecurity.ISSUE__ENTER, 
                                               modules);
           }
           else
           {
               try
               {
                   Attribute attribute = 
                       scarabR.getAttribute(new Integer(attrId));
                   success = newUser.hasPermission(attribute.getPermission(), 
                                                   modules);
               }
               catch (Exception e)
               {
                   // don't allow adding the user
                   success = false;
                   Log.get().error("Error trying to get user ," + userName + 
                       ", for a query. Attribute id = " + attrId, e);
               }
           }
       }

       if (success)
       {
           String userId = newUser.getUserId().toString();
           addAttributeToMap(userMap, userId, attrId, context);
           user.setSelectedUsersMap(userMap);
           scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereAdded);
       }
       else
       {
           scarabR.setAlertMessage(L10NKeySet.UserNotPossibleAssignee);
       }
    }

    private void addAttributeToMap(Map userMap, String userId, String attrId, 
                                   TemplateContext context)
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List attrIds = (List)userMap.get(userId);
        if (attrIds == null) 
        {
            attrIds = new ArrayList(3);
            userMap.put(userId, attrIds);
        }

        if (ANY.equals(attrId)) 
        {
            if (!attrIds.isEmpty()) 
            {
                scarabR.setInfoMessage(L10NKeySet.AnyHasReplacedPreviousChoices);
            }
            attrIds.clear();
        }

        boolean isNew = true;
        for (Iterator i = attrIds.iterator(); i.hasNext() && isNew;) 
        {
            Object oldAttrId = i.next();
            isNew = !ANY.equals(oldAttrId) && !oldAttrId.equals(attrId); 
        }
        
        if (isNew) 
        {
            attrIds.add(attrId);            
        }
        else 
        {
            scarabR.setInfoMessage(L10NKeySet.ChoiceAlreadyAccountedAny);
        }
    }

    /**
     * Removes users from temporary working list.
     */
    public void doRemoveusers(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
         
        Map userMap = user.getSelectedUsersMap();
        if (userMap == null || userMap.size() == 0)
        {
            userMap = new HashMap();
            loadUsersFromUserList(data, userMap);
        }
        
        String[] userAttrIds =  params.getStrings(SELECTED_USER);
        if (userAttrIds != null && userAttrIds.length > 0) 
        {
            for (int i =0; i<userAttrIds.length; i++)
            {
                String userAttrId = userAttrIds[i];
                int delimPos = userAttrId.indexOf('_');
                String userId = userAttrId.substring(0, delimPos);
                List currentAttrIds = (List)userMap.get(userId);
                if (currentAttrIds.size() == 1) 
                {
                    userMap.remove(userId);                    
                }
                else 
                {
                    currentAttrIds.remove(userAttrId.substring(delimPos+1));
                }
            }
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereRemoved);
        }
        else 
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    public void doUpdateusers(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        
        Map userMap = user.getSelectedUsersMap();
        
        String[] userAttrIds =  params.getStrings(SELECTED_USER);
        if (userAttrIds != null && userAttrIds.length > 0) 
        {
            for (int i =0; i<userAttrIds.length; i++)
            {
                String userAttrId = userAttrIds[i];
                int delimPos = userAttrId.indexOf('_');
                String userId = userAttrId.substring(0, delimPos);
                String oldAttrId = userAttrId.substring(delimPos+1);
                String newAttrId = params
                    .getString("user_attr_" + userAttrId);
                if (!oldAttrId.equals(newAttrId)) 
                {
                    List currentAttrIds = (List)userMap.get(userId);
                    if (ANY.equals(newAttrId)) 
                    {
                        currentAttrIds.clear();
                        currentAttrIds.add(ANY);
                    }
                    else 
                    {
                        for (int j=currentAttrIds.size()-1; j>=0; j--) 
                        {
                            if (currentAttrIds.get(j).equals(oldAttrId)) 
                            {
                                currentAttrIds.set(j, newAttrId);
                                break;
                            }
                        }
                    }
                }                
            }
            user.setSelectedUsersMap(userMap);
            scarabR.setConfirmMessage(L10NKeySet.SelectedUsersWereModified);
        }
        else 
        {
            scarabR.setAlertMessage(L10NKeySet.NoUsersSelected);
        }
    }

    /**
     * In the case of a saved query, puts the saved query's users
     * Into the selected users map
     */
    public void loadUsersFromUserList(RunData data, Map userMap)
        throws Exception
    {
        ParameterParser params = data.getParameters();
        String[] userList = params.getStrings(USER_LIST);
        if (userList !=null && userList.length > 0)
        {
            for (int i =0;i<userList.length;i++)
            {
                String userId = userList[i];
                String[] attrIds = params.getStrings("user_attr_" + userId);
                if (attrIds != null) 
                {
                    for (int j=0; j<attrIds.length; j++) 
                    {
                        addAttributeToMap(userMap, userId, attrIds[j],
                                          getTemplateContext(data));
                    }
                }
            }
            ((ScarabUser)data.getUser()).setSelectedUsersMap(userMap);
        }
    }

    public void doSetquerytarget(RunData data, TemplateContext context)
        throws Exception
    {
        setup(data, context);
        
        MITList mitlist = user.getCurrentMITList();
        if (mitlist != null && mitlist.isSingleModuleIssueType()) 
        {
            user.setSingleIssueTypeQueryTarget(
                mitlist.getIssueType(), data.getTarget());
        }
        else 
        {
        
            if (mitlist == null) 
            {
                scarabR.setAlertMessage(L10NKeySet.NoIssueTypeList);
            }
            else 
            {
                scarabR.setAlertMessage(L10NKeySet.IssueTypeListMoreThanOne);
            }
            

            Log.get().warn(
                "Issue type list did not contain one and only one element.");
        }
    }
    private void setup(RunData data, TemplateContext context) throws Exception{
        l10n = getLocalizationTool(context);
        scarabR = getScarabRequestTool(context);
        intake = getIntakeTool(context);
        params = data.getParameters();
        user = (ScarabUser)data.getUser();
    }
        
}
