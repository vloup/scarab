package org.tigris.scarab.tools;

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

import java.util.Collections;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.velocity.TurbineVelocityService;
import org.apache.fulcrum.velocity.VelocityService;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.turbine.services.pull.ApplicationTool;

import org.apache.velocity.app.FieldMethodizer;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;

import org.tigris.scarab.notification.ActivityType;
import org.tigris.scarab.notification.Notification;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.IssueTypePeer;

import org.tigris.scarab.om.NotificationRule;
import org.tigris.scarab.om.NotificationRuleManager;
import org.tigris.scarab.om.NotificationRulePeer;
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.radeox.ScarabRenderEngine;
import org.tigris.scarab.util.ReferenceInsertionFilter;
import org.tigris.scarab.workflow.Workflow;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.util.IssueIdParser;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.SkipFiltering;
import org.tigris.scarab.util.SimpleSkipFiltering;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.ScarabUtil;

import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

import org.apache.turbine.Turbine;

/**
 * This scope is an object that is made available as a global
 * object within the system.
 * This object must be thread safe as multiple
 * requests may access it at the same time. The object is made
 * available in the context as: $scarabG
 * <p>
 * The design goals of the Scarab*API is to enable a <a
 * href="http://jakarta.apache.org/turbine/pullmodel.html">pull based
 * methodology</a> to be implemented.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:dr@bitonic.com">Douglas B. Robertson</a>
 * @version $Id$
 */
