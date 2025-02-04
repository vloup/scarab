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
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
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
 * individuals on behalf of CollabNet.
 */

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.tools.localization.L10NKeySet;

import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.QueryPeer;
import org.tigris.scarab.om.MITListItem;
import org.tigris.scarab.om.MITListItemPeer;
import org.tigris.scarab.workflow.WorkflowFactory;

/** 
 * This class represents a RModuleIssueType
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class RModuleIssueType 
    extends BaseRModuleIssueType
    implements Persistent, Conditioned
{

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule"); //EXCEPTION
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new."); //EXCEPTION
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        Integer id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects."); //EXCEPTION
        }
        setModuleId(id);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>Module</code> value
     */
    public Module getModule()
        throws TorqueException
    {
        Module module = null;
        Integer id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }


    /**
     * Checks if user has permission to delete module-issue type mapping.
     */
    public void delete(final ScarabUser user)
         throws TorqueException, ScarabException
    {                
        final Module module = getModule();
        final IssueType issueType = getIssueType();

        if (user.hasPermission(ScarabSecurity.MODULE__CONFIGURE, module))
        {
            // Delete both active and inactive attribute groups first
            final List attGroups = issueType.getAttributeGroups(module, false);
            for (int j=0; j<attGroups.size(); j++)
            {
                // delete attribute-attribute group map
                final AttributeGroup attGroup = 
                              (AttributeGroup)attGroups.get(j);
                attGroup.delete();
            }

            // Delete mappings with user attributes
            List rmas = module.getRModuleAttributes(issueType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete();
            }
            // Delete mappings with user attributes for template type
            final IssueType templateType = issueType.getTemplateIssueType();
            rmas = module.getRModuleAttributes(templateType);
            for (int i=0; i<rmas.size(); i++)
            {
                ((RModuleAttribute)rmas.get(i)).delete();
            }
 
            // delete workflows
            WorkflowFactory.getInstance().resetAllWorkflowsForIssueType(module, 
                                                                        issueType);
            // delete templates
            Criteria c = new Criteria()
                .add(IssuePeer.TYPE_ID, templateType.getIssueTypeId());
            final List result = IssuePeer.doSelect(c);
            final List issueIdList = new ArrayList(result.size());
            for (int i=0; i < result.size(); i++) 
            {
                final Issue issue = (Issue)result.get(i);
                issueIdList.add(issue.getIssueId());
            }
            IssuePeer.doDelete(c);
            if (!issueIdList.isEmpty())
            {
                c = new Criteria()
                    .addIn(IssueTemplateInfoPeer.ISSUE_ID, issueIdList);
                IssueTemplateInfoPeer.doDelete(c);
            }
            
            // mit list items
            c = new Criteria()
                .add(MITListItemPeer.MODULE_ID, module.getModuleId())
                .add(MITListItemPeer.ISSUE_TYPE_ID, issueType.getIssueTypeId());
            final List listItems = MITListItemPeer.doSelect(c);
            final List listIds = new ArrayList(listItems.size());

            for (int i=0; i < listItems.size(); i++) 
            {
                final MITList list = ((MITListItem)listItems.get(i)).getMITList();
                final Long listId = list.getListId();
                if (list.isSingleModuleIssueType() && !listIds.contains(listId))
                {
                    listIds.add(listId);
                }
            }
            MITListItemPeer.doDelete(c);

            if (!listIds.isEmpty())
            {
                // delete single module-issuetype mit lists
                c = new Criteria()
                    .addIn(MITListPeer.LIST_ID, listIds);
                MITListPeer.doDelete(c);

                // delete queries
                c = new Criteria()
                    .addIn(QueryPeer.LIST_ID, listIds);
                QueryPeer.doDelete(c);
            }

            c = new Criteria()
                .add(RModuleIssueTypePeer.MODULE_ID, getModuleId())
                .add(RModuleIssueTypePeer.ISSUE_TYPE_ID, getIssueTypeId());
            RModuleIssueTypePeer.doDelete(c);
            RModuleIssueTypeManager.removeFromCache(this);
            final List rmits = module.getRModuleIssueTypes();
            rmits.remove(this);
        }
        else
        {
            throw new ScarabException(L10NKeySet.YouDoNotHavePermissionToAction);
        }
    }

    /**
     * Not really sure why getDisplayText was created because 
     * it really should just be getDisplayName() (JSS)
     *
     * @see #getDisplayText()
     */
    public String getDisplayName()
    {
        String display = super.getDisplayName();
        if (display == null)
        {
            try
            {
                display = getIssueType().getName();
            }
            catch (TorqueException e)
            {
                getLog().error("Error getting the issue type name: ", e);
            }
        }
        return display;
    }

    /**
     * Gets name to display. First tries to get the DisplayName 
     * for the RMIT, if that is null, then it will get the IssueType's
     * name and use that.
     *
     * @deprecated use getDisplayName() instead
     */
    public String getDisplayText()
    {
        return this.getDisplayName();
    }

    public String getDisplayDescription()
    {
        String display = super.getDisplayDescription();
        if (display == null)
        {
            try
            {
                display = getIssueType().getDescription();
            }
            catch (TorqueException e)
            {
                getLog().error("Error getting the issue type description: ", e);
            }
        }
        return display;
    }

    /**
     * Copies object.
     */
    public RModuleIssueType copy()
         throws TorqueException
    {
        RModuleIssueType rmit2 = new RModuleIssueType();
        rmit2.setModuleId(getModuleId());
        rmit2.setIssueTypeId(getIssueTypeId());
        rmit2.setActive(getActive());
        rmit2.setDisplay(getDisplay());
        rmit2.setDisplayDescription(getDisplayDescription());
        rmit2.setOrder(getOrder());
        rmit2.setDedupe(getDedupe());
        rmit2.setHistory(getHistory());
        rmit2.setComments(getComments());
        return rmit2;
    }

    /**
     * Forces the relationship to only retrieve IssueType level conditions (if ATTRIBUTE_ID is not
     * asked to be NULL, this method could return conditions already defined for
     * SCARAB_R_MODULE_ATTRIBUTE's records)
     * 
     */
    public List getConditions(Criteria criteria) throws TorqueException
    {
    	criteria.add(ConditionPeer.ATTRIBUTE_ID, (Object)(ConditionPeer.ATTRIBUTE_ID + " IS NULL"), Criteria.CUSTOM);
    	return super.getConditions(criteria);
    }
    
    /* (non-Javadoc)
     * @see org.tigris.scarab.om.Conditioned#getConditionsArray()
     */
    public Integer[] getConditionsArray()
    {
        List conditions = new ArrayList();
        Integer[] aIDs = null;
        try
        {
            
            conditions = this.getConditions();
            aIDs = new Integer[conditions.size()];
            int i=0;
            for (Iterator iter = conditions.iterator(); iter.hasNext(); i++)
            {
                aIDs[i] = (Integer)iter.next();
            }
        }
        catch (TorqueException e)
        {
            this.getLog().error("getConditionsArray: " + e);
        }
        return aIDs;
    }

    /* (non-Javadoc)
     * @see org.tigris.scarab.om.Conditioned#setConditionsArray(java.lang.Integer[])
     */
    public void setConditionsArray(Integer[] aOptionId) throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(ConditionPeer.ATTRIBUTE_ID, null);
        crit.add(ConditionPeer.MODULE_ID, this.getModuleId());
        crit.add(ConditionPeer.ISSUE_TYPE_ID, this.getIssueTypeId());
        crit.add(ConditionPeer.TRANSITION_ID, null);
        ConditionPeer.doDelete(crit);
        this.save();
        this.getConditions().clear();
        ConditionManager.clear();
        if (aOptionId != null)
            for (int i = 0; i < aOptionId.length; i++)
            {
                if (aOptionId[i].intValue() != 0)
                {
                    Condition cond = new Condition();
                    cond.setAttributeId(null);
                    cond.setOptionId(aOptionId[i]);
                    cond.setModuleId(this.getModuleId());
                    cond.setIssueTypeId(this.getIssueTypeId());
                    cond.setTransitionId(null);
                    this.addCondition(cond);
                    cond.save();
                }
            }
    }

    /* (non-Javadoc)
     * @see org.tigris.scarab.om.Conditioned#isRequiredIf(java.lang.Integer)
     */
    public boolean isRequiredIf(Integer optionID) throws TorqueException
    {
        Condition cond = new Condition();
        cond.setAttributeId(null);
        cond.setOptionId(optionID);
        cond.setModuleId(this.getModuleId());
        cond.setIssueTypeId(this.getIssueTypeId());
        cond.setTransitionId(null);
        return this.getConditions().contains(cond);

    }

    /* (non-Javadoc)
     * @see org.tigris.scarab.om.Conditioned#isConditioned()
     */
    public boolean isConditioned()
    {
        boolean bRdo = false;
        try {
        	bRdo = this.getConditions().size()>0;
        } catch (TorqueException te)
        {
            // Nothing to do
        }
        return bRdo;
    }
    
    /**
     * Method called to associate a Condition object to this object
     * through the Condition foreign key attribute
     *
     * @param l Condition
     * @throws TorqueException
     */
    public void addCondition(Condition cond) throws TorqueException
    {
        getConditions().add(cond);
        cond.setRModuleIssueType(this);
    }    
}
