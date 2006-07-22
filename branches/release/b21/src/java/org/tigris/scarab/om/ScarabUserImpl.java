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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.security.impl.db.entity.TurbinePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineRolePeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineRolePermissionPeer;
import org.apache.fulcrum.security.impl.db.entity.TurbineUserGroupRolePeer;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.DataBackendException;
import org.apache.fulcrum.security.util.EntityExistsException;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.log4j.Logger;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.Turbine;
import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.AnonymousUserUtil;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;

import com.workingdogs.village.DataSetException;


/**
 * This class is an abstraction that is currently based around
 * Turbine's code. We can change this later. It is here so
 * that it is easier to change later to work under different
 * implementation needs.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class ScarabUserImpl 
    extends BaseScarabUserImpl 
    implements ScarabUser
{
    private static final Logger TORQUE_LOG = 
        Logger.getLogger("org.apache.torque");

    public static final String PASSWORD_EXPIRE = "PASSWORD_EXPIRE";
    
    private AbstractScarabUser internalUser;
    
    /**
     * The maximum length for the unique identifier used at user
     * creation time.
     */
    private static final int UNIQUE_ID_MAX_LEN = 10;

    /**
     * Call the superclass constructor to initialize this object.
     */
    public ScarabUserImpl()
    {
        super();
        
        /*
         * Functionality that would be useful in any implementation of
         * ScarabUser is available in AbstractScarabUser (ASU).  This 
         * implementation must extend from TurbineUser, so TurbineUser 
         * would need to extend ASU to gain the functionality through
         * inheritance.  This is possible with some modifications to 
         * fulcrum's build process.  But until changes to fulcrum allow it,
         * we will wrap a instance of ASU.
         */
        internalUser = new AbstractScarabUser()
        {
            public Integer getUserId()
            {
                return getPrivateUserId();
            }
            
            public String getEmail()
            {
                return getPrivateEmail();
            }
            
            public String getFirstName()
            {
                return getPrivateFirstName();
            }
            
            public String getLastName()
            {
                return getPrivateLastName();
            }

            protected List getRModuleUserAttributes(Criteria crit)
                throws TorqueException
            {
                return getPrivateRModuleUserAttributes(crit);
            }
            
            public boolean hasPermission(String perm, Module module)
            {
                return hasPrivatePermission(perm, module);
            }
            
            public List getModules() 
                throws TorqueException
            {
                return getModules(false);
            }
            
            public List getModules(boolean showDeletedModules) 
                throws TorqueException
            {
                List permList = ScarabSecurity.getAllPermissions();
                String[] perms = new String[permList.size()];
                perms = (String[])permList.toArray(perms);
                
                Module[] modules = getPrivateModules(perms, showDeletedModules);
                return (modules == null || modules.length == 0
                        ? Collections.EMPTY_LIST : Arrays.asList(modules));
            }

            /**
             * @see org.tigris.scarab.om.ScarabUser#getModules(String)
             */
            public Module[] getModules(String permission)
            {
                return getPrivateModules(permission);
            }

            protected void deleteRModuleUserAttribute(final RModuleUserAttribute rmua)
                throws TorqueException, TurbineSecurityException
            {
                privateDeleteRModuleUserAttribute(rmua);
            }    
        };
    }
    
    // the following getPrivateFoo methods are to avoid naming conflicts when
    // supplying implementations of the methods needed by AbstractScarabUser
    // when instantiated in the constructor
    private Integer getPrivateUserId()
    {
        return getUserId();
    }
    private String getPrivateEmail()
    {
        return getEmail();
    }
    private String getPrivateFirstName()
    {
        return getFirstName();
    }
    private String getPrivateLastName()
    {
        return getLastName();
    }
    public String getName()
    {
        return internalUser.getName();
    }
    private List getPrivateRModuleUserAttributes(Criteria crit)
        throws TorqueException
    {
        return getRModuleUserAttributes(crit);
    }
    private boolean hasPrivatePermission(String perm, Module module)
    {
        return hasPermission(perm, module);
    }
    private Module[] getPrivateModules(String permission)
    {        
        String[] perms = {permission};
        return getModules(perms);
    }
    private Module[] getPrivateModules(String[] permissions, boolean showDeletedModules)
    {        
        return getModules(permissions, showDeletedModules);
    }

    private void privateDeleteRModuleUserAttribute(final RModuleUserAttribute rmua)
        throws TorqueException, TurbineSecurityException
    {
        rmua.delete(this);
    }

    /**
     *   Utility method that takes a username and a confirmation code
     *   and will return true if there is a match and false if no match.
     *   <p>
     *   If there is an Exception, it will also return false.
     */
    public static boolean checkConfirmationCode (String username, 
                                                 String confirm)
    {
        // security check. :-)
        if (confirm.equalsIgnoreCase(User.CONFIRM_DATA))
        {
            return false;
        }
        
        try
        {
            Criteria criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), 
                          username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE),
                          confirm);
            criteria.setSingleRecord(true);
            List result = ScarabUserImplPeer.doSelect(criteria);
            if (result.size() > 0)
            {
                return true;
            }

            // FIXME: once i figure out how to build an OR in a Criteria i 
            // won't need this.
            // We check to see if the user is already confirmed because that
            // should result in a True as well.
            criteria = new Criteria();
            criteria.add (ScarabUserImplPeer.getColumnName(User.USERNAME), 
                          username);
            criteria.add (ScarabUserImplPeer.getColumnName(User.CONFIRM_VALUE),
                          User.CONFIRM_DATA);
            criteria.setSingleRecord(true);
            result = ScarabUserImplPeer.doSelect(criteria);
            return (result.size() > 0);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     This method will mark username as confirmed.
     returns true on success and false on any error
     */
    public static boolean confirmUser (String username)
    {
        try
        {
            User user = TurbineSecurity.getUser(username);
            user.setConfirmed(User.CONFIRM_DATA);
            TurbineSecurity.saveUser(user);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, Module)
     * Determine if a user has a permission, either within the specified
     * module or within the 'Global' module.
     */
    public boolean hasPermission(String perm, Module module)
    {
        boolean hasPermission = false;
        AccessControlList aclAnonymous = null;
        try
        {
            if (AnonymousUserUtil.anonymousAccessAllowed())
            {
                aclAnonymous = ((ScarabUser)AnonymousUserUtil.getAnonymousUser()).getACL();
            }
        }
        catch (Exception e)
        {
            getLog().error("hasPermission: " + e);
        }
        
        if (TORQUE_LOG.isDebugEnabled()) 
        {
            String name = (module == null) ? null : module.getName();
            TORQUE_LOG.debug("ScarabUserImpl.hasPermission(" + perm + ", " + 
                            name + ") started");
        }
    
        if (perm.equals(ScarabSecurity.USER__CHANGE_PASSWORD) && isUserAnonymous())
        {
            return false;
        }
        
        // Cache permission check results internally, so that we do not have
        // to ask the acl everytime.  FIXME!  This mechanism needs to be
        // modified to allow for invalidating the cached results.  Possible
        // candidates are the TurbineGlobalCacheService or JCS.  But keeping
        // this in place for the moment while investigating other sql, so
        // turbine's security sql does not dominate.
        String moduleKey = (module == null) ? null : module.getQueryKey();
        Object obj = getTemp("hasPermission" + perm + moduleKey);
        if (obj == null) 
        {        
        try
        {
            AccessControlList acl = this.getACL();
            if (acl != null) 
            {
                if (module != null)
                {
                    // first check for the permission in the specified module
                    hasPermission = acl.hasPermission(perm, (Group)module);
                    if (!hasPermission && aclAnonymous != null)
                    {
                        hasPermission |= aclAnonymous.hasPermission(perm, (Group)module);
                    }
                        
                }
                
                if (!hasPermission)
                {
                    // check for the permission within the 'Global' module
                    Module globalModule = ModuleManager
                        .getInstance(Module.ROOT_ID);
                    hasPermission = acl.hasPermission(perm, 
                                                      (Group)globalModule);
                    if (!hasPermission && aclAnonymous != null)
                    {
                        hasPermission |= aclAnonymous.hasPermission(perm, (Group)globalModule);
                    }
                }
            }
        }
        catch (Exception e)
        {
            hasPermission = false;
            getLog().error("Permission check failed on:" + perm, e);
        }
        
        Boolean b = hasPermission ? Boolean.TRUE : Boolean.FALSE;
        setTemp("hasPermission" + perm + moduleKey, b);
        }
        else 
        {
            hasPermission = ((Boolean)obj).booleanValue();
        }
        
        if (TORQUE_LOG.isDebugEnabled()) 
        {
            String name = (module == null) ? null : module.getName();
            TORQUE_LOG.debug("ScarabUserImpl.hasPermission(" + perm + ", " + 
                            name + ") end\n");
        }
        return hasPermission;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#hasPermission(String, List)
     */
    public boolean hasPermission(String perm, List modules)
    {
        return internalUser.hasPermission(perm, modules);
    }
        
    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules()
     */
    public List getModules() throws TorqueException
    {
        return internalUser.getModules();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(boolean)
     */
    public List getModules(boolean showDeletedModules)
        throws TorqueException
    {
        return internalUser.getModules(showDeletedModules);
    }


    public Module[] getModules(String permission) throws TorqueException
    {
        return internalUser.getModules(permission);
    }

    private static final String GET_MODULES = 
        "getModules";

    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(String[])
     */
    public Module[] getModules(String[] permissions)
    {
        return getModules(permissions, false);
    }
    
    /**
     * Get modules that user can copy an issue to.
     */
    public List getCopyToModules(Module currentModule) throws TorqueException
    {        
         return internalUser.getCopyToModules(currentModule);
    }
    public List getCopyToModules(Module currentModule, String action) throws TorqueException
    {        
         return internalUser.getCopyToModules(currentModule, action, null);
    }
    public List getCopyToModules(Module currentModule, String action, 
                                 String searchString)
        throws TorqueException
    {        
         return internalUser.getCopyToModules(currentModule, action, searchString);
    }

   
    /**
     * @see org.tigris.scarab.om.ScarabUser#getModules(String[], boolean)
     */
    public Module[] getModules(String[] permissions, boolean showDeletedModules)
    {        
        Module[] result = null;
        Object obj = ScarabCache.get(this, GET_MODULES, permissions); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.setDistinct();
            if (!showDeletedModules)
            {
                crit.add(ScarabModulePeer.DELETED, 0);
            }
            crit.addIn(TurbinePermissionPeer.PERMISSION_NAME, permissions);
            crit.addJoin(TurbinePermissionPeer.PERMISSION_ID, 
                     TurbineRolePermissionPeer.PERMISSION_ID);
            crit.addJoin(TurbineRolePermissionPeer.ROLE_ID, 
                         TurbineUserGroupRolePeer.ROLE_ID);
            crit.add(TurbineUserGroupRolePeer.USER_ID, getUserId());
            crit.addJoin(ScarabModulePeer.MODULE_ID, 
                         TurbineUserGroupRolePeer.GROUP_ID);
            
            try
            {
                List scarabModules = ScarabModulePeer.doSelect(crit);
                // check for permissions in global, if so get all modules
                for (int i=scarabModules.size()-1; i>=0; i--) 
                {
                    if (Module.ROOT_ID.equals(
                     ((Module)scarabModules.get(i)).getModuleId())) 
                    {
                        crit = new Criteria();
                        if (!showDeletedModules)
                        {
                            crit.add(ScarabModulePeer.DELETED, 0);
                        }
                        scarabModules = ScarabModulePeer.doSelect(crit);
                        break;
                    }
                }
                
                // Sort list of modules using a special comparator which
                // takes the name of the module and its parents in account
                Set sortedResult = new TreeSet(new Comparator() {
                  public int compare(Object object1, Object object2) {
                    Module m1 = (Module)object1;
                    Module m2 = (Module)object2;
                    return m1.getName().compareTo(m2.getName());
                  }
                });
                sortedResult.addAll(scarabModules);
                
                result = (Module[])sortedResult.toArray(new Module[sortedResult.size()]);
            }
            catch (Exception e)
            {
                getLog().error("An exception prevented retrieving any modules", e);
            }
            ScarabCache.put(result, this, GET_MODULES, permissions);
        }
        else 
        {
            result = (Module[])obj;
        }
        return result;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#hasAnyRoleIn(Module)
     */ 
    public boolean hasAnyRoleIn(Module module)
        throws TorqueException
    {
        return getRoles(module).size() != 0;
    }
    
    /**
     * eturn the list of currently active roles.
     * If no roles are active, reuturn an empty list.
     * @return
     */
    public List getCurrentRoleNames()
    {
        Module module = getCurrentModule();
        
        List roles=null;
        try
        {
            roles = getRoles(module);
        }
        catch (Exception e){}

        List result = new ArrayList();
        if(roles!=null)
        {
            int size = roles.size();
            if(size>0)
            {
                for(int index=0; index< size; index++)
                {
                    Role role = (Role)roles.get(index);
                    result.add (role.getName());
                }
            }
        }
        return result;
        
    }
    
    private static final String GET_ROLES = 
        "getRoles";

    /* *
     * @see org.tigris.scarab.om.ScarabUser#getRoles(Module)
     * !FIXME! need to define a Role interface (maybe the one in fulcrum is 
     * sufficient?) before making a method like this public.   
     * Right now it is only used in one place to determine
     * if the user has any roles available, so we will use a more specific
     * public method for that.
     */
    private List getRoles(Module module)
        throws TorqueException
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ROLES, module); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.setDistinct();
            crit.add(TurbineUserGroupRolePeer.USER_ID, getUserId());
            crit.add(TurbineUserGroupRolePeer.GROUP_ID, module.getModuleId());
            crit.addJoin(TurbineRolePeer.ROLE_ID, 
                     TurbineUserGroupRolePeer.ROLE_ID);
            result = TurbineRolePeer.doSelect(crit);
            
            ScarabCache.put(result, this, GET_ROLES, module);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#createNewUser()
     */
    public void createNewUser()
        throws TorqueException, DataBackendException, EntityExistsException
    {
        // get a unique id for validating the user
        final String uniqueId = RandomStringUtils
                .randomAlphanumeric(UNIQUE_ID_MAX_LEN);
        // add it to the perm table
        setConfirmed(uniqueId);
        TurbineSecurity.addUser (this, getPassword());
        setPasswordExpire();
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules()
     */
    public List getEditableModules() throws TorqueException
    {
        return internalUser.getEditableModules();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#getEditableModules(Module)
     */
    public List getEditableModules(Module currEditModule)
        throws TorqueException
    {
        return internalUser.getEditableModules(currEditModule);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttributes(Module, IssueType)
     */
    public List getRModuleUserAttributes(Module module,
                                         IssueType issueType)
        throws TorqueException
    {
        return internalUser.getRModuleUserAttributes(module, issueType);
    }
    
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getRModuleUserAttribute(Module, Attribute, IssueType)
     */
    public RModuleUserAttribute getRModuleUserAttribute(final Module module, 
                                                        final Attribute attribute,
                                                        final IssueType issueType)
        throws TorqueException, ScarabException
    {
        return internalUser
            .getRModuleUserAttribute(module, attribute, issueType);
    }
    
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getReportingIssue(String)
     */
    public Issue getReportingIssue(String key)
        throws TorqueException
    {
        return internalUser.getReportingIssue(key);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(Issue)
     */
    public String setReportingIssue(Issue issue)
        throws ScarabException
    {
        return internalUser.setReportingIssue(issue);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setReportingIssue(String, Issue)
     */
    public void setReportingIssue(String key, Issue issue)
    {
        internalUser.setReportingIssue(key, issue);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#getCurrentReport(String)
     */
    public ReportBridge getCurrentReport(String key)
        throws TorqueException
    {
        return internalUser.getCurrentReport(key);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(ReportBridge)
     */
    public String setCurrentReport(ReportBridge report)
        throws ScarabException
    {
        return internalUser.setCurrentReport(report);
    }
    
    /**
     * @see org.tigris.scarab.om.ScarabUser#setCurrentReport(String, ReportBridge)
     */
    public void setCurrentReport(String key, ReportBridge report)
    {
        internalUser.setCurrentReport(key, report);
    }
    
    /**
     * Sets the password to expire with information from the scarab.properties
     * scarab.login.password.expire value.
     *
     * @exception Exception if problem setting the password.
     */
    public void setPasswordExpire()
        throws TorqueException
    {
        String expireDays = Turbine.getConfiguration()
            .getString("scarab.login.password.expire", null);
        
        if (expireDays == null || expireDays.trim().length() == 0)
        {
            setPasswordExpire(null);
        }
        else
        {
            Calendar expireDate = Calendar.getInstance();
            expireDate.add(Calendar.DATE, Integer.parseInt(expireDays));
            setPasswordExpire(expireDate);
        }
    }
    
    /**
     * Sets the password to expire on the specified date.
     *
     * @param expire a <code>Calendar</code> value specifying the expire date.  If
     * this value is null, the password will be set to expire 10 years from the
     * current year. Since Logging in resets this value, it should be ok to 
     * have someone's password expire after 10 years.
     *
     * @exception Exception if problem updating the password.
     */
    public void setPasswordExpire(Calendar expire)
        throws TorqueException
    {
        Integer userid = getUserId();
        if (userid == null)
        {
            throw new TorqueException("Userid cannot be null"); //EXCEPTION
        }
        UserPreference up = UserPreferenceManager.getInstance(getUserId());
        if (expire == null)
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 10);
            up.setPasswordExpire(cal.getTime());
        }
        else
        {
            up.setPasswordExpire(expire.getTime());
        }
        up.save();
    }

    /**
     * Checks if the users password has expired.
     *
     * @exception Exception if problem querying for the password.
     */
    public boolean isPasswordExpired()
        throws TorqueException, ScarabException
    {
        // Password for anonymous never expires.
        if (isUserAnonymous())
        {
            return false;
        }
        
        final Integer userid = getUserId();
        if (userid == null)
        {
            return false; // throw new ScarabException (L10NKeySet.ExceptionGeneral,"Userid cannot be null"); //EXCEPTION
        }
        final Criteria crit = new Criteria();
        crit.add(UserPreferencePeer.USER_ID, userid);
        final Calendar cal = Calendar.getInstance();
        crit.add(UserPreferencePeer.PASSWORD_EXPIRE, 
                 cal.getTime() , Criteria.LESS_THAN);
        final List result = UserPreferencePeer.doSelect(crit);
        return result.size() == 1 ? true : false;
    }

    /**
     * Returns true if the user is the one set in scarab.anonymous.username, and
     * false otherwise.
     * @return
     */
    public boolean isUserAnonymous()
    {
        boolean brdo = false;
        String anonymous = Turbine.getConfiguration().getString("scarab.anonymous.username", null);
        String username = getUserName();
        if (anonymous != null && username.equals(anonymous))
        {
            brdo = true;
        }
        return brdo;
    }
    
    /**
     * Returns integer representing user preference for
     * Which screen to return to after entering an issue.
     * 1 = Enter New Issue. 2 = Assign Issue (default)
     * 3 = View Issue. 4 = Issue Types index.
     */
    public int getEnterIssueRedirect()
        throws TorqueException
    {
        return internalUser.getEnterIssueRedirect();
    }
    

    /**
     * Sets integer representing user preference for
     * Which screen to return to after entering an issue.
     * 1 = Enter New Issue. 2 = Assign Issue (default)
     * 3 = View Issue. 4 = Issue Types index.
     */
    public void setEnterIssueRedirect(int templateCode)
        throws TorqueException
    {
        internalUser.setEnterIssueRedirect(templateCode);
    }
                
    /**
     * @see ScarabUser#getHomePage()
     */
    public String getHomePage()
        throws TorqueException
    {
        return internalUser.getHomePage();
    }
    
    /**
     * @see ScarabUser#getHomePage(Module)
     */
    public String getHomePage(Module module)
        throws TorqueException
    {
        return internalUser.getHomePage(module);
    }

    /**
     * @see ScarabUser#setHomePage(String)
     */
    public void setHomePage(String homePage)
        throws TorqueException, ScarabException
    {
        internalUser.setHomePage(homePage);
    }

                
    /**
     * @see ScarabUser#getQueryTarget()
     */
    public String getQueryTarget()
    {
        return internalUser.getQueryTarget();
    }
    
    /**
     * @see ScarabUser#setSingleIssueTypeQueryTarget(IssueType, String)
     */
    public void setSingleIssueTypeQueryTarget(IssueType type, String target)
    {
        internalUser.setSingleIssueTypeQueryTarget(type, target);
    }

    /**
     * @see ScarabUser#getMITLists()
     */
    public List getMITLists()
        throws TorqueException
    {
        return internalUser.getMITLists();
    }

    /**
     * @see ScarabUser#hasAnySearchableRMITs().
     */
    public boolean hasAnySearchableRMITs()
        throws TorqueException, DataSetException    
    {
        return internalUser.hasAnySearchableRMITs();
    }
  
    /**
     * @see ScarabUser#getSearchableRMITs(String, String, String, String, Module)
     */
    public List getSearchableRMITs(String searchField, String searchString, 
                                   String sortColumn, String sortPolarity,
                                   Module skipModule)
        throws TorqueException    
    {
        return internalUser.getSearchableRMITs(searchField, searchString, 
            sortColumn, sortPolarity, skipModule);
    }

    /**
     * @see ScarabUser#getUnusedRModuleIssueTypes(Module).
     */
    public List getUnusedRModuleIssueTypes(Module module)
        throws TorqueException
    {
        return internalUser.getUnusedRModuleIssueTypes(module);
    }

    /**
     * @see ScarabUser#getAllRModuleIssueTypes(Module).
     */
    public List getAllRModuleIssueTypes(Module module)
        throws TorqueException
    {
        return internalUser.getAllRModuleIssueTypes(module);
    }

    /**
     * @see ScarabUser#addRMITsToCurrentMITList(List)
     */
    public void addRMITsToCurrentMITList(List rmits)
        throws TorqueException
    {
        internalUser.addRMITsToCurrentMITList(rmits);
    }

    /**
     * @see ScarabUser#getCurrentMITList()
     */
    public MITList getCurrentMITList()
    {
        MITList mitList = internalUser.getCurrentMITList();
        return mitList;
    }

    /**
     * @see ScarabUser#setCurrentMITList(MITList)
     */
    public void setCurrentMITList(MITList list)
    {
        internalUser.setCurrentMITList(list);
    }

    /**
     * @see ScarabUser#removeItemsFromCurrentMITList(String[])
     */
    public void removeItemsFromCurrentMITList(String[] ids)
    {
        internalUser.removeItemsFromCurrentMITList(ids);
    }

    /**
     * @see ScarabUser#lastEnteredIssueTypeOrTemplate()
     */
    public Object lastEnteredIssueTypeOrTemplate()
    {
        return internalUser.lastEnteredIssueTypeOrTemplate();
    }

    /**
     * @see ScarabUser#setLastEnteredIssueType(IssueType)
     */
    public void setLastEnteredIssueType(IssueType type)
    {
        internalUser.setLastEnteredIssueType(type);
    }

    /**
     * @see ScarabUser#setLastEnteredTemplate(Issue)
     */
    public void setLastEnteredTemplate(Issue template)
    {
        internalUser.setLastEnteredTemplate(template);
    }


    /**
     * @see org.tigris.scarab.om.ScarabUser#getMostRecentQuery()
     */
    public String getMostRecentQuery()
    {
        return internalUser.getMostRecentQuery();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setMostRecentQuery(String)
     */
    public void setMostRecentQuery(String queryString)
    {
        internalUser.setMostRecentQuery(queryString.toLowerCase());
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#hasMostRecentQuery()
     */
    public boolean hasMostRecentQuery()
    {
        return internalUser.hasMostRecentQuery();
    }

    public Map getAssociatedUsersMap()
        throws TorqueException
    {
        return internalUser.getAssociatedUsersMap();
    }

    public void setAssociatedUsersMap(Map associatedUsers)
        throws TorqueException
    {
        internalUser.setAssociatedUsersMap(associatedUsers);
    }

    public Map getSelectedUsersMap()
        throws TorqueException
    {
        return internalUser.getSelectedUsersMap();
    }

    public void setSelectedUsersMap(Map selectedUsers)
        throws TorqueException
    {
        internalUser.setSelectedUsersMap(selectedUsers);
    }
    
    
    /**
     * @see ScarabUser#getThreadKey()
     */
    public Object getThreadKey()
    {
        return internalUser.getThreadKey();
    }

    /**
     * @see ScarabUser#setThreadKey(Integer)
     */
    public void setThreadKey(Integer key)
    {  
        internalUser.setThreadKey(key);
    }


    /**
     * The current module
     */
    public Module getCurrentModule() 
    {
        return internalUser.getCurrentModule();
    }
    
    /**
     * The current module
     */
    public void setCurrentModule(Module  v) 
    {
        internalUser.setCurrentModule(v);
    }
     
    /**
     * The current issue type
     */
    public IssueType getCurrentIssueType()
        throws TorqueException
    {
        return internalUser.getCurrentIssueType();
    }
    
    /**
     * The current issue type
     */
    public void setCurrentIssueType(IssueType  v) 
    {
        internalUser.setCurrentIssueType(v);
    }    
    
    /**
     * @see ScarabUser#getCurrentRModuleIssueType()
     */
    public RModuleIssueType getCurrentRModuleIssueType()
        throws TorqueException
    {
        return internalUser.getCurrentRModuleIssueType();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#updateIssueListAttributes(List)
     */
    public void updateIssueListAttributes(List attributes)
        throws TorqueException, TurbineSecurityException
    {
        internalUser.updateIssueListAttributes(attributes);
    }

    public List getRoleNames(Module module)
       throws TorqueException
    {
       return null;
    }

    /**
     * Report on size of several maps
     */
    public String getStats()
    {
        return internalUser.getStats() 
            + "; TempStorage=" + getTempStorage().size() 
            + "; PermStorage=" + getPermStorage().size(); 
    }

    /**
     * Set the user's locale to a new value.
     */
    public void setLocale(Locale newLocale)
    {
        internalUser.setLocale(newLocale);
    }

    /**
     * Get the user's current locale. If it is not set, retrieve the
     * preferred Locale instead.
     */
    public Locale getLocale()
    {
        return internalUser.getLocale();
    }

    /**
     * Get the user's preferred locale.
     */
    public Locale getPreferredLocale()
    {
        return internalUser.getPreferredLocale();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#isShowOtherModulesInIssueTypeList()
     */
    public boolean isShowOtherModulesInIssueTypeList()
    {
        return internalUser.isShowOtherModulesInIssueTypeList();
    }

    /**
     * @see org.tigris.scarab.om.ScarabUser#setShowOtherModulesInIssueTypeList(boolean)
     */
    public void setShowOtherModulesInIssueTypeList(
        boolean newShowOtherModulesInIssueTypeList)
    {
        internalUser.setShowOtherModulesInIssueTypeList(
            newShowOtherModulesInIssueTypeList);
    }

    public List getAssignIssuesList()
    {
        List issues = null;
        try
        {
            Map userMap = this.getAssociatedUsersMap();
            if (userMap != null && userMap.size() > 0)
            {
                issues = new ArrayList();
                Iterator iter = userMap.keySet().iterator();
                while (iter.hasNext()) 
                {
                    issues.add(IssueManager.getInstance((Long)iter.next()));
                }
            }
        }
        catch (Exception te)
        {
            getLog().error("getAssignIssuesList(): " + te);
        }
        return issues;
    }

    /**
     * Return the AccessControlList for this user.
     * TODO: This should go wrapped in a fulcrum-independent interface, with Role and others.
     * 
     */
	public AccessControlList getACL()
	{
		AccessControlList acl = (AccessControlList)ScarabUserManager.getMethodResult().get(this.getUserName(), ScarabUserManager.GET_ACL);
		if (acl == null)
		{
			try
			{
				acl = TurbineSecurity.getACL(this);
				ScarabUserManager.getMethodResult().put(acl, this.getUserName(), ScarabUserManager.GET_ACL);
			} catch (Exception e) {
			}
		}
		return acl;
	}
    
    public boolean hasRoleInModule(Role role, Module module)
    {
    	boolean bRdo = false;
    	Boolean cached = (Boolean)ScarabUserManager.getMethodResult().get(this.getUserName(), ScarabUserManager.HAS_ROLE_IN_MODULE, role.getName(), module.getModuleId());
    	if (cached == null)
    	{
        	AccessControlList acl = this.getACL();
            GroupSet allGroups;
    		try
    		{
    			allGroups = TurbineSecurity.getAllGroups();
    	        Group group = allGroups.getGroup(module.getName());
    	        bRdo = acl.hasRole(role, group);
    	        ScarabUserManager.getMethodResult().put(Boolean.valueOf(bRdo), this.getUserName(), ScarabUserManager.HAS_ROLE_IN_MODULE, role.getName(), module.getModuleId());
    		}
    		catch (DataBackendException e)
    		{
    			Log.get().error("hasRoleInModule: " + e);
    		}
    	}
    	else
    	{
    		bRdo = cached.booleanValue();
    	}
		return bRdo;
    }
}
