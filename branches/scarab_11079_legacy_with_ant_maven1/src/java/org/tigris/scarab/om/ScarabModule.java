package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
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

// JDK classes
import com.workingdogs.village.DataSetException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;

// Commons classes
import org.apache.commons.lang.StringUtils;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.SqlEnum;

import java.sql.Connection;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.RoleSet;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;

// Scarab classes
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.tools.ScarabUserTool;
import org.tigris.scarab.tools.localization.L10NKey;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabPaginatedList;
import org.tigris.scarab.util.ScarabLocalizedTorqueException;
import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.services.cache.ScarabCache;

// FIXME! do not like referencing servlet inside of business objects
// though I have forgotten how I might avoid it
import org.apache.turbine.Turbine;
import org.apache.fulcrum.security.impl.db.entity
    .TurbinePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.impl.db.entity
    .TurbineRolePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserPeer;
import org.tigris.scarab.util.ScarabRuntimeException;

/**
 * The ScarabModule class is the focal point for dealing with
 * Modules. It implements the concept of a Module which is a
 * single module and is the base interface for all Modules. In code,
 * one should <strong>never reference ScarabModule directly</strong>
 * -- use its Module interface instead.  This allows us to swap
 * out Module implementations by modifying the Scarab.properties
 * file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class ScarabModule
    extends BaseScarabModule
    implements Persistent, Module, Group
{
    private static final String GET_USERS = "getUsers";

    protected static final Integer ROOT_ID = new Integer(0); 
    private static final String PROJECT_OWNER_ROLE = "Project Owner";
    private String httpDomain = null;
    private String instanceId = null;
    private String port       = null;
    private String scheme     = null;
    private String scriptName = null;
    
    public static final String GET_DEFAULTREPORT="getDefaultReport";
    
    /**
     * Get the value of domain.
     * @return value of domain.
     */
    public String getHttpDomain()
    {
        if (httpDomain == null || httpDomain.length() == 0)
        {
            try
            {
                httpDomain = GlobalParameterManager
                    .getString(ScarabConstants.HTTP_DOMAIN);
            }
            catch (Exception e)
            {
                getLog().error("Error getting HTTP_DOMAIN:", e);
            }
        }
        return httpDomain;
    }
    
    /**
     * Set the value of domain.
     * @param v  Value to assign to domain.
     */
    public void setHttpDomain(String v)
    {
        if (v != null)
        {
            this.httpDomain = v;
        }
    }

    /**
     * Get the value of the Scarab instance id.
     * @return value of domain.
     */
    public String getScarabInstanceId()
    {
        if (instanceId == null || instanceId.length() == 0)
        {
            try
            {
                instanceId = GlobalParameterManager
                    .getString(ScarabConstants.INSTANCE_ID);
            }
            catch (Exception e)
            {
                getLog().error("Error getting DOMAIN_NAME:", e);
            }
        }
        return instanceId;
    }
    
    /**
     * Set the value of Scarab domain name.
     * The value can be an arbirtrary String.
     * Note: This instance attriute is NOT related to ip/email-domains!
     * @param v  Value to assign to domain.
     */
    public void setScarabInstanceId(String v)
    {
        if (v != null)
        {
            this.instanceId = v;
        }
    }

    /**
     * Get the value of port.
     * @return value of port.
     */
    public String getPort() 
        throws TorqueException
    {
        if (port == null)
        {
            port = GlobalParameterManager
                    .getString(ScarabConstants.HTTP_PORT);
        }
        return port;
    }
    
    /**
     * Set the value of port.
     * @param v  Value to assign to port.
     */
    public void setPort(String v)
    {
        if (v != null)
        {
            this.port = v;
        }
    }

    /**
     * Get the value of scheme.
     * @return value of scheme.
     */
    public String getScheme() 
        throws TorqueException
    {
        if (scheme == null)
        {
            scheme = GlobalParameterManager
                    .getString(ScarabConstants.HTTP_SCHEME);
        }
        return scheme;
    }
    
    /**
     * Set the value of scheme.
     * @param v  Value to assign to scheme.
     */
    public void setScheme(String v) 
    {
        if (v != null)
        {
            this.scheme = v;
        }
    }

    /**
     * Get the value of scriptName.
     * @return value of scriptName.
     */
    public String getScriptName() 
        throws TorqueException
    {
        if (scriptName == null)
        {
            scriptName = GlobalParameterManager
                    .getString(ScarabConstants.HTTP_SCRIPT_NAME);
        }
        return scriptName;
    }
    
    /**
     * Set the value of scriptName.
     * @param v  Value to assign to scriptName.
     */
    public void setScriptName(String v) 
    {
        if (v != null)
        {
            this.scriptName = v;
        }
    }

    /**
     * @see org.tigris.scarab.om.Module#getUsers(String)
     */
    public ScarabUser[] getUsers(String permission)
    {
        List perms = new ArrayList(1);
        perms.add(permission);
        return getUsers(perms);
    }

    /**
     * @see org.tigris.scarab.om.Module#getUsers(List)
     */
    public ScarabUser[] getUsers(List permissions)
    {
        ScarabUser[] result = null;
        Object obj = ScarabCache.get(this, GET_USERS, 
                                     (Serializable)permissions); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.setDistinct();
            if (permissions.size() == 1) 
            {
                crit.add(TurbinePermissionPeer.NAME, permissions.get(0));
            }
            else if (permissions.size() > 1)
            {
                crit.addIn(TurbinePermissionPeer.NAME, permissions);
            }      
            
            if (permissions.size() >= 1)
            {
                ArrayList groups = new ArrayList(2);
                groups.add(getModuleId());
                groups.add(ROOT_ID);
                crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                             TurbineRolePermissionPeer.PERMISSION_ID);
                crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                             TurbineUserGroupRolePeer.ROLE_ID);
                crit.addIn(TurbineUserGroupRolePeer.GROUP_ID, groups);
                crit.addJoin(ScarabUserImplPeer.USER_ID, 
                             TurbineUserGroupRolePeer.USER_ID);
                
                crit.add(ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE),(Object)ScarabUser.DELETED,Criteria.NOT_EQUAL);
                crit.addAscendingOrderByColumn(ScarabUserImplPeer.FIRST_NAME);

                try
                {
                    User[] users = TurbineSecurity.getUsers(crit);
                    result = new ScarabUser[users.length];
                    for (int i=result.length-1; i>=0; i--) 
                    {
                        result[i] = (ScarabUser)users[i];
                    }
                }
                catch (Exception e)
                {
                    getLog().error(
                        "An exception prevented retrieving any users", e);
                    // this method should probably throw the exception, but
                    // until the interface is changed, wrap it in a RuntimeExc.
                    throw new RuntimeException(
                        "Please check turbine.log for more info: " + 
                        e.getMessage()); //EXCEPTION
                }
            }
            else 
            {
                result = new ScarabUser[0];
            }
            ScarabCache.put(result, this, GET_USERS, 
                            (Serializable)permissions);
        }
        else 
        {
            result = (ScarabUser[])obj;
        }
        return result;
    }


    /**
     * @see org.tigris.scarab.om.Module#getUsers(String, String, String, String, IssueType)
     * @param mitList MITs to restrict the user's search. If null, it will not be restricted.
     */
    public ScarabPaginatedList getUsers(final String name, 
                                        final String username, 
                                        final MITList mitList, 
                                        final int pageNum, 
                                        final int resultsPerPage,
                                        final String sortColumn, 
                                        final String sortPolarity,
                                        final boolean includeCommitters)
        throws TorqueException, DataSetException
    {
        final int polarity = sortPolarity.equals("asc") ? 1 : -1; 
        List result = null;
        ScarabPaginatedList paginated = null; 

        final Comparator c = new Comparator() 
        {
            public int compare(Object o1, Object o2) 
            {
                int i = 0;
                if ("username".equals(sortColumn))
                {
                    i =  polarity * ((ScarabUser)o1).getUserName()
                              .compareTo(((ScarabUser)o2).getUserName());
                }
                else
                {
                    i =  polarity * ((ScarabUser)o1).getName()
                             .compareTo(((ScarabUser)o2).getName());
                }
                return i;
             }
        };

            final Criteria crit = new Criteria();//
            final Criteria critCount = new Criteria();
            critCount.addSelectColumn("COUNT(DISTINCT " + TurbineUserPeer.USERNAME + ")");
            if (mitList != null)
            {
                final List modules = mitList.getModules();
                for (Iterator it = modules.iterator(); it.hasNext(); )
                {
                    final Module mod = (Module)it.next();
                    final List perms = mitList.getUserAttributePermissions();
                    if (includeCommitters && !perms.contains(org.tigris.scarab.services.security.ScarabSecurity.ISSUE__ENTER))
                    {
                        perms.add(org.tigris.scarab.services.security.ScarabSecurity.ISSUE__ENTER);
                    }

                    crit.addIn(TurbinePermissionPeer.PERMISSION_NAME, perms);
                    crit.setDistinct();
                    critCount.addIn(TurbinePermissionPeer.PERMISSION_NAME, perms);
                }
                crit.addIn(TurbineUserGroupRolePeer.GROUP_ID, mitList.getModuleIds());
                critCount.addIn(TurbineUserGroupRolePeer.GROUP_ID, mitList.getModuleIds());
            }
            crit.addJoin(TurbineUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
            crit.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePermissionPeer.ROLE_ID);
            crit.addJoin(TurbineRolePermissionPeer.PERMISSION_ID, TurbinePermissionPeer.PERMISSION_ID);
            critCount.addJoin(TurbineUserPeer.USER_ID, TurbineUserGroupRolePeer.USER_ID);
            critCount.addJoin(TurbineUserGroupRolePeer.ROLE_ID, TurbineRolePermissionPeer.ROLE_ID);
            critCount.addJoin(TurbineRolePermissionPeer.PERMISSION_ID, TurbinePermissionPeer.PERMISSION_ID);            

            if (name != null)
            {
                int nameSeparator = name.indexOf(" ");
                if (nameSeparator != -1) 
                {
                    final String firstName = name.substring(0, nameSeparator);
                    final String lastName = name.substring(nameSeparator+1, name.length());
                    crit.add(ScarabUserImplPeer.FIRST_NAME, 
                             addWildcards(firstName), Criteria.LIKE);
                    crit.add(ScarabUserImplPeer.LAST_NAME, 
                             addWildcards(lastName), Criteria.LIKE);
                    critCount.add(ScarabUserImplPeer.FIRST_NAME, 
                            addWildcards(firstName), Criteria.LIKE);
                    critCount.add(ScarabUserImplPeer.LAST_NAME, 
                            addWildcards(lastName), Criteria.LIKE);
                    
                }
                else 
                {
                    String[] tableAndColumn = StringUtils.split(ScarabUserImplPeer.FIRST_NAME, ".");
                    final Criteria.Criterion fn = crit.getNewCriterion(tableAndColumn[0],
                                                                 tableAndColumn[1], 
                                                                 addWildcards(name), 
                                                                 Criteria.LIKE);
                    tableAndColumn = StringUtils.split(ScarabUserImplPeer.LAST_NAME, ".");
                    final Criteria.Criterion ln = crit.getNewCriterion(tableAndColumn[0],
                                                                 tableAndColumn[1], 
                                                                 addWildcards(name), 
                                                                 Criteria.LIKE);
                    fn.or(ln);
                    crit.add(fn);
                    critCount.add(fn);
                }
            }

            if (username != null)
            {
                crit.add(ScarabUserImplPeer.LOGIN_NAME, 
                         addWildcards(username), Criteria.LIKE);
                critCount.add(ScarabUserImplPeer.LOGIN_NAME, 
                        addWildcards(username), Criteria.LIKE);
            }
            
            String col = ScarabUserImplPeer.FIRST_NAME;
            if (sortColumn.equals("username"))
                col = ScarabUserImplPeer.USERNAME;
            if (sortPolarity.equals("asc"))
            {
                crit.addAscendingOrderByColumn(col);
            }
            else
            {
                crit.addDescendingOrderByColumn(col);
            }
            
            final int totalResultSize = ScarabUserImplPeer.getUsersCount(critCount);
            
            crit.setOffset((pageNum - 1)* resultsPerPage);
            crit.setLimit(resultsPerPage);
            result = ScarabUserImplPeer.doSelect(crit);

            // if there are results, sort the result set
            if (totalResultSize > 0 && resultsPerPage > 0)
            {

                paginated = new ScarabPaginatedList(result, totalResultSize,
                                                    pageNum,
                                                    resultsPerPage);
            }
            else 
            {
                paginated = new ScarabPaginatedList();
            }
        
        return paginated;
    }

    /**
     * @see org.tigris.scarab.om.Module#getUsers(String, String, String, String, IssueType)
     * This implementation adds wildcard prefix and suffix and performs an SQL 
     * LIKE query for each of the String args that are not null.
     * WARNING: This is potentially a very EXPENSIVE method.
     */
    public List getUsers(String firstName, String lastName, 
                         String username, String email, IssueType issueType)
        throws TorqueException
    {
        List result = null;
        Serializable[] keys = {this, GET_USERS, firstName, lastName, 
                               username, email, issueType};
        Object obj = ScarabCache.get(keys); 
        if (obj == null) 
        {
            ScarabUser[] eligibleUsers = getUsers(getUserPermissions(issueType));
            if (eligibleUsers == null || eligibleUsers.length == 0) 
            {
                result = Collections.EMPTY_LIST;
            }
            else 
            {
                List userIds = new ArrayList();
                for (int i = 0; i < eligibleUsers.length; i++)
                {
                    userIds.add(eligibleUsers[i].getUserId());
                }
                Criteria crit = new Criteria();
                crit.addIn(ScarabUserImplPeer.USER_ID, userIds);
                
                if (firstName != null)
                {
                    crit.add(ScarabUserImplPeer.FIRST_NAME, 
                             addWildcards(firstName), Criteria.LIKE);
                }
                if (lastName != null)
                {
                    crit.add(ScarabUserImplPeer.LAST_NAME, 
                             addWildcards(lastName), Criteria.LIKE);
                }
                if (username != null)
                {
                    crit.add(ScarabUserImplPeer.LOGIN_NAME, 
                             addWildcards(username), Criteria.LIKE);
                }
                if (email != null)
                {
                    crit.add(ScarabUserImplPeer.EMAIL, addWildcards(email), 
                             Criteria.LIKE);
                }
                result = ScarabUserImplPeer.doSelect(crit);
            }
            ScarabCache.put(result, keys);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    private Object addWildcards(String s)
    {
        return new StringBuffer(s.length() + 2)
            .append('%').append(s).append('%').toString(); 
    }

    /**
     * Wrapper method to perform the proper cast to the BaseModule method
     * of the same name. FIXME: find a better way
     */
    public void setParent(Module v)
        throws TorqueException
    {
        super.setModuleRelatedByParentId(v);
        // setting the name to be null so that 
        // it gets rebuilt with the new information
        setName(null);
        resetAncestors();
    }

    /**
     * Cast the getScarabModuleRelatedByParentId() to a Module
     */
    public Module getParent()
        throws TorqueException
    {
        Module parent = super.getModuleRelatedByParentId();

        // The top level module has itself as parent.
        // Return null in this case, to avoid endless loops.
        if (this.getModuleId() == parent.getModuleId())
        {
            parent = null;
        }
        return parent;
    }

    /**
     * Override method to make sure the module name gets recalculated.
     *
     * @param id a <code>Integer</code> value
     */
    public void setParentId(Integer id)
        throws TorqueException
    {
        super.setParentId(id);
        // setting the name to be null so that 
        // it gets rebuilt with the new information
        setName(null);
        resetAncestors();
    }

    /**
     * This method returns a complete list of RModuleIssueTypes
     * which are not deleted, have a IssueType.PARENT_ID of 0 and
     * sorted ascending by PREFERRED_ORDER.
     */
    public List getRModuleIssueTypes()
        throws TorqueException
    {
        return super.getRModuleIssueTypes("preferredOrder","asc");
    }

    /**
     * The number of active issues within the module.
     *
     * @param user a <code>ScarabUser</code> value used to determine if
     * a count should be given.  
     * @return an <code>int</code> the number of issues entered for the 
     * module unless the user does not have permission to
     * search for issues in the given module, then a value of 0 will be
     * returned.  if resource limited, this method will return -1. 
     * @throws DataSetException 
     * @exception Exception if an error occurs
     */
    public int getIssueCount(ScarabUser user, AttributeOption attributeOption)
        throws TorqueException, DataSetException
    {
        Criteria crit = new Criteria();

        Integer attributeId = attributeOption.getAttributeId();
        Integer optionId    = attributeOption.getOptionId();

        crit.add(AttributeValuePeer.ATTRIBUTE_ID, attributeId);
        crit.add(AttributeValuePeer.OPTION_ID,optionId);
        crit.add(IssuePeer.MODULE_ID,getModuleId());
        crit.add(IssuePeer.MOVED, 0);
        crit.add(IssuePeer.DELETED, 0);
        crit.add(IssuePeer.ID_COUNT, 0, SqlEnum.GREATER_THAN);
        crit.addJoin(AttributeValuePeer.ISSUE_ID, IssuePeer.ISSUE_ID);
        crit.add(AttributeValuePeer.DELETED,0);
        int count = AttributeValuePeer.count(crit);
        return count;
    }
    
    
    /**
     * The number of active issues within the module.
     *
     * @param user a <code>ScarabUser</code> value used to determine if
     * a count should be given.  
     * @return an <code>int</code> the number of issues entered for the 
     * module unless the user does not have permission to
     * search for issues in the given module, then a value of 0 will be
     * returned.  if resource limited, this method will return -1. 
     * @throws DataSetException 
     * @exception Exception if an error occurs
     */
    public int getIssueCount(ScarabUser user)
        throws TorqueException, DataSetException
    {
        Criteria crit = new Criteria();
        crit.add(IssuePeer.MODULE_ID,getModuleId());
        crit.add(IssuePeer.DELETED,0);
        crit.add(IssuePeer.MOVED,0);
        crit.add(IssuePeer.ID_COUNT, 0, SqlEnum.GREATER_THAN);
        int count = IssuePeer.count(crit);
        return count;
    }
    
    
    /**
     * Returns RModuleAttributes associated with this Module.  Tries to find
     * RModuleAttributes associated directly through the db, but if none are
     * found it should look up the parent module tree until it finds a 
     * non-empty list.
     */
    public List getRModuleAttributes(Criteria crit)
        throws TorqueException
    {
        return super.getRModuleAttributes(crit);
    }

    /**
     * Returns associated RModuleOptions.  if a related AttributeOption is
     * deleted the RModuleOption will not show up in this list.
     *
     * @param crit a <code>Criteria</code> value
     * @return a <code>List</code> value
     */
    public List getRModuleOptions(Criteria crit)
        throws TorqueException
    {
        crit.addJoin(RModuleOptionPeer.OPTION_ID, 
                     AttributeOptionPeer.OPTION_ID)
            .add(AttributeOptionPeer.DELETED, false);
        return super.getRModuleOptions(crit);
    }


    public boolean allowsIssues()
    {
        return (true);
    }
    
    /**
     * Saves the module into the database
     * @throws ScarabRuntimeException when a TorqueException is thrown internally.
     */
    public void save() 
    {
        try
        {
            
            super.save();
        }
        catch (TorqueException e)
        {
            // a way to satisfy method signature regarding "throws" for Torque class and Group class.
            // that is hide it all by throwing a RuntimeException.
            // usuages of this method must be careful of this!
            throw new ScarabRuntimeException(new L10NKey("ScarabModule.save TorqueException <localize me>"),e); //EXCEPTION
        }
    }

    /**
     * Saves the module into the database. Note that this
     * cannot be used within a activitySet if the module isNew()
     * because dbCon.commit() is called within the method. An
     * update can be done within a activitySet though.
     */
    public void save(final Connection dbCon) 
        throws TorqueException
    {
        // if new, make sure the code has a value.
        if (isNew())
        {
            final Criteria crit = new Criteria();
            crit.add(ScarabModulePeer.MODULE_NAME, getRealName());
            crit.add(ScarabModulePeer.PARENT_ID, getParentId());
            // FIXME: this should be done with a method in Module
            // that takes the two criteria values as a argument so that other 
            // implementations can benefit from being able to get the 
            // list of modules. -- do not agree - jdm

            List result;
            try {
                result = ScarabModulePeer.doSelect(crit);
            }
            catch (TorqueException te)
            {
             throw new ScarabLocalizedTorqueException(
                     new ScarabException(
                             L10NKeySet.ExceptionTorqueGeneric, te));
            }
            
            if (result.size() > 0)
            {
                throw new ScarabLocalizedTorqueException(
                        new ScarabException(
                                L10NKeySet.ExceptionModuleAllreadyExists,
                        getRealName(), 
                        getParentId()));
            }

            final String code = getCode();
            if (code == null || code.length() == 0)
            {
                if (getParentId().equals(ROOT_ID))
                {
                    throw new ScarabLocalizedTorqueException(new ScarabException(L10NKeySet.ExceptionTopLevelModuleWithoutCode));
                }

                try
                {
                    setCode(getParent().getCode());
                }
                catch (Exception e)
                {
                    throw new ScarabLocalizedTorqueException(new ScarabException(L10NKeySet.ExceptionCantPropagateModuleCode, e));
                }
            }

            // need to do this before the relationship save below
            // in order to set the moduleid for the new module.
            super.save(dbCon);
            try
            {
                dbCon.commit();
            }
            catch (Exception e)
            {
                throw new ScarabLocalizedTorqueException(new ScarabException(L10NKeySet.ExceptionGeneric, e));
            }
            
            if (getOwnerId() == null) 
            {
                throw new ScarabLocalizedTorqueException(new  ScarabException(L10NKeySet.ExceptionSaveNeedsOwner));
            }
            // grant the ower of the module the Project Owner role
            try
            {
                final User user = ScarabUserManager.getInstance(getOwnerId());
 
                final Role role = TurbineSecurity.getRole(PROJECT_OWNER_ROLE);
                grant (user, role);
                setInitialAttributesAndIssueTypes();
            }
            catch (Exception e)
            {
                throw new ScarabLocalizedTorqueException(new ScarabException(L10NKeySet.ExceptionGeneric, e));
            }
        }
        else
        {
            super.save(dbCon);
        }
        
        // clear out the cache beause we want to make sure that
        // things get updated properly.
        ScarabCache.clear();
    }

    // *******************************************************************
    // Turbine Group implementation get/setName and save are defined in
    // parent class AbstractScarabModule
    // *******************************************************************

    /**
     * Removes a group from the system.
     *
     * @throws TurbineSecurityException if the Group could not be removed.
     */
    public void remove()
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented"); //EXCEPTION
    }

    /**
     * Renames the group.
     *
     * @param name The new Group name.
     * @throws TurbineSecurityException if the Group could not be renamed.
     */
    public void rename(String name)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented"); //EXCEPTION
    }

    /**
     * Grants a Role in this Group to an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Role.
     */
    public void grant(User user, Role role)
        throws TurbineSecurityException
    {
        TurbineSecurity.grant(user,this,role);

        // TODO: Needs to be refactored into the Users system?
        ScarabUserManager.getMethodResult().remove(user, ScarabUserManager.GET_ACL);
        ScarabUserManager.getMethodResult().remove(user, ScarabUserManager.HAS_ROLE_IN_MODULE, (Serializable)role, this);
    }

    /**
     * Grants Roles in this Group to an User.
     *
     * @param user An User.
     * @param roleSet A RoleSet.
     * @throws TurbineSecurityException if there is a problem while assigning
     * the Roles.
     */
    public void grant(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented"); //EXCEPTION
    }

    /**
     * Revokes a Role in this Group from an User.
     *
     * @param user An User.
     * @param role A Role.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Role.
     */
    public void revoke(User user, Role role)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented"); //EXCEPTION
    }

    /**
     * Revokes Roles in this group from an User.
     *
     * @param user An User.
     * @param roleSet a RoleSet.
     * @throws TurbineSecurityException if there is a problem while unassigning
     * the Roles.
     */
    public void revoke(User user, RoleSet roleSet)
        throws TurbineSecurityException
    {
        throw new TurbineSecurityException("Not implemented"); //EXCEPTION
    }

    /**
     * Used for ordering Groups.
     *
     * @param obj The Object to compare to.
     * @return -1 if the name of the other object is lexically greater than 
     * this group, 1 if it is lexically lesser, 0 if they are equal.
     */
    public int compareTo(Object obj)
    {
        //---------------------------------------------------------------------
        // dr@bitonic.com : commented out as per conversation with John McNally
        //   over IRC on 20-Dec-2001
        //---------------------------------------------------------------------
        //if (this.getClass() != obj.getClass())
        //{
        //    throw new ClassCastException();
        //}
        String name1 = ((Group)obj).getName();
        String name2 = this.getName();

        return name2.compareTo(name1);
    }


    /**
     * All emails related to this module will have a copy sent to
     * this address.  A system-wide default email address can be specified in 
     * Scarab.properties with the key: scarab.email.archive.toAddress
     */
    public String getArchiveEmail()
    {
        String email = super.getArchiveEmail();
        if (email == null || email.length() == 0) 
        {
            email = Turbine.getConfiguration()
                .getString(ScarabConstants.ARCHIVE_EMAIL_ADDRESS, null);
        }
        
        return email;
    }
    
    /** 
     * Examines the modules archiveEmail string (which may contain multiple
     * email targets) and returns only those entries, which are recognized as
     * local ScarabUsers.
     * Note: archiving ScarabUsers need to be specified by their exact username
     * as known in Scarab.
     * This method returns a set of ScarabUser instances.
     */
    public Set<ScarabUser> getArchivingScarabUsers()
    {
        Set<ScarabUser> expandedArchiveAddresses = new HashSet<ScarabUser>();
        
        String archiveAddresses = getArchiveEmail();
        if(archiveAddresses!=null)
        {
            StringTokenizer st = new StringTokenizer(archiveAddresses, ",;");
            while (st.hasMoreTokens())
            {
                String userName = st.nextToken().trim();
                ScarabUser user = ScarabUserTool.getUserByUserName(userName);
                if(user != null)
                {
                    expandedArchiveAddresses.add(user);
                }
            }
        }
        return expandedArchiveAddresses;
    }
    
    
    /** 
     * Examines the modules archiveEmail string (which may contain multiple
     * email targets) and returns only those entries, which are NOT recognized as
     * local ScarabUsers, hence interpreted as foreign Email Addresses.
     * Note: archivingMailAdresses must be valid EmailAdresses!
     * This method returns a set of Strings containing Email Addresses
     */
    public Set<String> getArchivingMailAddresses()
    {
        Set<String> expandedArchiveAddresses = new HashSet();
        
        String archiveAddresses = getArchiveEmail();
        if(archiveAddresses!=null)
        {
            StringTokenizer st = new StringTokenizer(archiveAddresses, ",;");
            while (st.hasMoreTokens())
            {
                String userName = st.nextToken().trim();
                ScarabUser user = ScarabUserTool.getUserByUserName(userName);
                if(user == null)
                {
                    expandedArchiveAddresses.add(userName);
                }
            }
        }
        return expandedArchiveAddresses;
    }
    
    
    
    
    /**
     * returns an array of Roles that can be approved without need for
     * moderation.
     */
    public String[] getAutoApprovedRoles()
    {
        return Turbine.getConfiguration()
            .getStringArray(ScarabConstants.AUTO_APPROVED_ROLES);
    }

    /**
     * Provides the flag, wether issue store needs valid reason.
     * Note: This method returns true, when the global variable
     * was not defined neither for this module nor for its ancestors.
     * This may be the case when you migrate from an
     * older version of scarab to a20++ where this parameter was not
     * used. Thus per default Scarab makes the field required.
     * 
     * @return true: yes, valid reason field needed; false: reason field may stay empty.
     */
    public boolean isIssueReasonRequired()
    {
        String key = GlobalParameter.ISSUE_REASON_REQUIRED;
        boolean result = true;
        try
        {
            result = GlobalParameterManager.
                getBooleanFromHierarchy(key, this, true); 
        }
        catch (TorqueException te)
        {
            getLog().error("isIssueReasonRequired(): " + te);
        }
        return result;
    }

    /**
     * Provides the flag, whether the reason field shall be hidden
     * from the interface. Note: This method returns false only,
     * if the corresponding flag has been set in the module editor AND 
     * the global parameter ISSUE_REASON_REQUIRED has been set to false!
     * @return
     */
    public boolean isIssueReasonVisible()
    {
        // Due to history this information is stored as
        // ISSUE_REASON_HIDDEN where it would have been 
        // more straight forward to use ISSUE_REASON_VISIBLE.
        // In order to not break existing implementations
        // we keep with the negative logic for now.
        String key = GlobalParameter.ISSUE_REASON_HIDDEN;
        
        boolean result = true; // per default show the issue reason field
        
        if (!isIssueReasonRequired())
        {
            try
            {
                result = !GlobalParameterManager.
                    getBooleanFromHierarchy(key, this, true); 
            }
            catch (TorqueException te)
            {
                getLog().error("isIssueReasonVisible(): " + te);
            }
        }
        return result;
    }
    
    

    /**
     * Determines if the value of isIssueReasonRequired is due to the configuration
     * of this module or inherited from ancestors or default configuration. The Global module
     * does never inherite the value, that's by default 'True'.
     * @return True if the configuration is inherited.
     */
    public boolean isIssueReasonRequiredInherited()
    {
        if (this.isGlobalModule())
            return false;
        
        String val = null;
        try
        {
            val = GlobalParameterManager.getString(
                    GlobalParameter.ISSUE_REASON_REQUIRED,
                    this);
        }
        catch (TorqueException te)
        {
            getLog().error("isIssueReasonRequiredInherited(): " + te);
        }
        return (val == null || val.length()==0);
    }


    /**
     * Checks if an attempt to register a role for this module
     * needs a specific role. returns false, if no role is
     * needed. otherwise return true.
     * To get the needed role, @see #getRequiredRole()
     * @return
     */
    public boolean registerNeedsRequiredRole()
    {
        Role role = getRequiredRole();
        boolean result = (role != null);
        return result;
    }
    
    /**
     * Returns the required role for *any* access to this module
     * including for requesting roles.
     * @return
     */
    public Role getRequiredRole()
    {
        Role role = null;
        try
        {
            String roleName = GlobalParameterManager
               .getString(GlobalParameter.REQUIRED_ROLE_FOR_REQUESTING_ACCESS, this);
            if (roleName != null && roleName.length() > 0)
                role = TurbineSecurity.getRole(roleName);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return role;
    } 
    
    /**
     * Returns the comment rendering engine currently in use.
     * @return
     */
    public String getCommentRenderingEngine()
    {
        // TODO: We should return a RenderEngine here and hide
        //       all rendering details in the returned instance.
        //       currently we provide radeoz and plaintext only.
        //       this may change soon ;-) [HD]
        //
        String key = GlobalParameter.COMMENT_RENDER_ENGINE;
        String result = null;
        try
        {
            result = GlobalParameterManager.getString(key, this);
        }
        catch (Exception e)
        {
            getLog().error("getCommentRenderingEngine(): " + e);
        }
        
        if(result == null || result.equals(""))
        {
            result = ScarabConstants.COMMENT_RENDER_ENGINE;
        }
        return result;
    } 
    
    /**
     * Gets all module roles.
     */
    public List getRoles() 
    {
        return new ArrayList(0);
    }

    public String toString()
    {
        // This is required for caching.
        // For a deeper explanation refer to Issue.toString().
        return getModuleId()==null ? "new" : getModuleId().toString();
    }
    
    /**
     * Method returns all not deleted reports with scope module
     * @return
     * @throws TorqueException 
     */
    public List getNotDeletedModuleReports() 
        throws TorqueException
    {
        return ReportManager.getManager().getNotDeletedModuleReports(this);
    }

    /**
     * method returns the default report defined for the current module
     * @author jhoech
     */
    public ReportBridge getDefaultReport()
        throws Exception
    {     
        ReportBridge defaultReport=(ReportBridge) ModuleManager.getMethodResult()
           .get(this,GET_DEFAULTREPORT);
            
        if(defaultReport==null){
            String reportId = GlobalParameterManager.getString(GlobalParameter.DEFAULT_REPORT,this);
            Report report=reportId.equals("") ? null : 
                ReportManager.getInstance(new NumberKey(reportId));

            if(report!=null && !report.getDeleted())
            {
                defaultReport = new ReportBridge(report);     
                ModuleManager.getMethodResult().put(defaultReport,this,GET_DEFAULTREPORT);
            }
        }
            
        return defaultReport;
    }
}