public class ScarabGlobalTool
    implements ApplicationTool
{
    private static int moduleCodeLength = 0;
    
    private static final String REGEX_URL =
        "s%\\b(?:[hH][tT]{2}[pP][sS]{0,1}|[fF][tT][pP]):[^ \\t\\n<>\"]+[\\w/]*%<a href=\"$0\">$0</a>%g";
    private static final String REGEX_MAILTO =
        "s%\\b(?:([mM][aA][iI][lL][tT][oO])):([^ \\t\\n<>\"]+[\\w/])*%<a href=\"$0\">$2</a>%g";

    private static Perl5Util perlUtil = new Perl5Util();

    private static final Logger LOG = 
        Logger.getLogger("org.tigris.scarab");

    /**
     * holds the Scarab constants
     */
    private FieldMethodizer constant = null;
    
    /**
     * holds the Scarab security permission constants
     */
    private FieldMethodizer security = null;

    /**
     * holds the Scarab parameter name constants
     */
    private FieldMethodizer parameterName = null;

    private static final String BUILD_VERSION = 
        Turbine.getConfiguration().getString("scarab.build.version", "");

    private static RenderContext context = new BaseRenderContext();
    private static RenderEngine engine = new ScarabRenderEngine();

    
    public void init(Object data)
    {
    }
    
    public void refresh()
    {
    }
    
    /**
     * Constructor does initialization stuff
     */    
    public ScarabGlobalTool()
    {
        constant = new FieldMethodizer(
            "org.tigris.scarab.util.ScarabConstants");
        security = new FieldMethodizer(
            "org.tigris.scarab.services.security.ScarabSecurity");
        parameterName = new FieldMethodizer(
            "org.tigris.scarab.om.GlobalParameter");

    	if(context.getRenderEngine() == null)
    	{
    	    context.setRenderEngine(engine);
    	}
    }

    /**
     * returns Scarab's build version.
     */
    public String getBuildVersion()
    {
        return BUILD_VERSION;
    }
    
    /**
     * holds the Scarab constants. it will be available to the template system
     * as $scarabG.Constant.CONSTANT_NAME.
     */
    public FieldMethodizer getConstant()
    {
        return constant;
    }
    
    /**
     * holds the Scarab permission constants.  It will be available to 
     * the template system as $scarabG.PERMISSION_NAME.
     */
    public FieldMethodizer getPermission()
    {
        return security;
    }

    /**
     * holds the names of parameters that are configurable through the ui.
     */
    public FieldMethodizer getParameterName()
    {
        return parameterName;
    }

    public String replace(String text, String a, String b)
    {
        return StringUtils.replace(text, a, b);
    }

    public GlobalParameterManager getParameter()
    {
        return GlobalParameterManager.getManager();
    }

    /**
     * Returns a list of all the permissions in use by scarab.  
     *
     * @return a <code>List</code> of <code>String</code>s
     */
    public List getAllPermissions()
    {
        return ScarabSecurity.getAllPermissions();
    }
    
    /**
     * Gets a List of all of the data (non-user) Attribute objects.
     */
    public List getAllAttributes()
        throws Exception
    {
        return AttributePeer.getAttributes();
    }
    
    /**
     * Gets a List of all of the Attribute objects by type.
     */
    public List getAttributes(String attributeType)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, false);
    }

    /**
     * Gets a List of all of the  data (non-user) Attribute objects.
     * Passes in sort criteria.
     */
    public List getAllAttributes(String attributeType, boolean includeDeleted,
                                 String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, includeDeleted, 
                                           sortColumn, sortPolarity);
    }

    /**
     * Gets a List of all of the  data (non-user) Attribute objects.
     * Passes in sort criteria.
     */
    public List getAllAttributes(String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(sortColumn, sortPolarity);
    }
    
    /**
     * Gets a List of all of user Attributes.
     */
    public List getUserAttributes()
        throws Exception
    {
        return AttributePeer.getAttributes("user");
    }


    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getUserAttributes(String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes("user", false, sortColumn, sortPolarity);
    }
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getUserAttributes(boolean includeDeleted, String sortColumn, 
                                  String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes("user", includeDeleted, sortColumn, sortPolarity);
    }

    /**
     * Gets a List of all of user Attribute objects.
     */
    public List getAttributes(String attributeType, boolean includeDeleted, 
                              String sortColumn, String sortPolarity)
        throws Exception
    {
        return AttributePeer.getAttributes(attributeType, includeDeleted,
                                           sortColumn, sortPolarity);
    }

    public List getAllIssueTypes()
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(false, "name", "asc");
    }

    /**
     * gets a list of all Issue Types 
     */
    public List getAllIssueTypes(boolean deleted)
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(deleted, "name", "asc");
    }
    
    /**
     * Gets a List of all of the Attribute objects.
     */
    public List getAllIssueTypes(boolean deleted, String sortColumn, 
                                 String sortPolarity)
        throws Exception
    {
        return IssueTypePeer.getAllIssueTypes(deleted, sortColumn, sortPolarity);
    }
    
    /**
     * Get the list of available activityType codes.
     * @return
     */
    public List getAllNotificationTypeCodes()
    {
        Set activityTypes = ActivityType.getActivityTypeCodes();
        List result = new Vector(activityTypes);
        return result;
    }
    
    /**
     * Remap Activity code to resource id. Necessary due to
     * incompatible name conventions.
     * @param code
     * @return
     */
    public String getActivityTypeLabelResource(String code)
    {
        return ActivityType.getResourceId(code);
    }
    
    
    /**
     * Return the list of available NotificationRules for
     * the given  user in the given module
     * @param moduleId
     * @param userId
     * @param activityCode
     * @return
     * @throws TorqueException
     */
    public List getCustomization(Object moduleId, Object userId, Object activityCode) throws TorqueException
    {
        NotificationRulePeer nfp = new NotificationRulePeer();
        List result = nfp.getCustomization(moduleId, userId, activityCode);
        return result;
    }

    public static Notification getEmptyNotificationFor(ScarabUser user, ScarabModule module) throws TorqueException
    {
        return new Notification(user, module);
    }
    
    public static NotificationRule getNotificationRule(Integer moduleId, Integer userId, String activityCode) throws ScarabException
    {
        NotificationRule result = NotificationRuleManager.getNotificationRule(moduleId, userId, activityCode);
        return result;
    }

    /**
     * Return the NotificationRule with the given RuleId.
     * This method has mainly been created for ConditionEdit.vm 
     * @param ruleId
     * @return
     * @throws TorqueException
     */
    public static NotificationRule getNotificationRule(Integer ruleId) throws TorqueException
    {
        NotificationRule result = NotificationRuleManager.getInstance(ruleId);
        return result;
    }

    
    /**
     * Makes the workflow tool accessible.
     * @throws ScarabException 
     */
    public static Workflow getWorkflow() throws ScarabException
    {
        return WorkflowFactory.getInstance();
    }
    
    /** 
     * Returns a List of users based on the given search criteria. This method
     * is an overloaded function which returns an unsorted list of users.
     *
     * @param searchField the name of the database attribute to search on
     * @param searchCriteria the search criteria to use within the LIKE command
     * @return a List of users matching the specifed criteria
     *
     */
    public List getSearchUsers(String searchField, String searchCriteria)
        throws Exception
    {
        return (getSearchUsers(searchField, searchCriteria, null, null));
    }
    
    /** 
     * Returns a List of users based on the given search criteria and orders
     * the list by the specified field.  The method will use the LIKE
     * SQL command and perform a search as such (assuming 'doug' is
     * specified as the search criteria:
     * <code>WHERE some_field LIKE '%doug%'</code>
     *
     * @param searchField the name of the database attribute to search on
     * @param searchCriteria the search criteria to use within the LIKE command
     * @param orderByField the name of the database attribute to order the list by
     * @param ascOrDesc either "ASC" of "DESC" specifying the order to sort in
     * @return a List of users matching the specifed criteria
     */
    /**
     * Describe <code>getSearchUsers</code> method here.
     *
     * @param searchField a <code>String</code> value
     * @param searchCriteria a <code>String</code> value
     * @param orderByField a <code>String</code> value
     * @param ascOrDesc a <code>String</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getSearchUsers(String searchField, String searchCriteria, 
                               String orderByField, String ascOrDesc)
        throws Exception
    {
        ArrayList userSearchList = new ArrayList();
        String lSearchField = "";
        String lOrderByField = "";
        
        Criteria criteria = new Criteria();
        
        // add the input from the user
        if (searchCriteria != null && searchCriteria.length() > 0)
        {
            if (searchField.equals("FIRST_NAME"))
            {
                lSearchField = ScarabUser.FIRST_NAME;
            }
            else if (searchField.equals("LAST_NAME"))
            {
                lSearchField = ScarabUser.LAST_NAME;
            }
            else if (searchField.equals("LOGIN_NAME"))
            {
                lSearchField = ScarabUser.USERNAME;
            }
            else
            {
                lSearchField = ScarabUser.EMAIL;
            }
            
            // FIXME: Probably shouldn't be using ScarabUserPeerImpl here
            // What should we do to get the right table name?
            lSearchField = ScarabUserImplPeer.getTableName() + '.' + lSearchField;
            
            criteria = criteria.add(lSearchField,
                                        (Object)('%' + searchCriteria.trim() + '%'),Criteria.LIKE);
        }
        
        // sort the results
        if (orderByField != null && orderByField.length() > 0)
        {
            if (orderByField.equals("FIRST_NAME"))
            {
                lOrderByField = ScarabUser.FIRST_NAME;
            }
            else if (orderByField.equals("LAST_NAME"))
            {
                lOrderByField = ScarabUser.LAST_NAME;
            }
            else if (orderByField.equals("LOGIN_NAME"))
            {
                lOrderByField = ScarabUser.USERNAME;
            }
            else
            {
                lOrderByField = ScarabUser.EMAIL;
            }
            
            // FIXME: Probably shouldn't be using ScarabUserPeerImpl here
            // What should we do to get the right table name?
            lOrderByField = ScarabUserImplPeer.getTableName() + '.' + lOrderByField;
            
            if (ascOrDesc != null && ascOrDesc.equalsIgnoreCase("DESC"))
            {
                criteria = criteria.addDescendingOrderByColumn(lOrderByField);
            } 
            else
            {
                criteria = criteria.addAscendingOrderByColumn(lOrderByField);
            }
        }
        
        User[] tempUsers = TurbineSecurity.getUsers(criteria);  
        for (int i=0; i < tempUsers.length; i++)
        {
            userSearchList.add(i, tempUsers[i]);
        }
        return (userSearchList);
    }

    /**
     * Create a list of Modules from the given list of issues.  Each
     * Module in the list of issues will only occur once in the list of 
     * Modules.
     */
    public List getModulesFromIssueList(List issues)
        throws TorqueException
    {
        return ModuleManager.getInstancesFromIssueList(issues);
    }

    public MITListManager getMITListManager()
    {
        return MITListManager.getManager();
    }

    /**
     * Get a new Date object initialized to the current time.
     * @return a <code>Date</code> value
     */
    public Date getNow()
    {
        return new Date();
    }

    /**
     * Creates a new array with elements reversed from the given array.
     *
     * @param a the orginal <code>Object[]</code>
     * @return a new <code>Object[]</code> with values reversed from the 
     * original
     */
    public Object[] reverse(Object[] a)
    {
        Object[] b = new Object[a.length];
        for (int i=a.length-1; i>=0; i--) 
        {
            b[a.length-1-i] = a[i];
        }
        return b;
    }

    /**
     * Creates a new <code>List</code> with elements reversed from the
     * given <code>List</code>.
     *
     * @param a the orginal <code>List</code>
     * @return a new <code>List</code> with values reversed from the 
     * original
     */
    public List reverse(List a)
    {
        int size = a.size();
        List b = new ArrayList(size);
        for (int i=size-1; i>=0; i--) 
        {
            b.add(a.get(i));
        }
        return b;
    }

    /**
     * Creates a view of the portion of the given
     * <code>List</code> between the specified <code>fromIndex</code>, inclusive, and
     * <code>toIndex</code>, exclusive.
     * The list returned by this method is backed by the original, so changes
     * to either affect the other.
     *
     * @param a the orginal <code>List</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
     * @return a derived <code>List</code> with a view of the original
     */
    public List subset(List a, Integer fromIndex, Integer toIndex)
    {
        int from = Math.min(fromIndex.intValue(), a.size());         
        from = Math.max(from, 0);
        int to = Math.min(toIndex.intValue(), a.size()); 
        to = Math.max(to, from); 
        return a.subList(from, to);
    }

    /**
     * Creates a new array with a view of the portion of the given array
     * between the specified fromIndex, inclusive, and toIndex, exclusive
     *
     * @param a the orginal <code>Object[]</code>
     * @param fromIndex the start index of the returned subset
     * @param toIndex the end index of the returned subset
     * @return a new <code>Object[]</code> with a view of the original
     */
    public Object[] subset(Object[] a, Integer fromIndex, Integer toIndex)
    {
        int from = Math.min(fromIndex.intValue(), a.length);
        from = Math.max(from, 0);
        int to = Math.min(toIndex.intValue(), a.length); 
        to = Math.max(to, from); 
        Object[] b = new Object[from-to];
        for (int i=from-1; i>=to; i--) 
        {
            b[i-to] = a[i];
        }
        return b;
    }

    /**
     * Velocity has no way of getting the size of an <code>Object[]</code>
     * easily. Usually this would be done by calling obj.length
     * but this doesn't work in Velocity.
     * @param obj the <code>Object[]</code>
     * @return the number of objects in the <code>Object[]</code> or -1 if obj is null
     */
    public int sizeOfArray(Object[] obj)
    {
        return (obj == null) ? -1 : obj.length;
    }

    public boolean isString(Object obj)
    {
        return obj instanceof String;
    }

    /**
     * Breaks text into a list of Strings.  Text is separated into tokens
     * at characters given in delimiters.  The delimiters are not part
     * of the resulting tokens. if delimiters is empty or null, "\n" is used.
     *
     * @param text a <code>String</code> value
     * @param delimiters a <code>String</code> value
     * @return a <code>List</code> value
     */
    public Enumeration tokenize(String text, String delimiters)
    {
        if (delimiters == null || delimiters.length() == 0) 
        {
            delimiters = "\n";
        }

        if (text == null) 
        {
            text = "";
        }
        
        StringTokenizer st = new StringTokenizer(text, delimiters);
        return st;
    }

    public List linkIssueIds(Module module, String text)
    {
        List result = null;
        try
        {
            result = IssueIdParser.tokenizeText(module, text);
        }
        catch (Exception e)
        {
            // return the text as is and log the error
            result = new ArrayList(1);
            result.add(text);
            Log.get().warn("Could not linkify text: " + text, e);
        }
        return result;
    }
    
    /**
     * <p>Converts a text string to HTML by:</p>
     * <ul>
     *   <li>replacing reserved characters with equivalent HTML entities</li>
     *   <li>adding hyperlinks for URLs</li>
     *   <li>adding hyperlinks for issue references</li>
     * </ul>
     * @param text The text string to convert.
     * @param link 
     * @param currentModule The active module.
     * @return A SkipFiltering object which contains the generated HTML. 
     * @throws Exception 
     */
    public SkipFiltering textToHTML(String text,
                                    ScarabLink link,
                                    Module currentModule) throws Exception
    {
        String renderEngine = currentModule.getCommentRenderingEngine();
        String txt;
        if(renderEngine.equals("radeox"))
        {
            txt = engine.render(text, context);
        }
        else
        {
            txt = "<pre>" + 
                  perlUtil.substitute(REGEX_URL,
                  perlUtil.substitute(REGEX_MAILTO,
                  ReferenceInsertionFilter.filter(text)))
                  + "</pre>";
        }
        
        return new SimpleSkipFiltering(ScarabUtil.linkifyText(txt, link, currentModule));
    }

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category is "org.tigris.scarab". 
     *
     * @param s message to log
     */
    public void log(String s)
    {
        LOG.debug(s);
    }

    /**
     * Logs a message at the debug level.  Useful for "I am here" type 
     * messages. The category in which to log is also specified. 
     *
     * @param category log4j Category
     * @param s message to log
     */
    public void log(String category, String s)
    {
        Logger.getLogger(category).debug(s);
    }

    /**
     * Prints a message to standard out.  Useful for "I am here" type 
     * messages. 
     *
     * @param s message to log
     */
    public void print(String s)
    {
        System.out.println(s);
    }

    /**
     * Provides the site name for the top banner.
     *
     * @return the configured site name
     */
    public String getSiteName()
    {
        String siteName = 
            Turbine.getConfiguration().getString("scarab.site.name","");

        if (siteName == null)
        {
            siteName = "";
        }
        return siteName;
    }

    /**
     * Provides the site logo for the top banner.
     *
     * @return the configured site logo
     */
    public String getSiteLogo()
    {
        String siteLogo = 
            Turbine.getConfiguration().getString("scarab.site.logo","");

        if (siteLogo == null)
        {
            siteLogo = "";
        }
        return siteLogo;
    }

    /**
     * Provides the maximum number of public modules to be shown on the
     * login screen. the number is stored in the property
     * scarab.public.modules.display.count
     * If no number is specified, this method returns -1
     *
     * @return the number specified in scarab.public.modules.display.count
     */
    public int getPublicModulesDisplayCount()
    {
        String publicModulesDisplayCount = 
            Turbine.getConfiguration().getString("scarab.public.modules.display.count","-1");
        return Integer.parseInt(publicModulesDisplayCount);
    }

    /**
     * Returns an <code>int</code> representation of the given
     * <code>Object</code> whose toString method should be a valid integer.
     * If the <code>String</code> cannot be parsed <code>0</code> is returned.
     * @param obj the object
     * @return the <code>int</code> representation of the <code>Object</code>
     *  if possible or <code>0</code>.
     */
    public int getInt(Object obj)
    {
        int result = 0;
        if (obj != null) 
        {
            try 
            {
                result = Integer.parseInt(obj.toString());
            }
            catch (Exception e)
            {
                Log.get().error(obj + " cannot convert to an integer.", e);
            }   
        }
        return result;
    }

    public int getCALENDAR_YEAR_FIELD()
    {
        return Calendar.YEAR;
    }

    public int getCALENDAR_MONTH_FIELD()
    {
        return Calendar.MONTH;
    }

    public int getCALENDAR_DAY_FIELD()
    {
        return Calendar.DAY_OF_MONTH;
    }

    public int getCALENDAR_HOUR_FIELD()
    {
        return Calendar.HOUR_OF_DAY;
    }

    public Date addApproxOneHour(Date date)
    { 
        date.setTime(date.getTime() + 3599999);
        return date;
    }

    /**
     * Delegates to Velocity's <code>templateExists()</code> method.
     */
    public boolean templateExists(String template)
    {
        return ((TurbineVelocityService) TurbineServices
                .getInstance().getService(VelocityService.SERVICE_NAME))
            .templateExists(template);
    }

    /**
     * @return
     */
    public synchronized static int getModuleCodeLength() {
        if (moduleCodeLength == 0)
        {
            try
            {
                moduleCodeLength = Integer.parseInt(Turbine.getConfiguration().
                                   getString("scarab.module.code.length", "4"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                moduleCodeLength = 4;
            }
        }
        return moduleCodeLength;
    }

    /**
     * @return
     */
    public static int getModuleCodeLengthPadded() {
        return getModuleCodeLength() + 6;
    }

    /**
     * @return  Return the current turbine configuration with all keys included
     */
    public Configuration getTurbineConfiguration()
    {
        return Turbine.getConfiguration();
    }
    
    /**
     * @return  Returns a lexicographic ascending sorted list of all keys from the current turbine configuration 
     */
    public List getTurbineConfigurationKeysSorted()
    {
        // TODO: Turbine.configuration should be encapsulated by GlobalParameterManager
        ArrayList sortedKeys = new ArrayList();
        for(Iterator keys = Turbine.getConfiguration().getKeys();keys.hasNext();) {
            sortedKeys.add(keys.next());
        }
        Collections.sort(sortedKeys, String.CASE_INSENSITIVE_ORDER);
        return sortedKeys;
    }
    
    /**
     * @return Returns the string value of a turbine property. If the property
     * does not map to a String, return Object.toString() instead.
     */
    public String getTurbineProperty(String key)
    {
        String result = null;
        try
        {
        	result = getTurbineConfiguration().getString(key);
        }
        catch( ConversionException ce)
        {
        	// This happens, if the Turbine Property contains data, which can
        	// not be converted to a String. This has been seen on JBOSS.
        	// Note: getProperty() does not resolve ${} values, so it is not an
        	// option to use it instead of getString(). But in the case of
        	// non convertible objects, getProperty().toString() is the best bet.
        	// In my opinion this should be handled inside of Configuration, 
        	// or at least we should be given a test method Configuration.isString(key)
        	// So we could avoid this stuff here...
        	// just my 2 cents. (hussayn dabbous)
        	result = getTurbineConfiguration().getProperty(key).toString();
        }
        return result;
    }
}
