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

import com.workingdogs.village.DataSetException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import org.apache.torque.TorqueException;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.manager.MethodResultCache;
import org.apache.fulcrum.localization.Localization;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.workflow.Workflow;
import org.tigris.scarab.workflow.WorkflowFactory;

/** 
 * This class represents an IssueType.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public  class IssueType 
    extends BaseIssueType
    implements Persistent
{
    private static final String ISSUE_TYPE = 
        "IssueType";
    private static final String GET_TEMPLATE_ISSUE_TYPE = 
        "getTemplateIssueType";
    private static final String GET_INSTANCE = 
        "getInstance";
    protected static final String GET_ATTRIBUTE_GROUPS = 
        "getAttributeGroups";
    protected static final String GET_ATTRIBUTE_GROUP =
        "getAttributeGroup";
    protected static final String GET_R_ISSUETYPE_ATTRIBUTES = 
        "getRIssueTypeAttributes";
    protected static final String GET_R_ISSUETYPE_OPTIONS = 
        "getRIssueTypeOptions";
    protected static final String GET_ALL_R_ISSUETYPE_OPTIONS = 
        "getAllRIssueTypeOptions";
    protected static final String GET_DEFAULT_TEXT_ATTRIBUTE = 
        "getDefaultTextAttribute";
    protected static final String GET_QUICK_SEARCH_ATTRIBUTES = 
        "getQuickSearchAttributes";
    protected static final String GET_REQUIRED_ATTRIBUTES = 
        "getRequiredAttributes";
    protected static final String GET_ACTIVE_ATTRIBUTES = 
        "getActiveAttributes";

    static final String USER = "user";
    static final String NON_USER = "non-user";

    private static final Properties SYSTEM_CONFIG = new Properties();

    //loads the properties file that specifies system defined issue types
    static
    {
        InputStream in = IssueType.class
                            .getResourceAsStream("IssueTypeConfig.properties");
        if (in != null)
        {
            try
            {
                SYSTEM_CONFIG.load(in);
            }
            catch(IOException ioe)
            {
                Log.get().warn("Exception while loading the file: IssueTypeConfig.properties", ioe);
            }
        }
    }


    // this will not change, so only look it up once.
    private IssueType templateIssueType;

    // this will not change, so only look it up once.
    private IssueType parentIssueType;

    /**
     * Gets the IssueType template for this IssueType. The template
     * is a special type of IssueType.
     */
    public IssueType getTemplateIssueType()
        throws TorqueException, ScarabException
    {
        if (templateIssueType == null) 
        {        
            final Criteria crit = new Criteria();
            crit.add(IssueTypePeer.PARENT_ID, getIssueTypeId());
            final List results = IssueTypePeer.doSelect(crit);
            if (results.isEmpty())
            {
                throw new ScarabException(L10NKeySet.ExceptionTemplateTypeForIssueType);
            }
            else
            {
                templateIssueType = (IssueType)results.get(0);
            }
        }
        return templateIssueType;
    }

    /**
     * Gets the parent IssueType for this template IssueType. The template
     * is a special type of IssueType.
     */
    public IssueType getIssueTypeForTemplateType()
        throws TorqueException
    {
        if (parentIssueType == null) 
        {        
            parentIssueType = getIssueTypeRelatedByParentId();
        }
        return parentIssueType;
    }


    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public Integer getTemplateId()
        throws TorqueException, ScarabException
    {
        return getTemplateIssueType().getIssueTypeId();
    }        

    /**
     *  Returns true if the issue type has issues associated with it.
     */
    public boolean hasIssues()
        throws TorqueException, DataSetException
    {
        return hasIssues((Module) null);
    }        

   /**
    * If module name is identical to global name, return the global 
    * name. Otherwise return the module name followed by a space 
    * and the global name in parentheses.
    * @return a <code>String</code> representation of the display name.
    */
    public String getDisplayName(Module module)
        throws TorqueException
    {
        String moduleName = module.getRModuleIssueType(this).getDisplayName();
        String displayName = getName();
        if (!moduleName.equals(displayName))
        {
            displayName = moduleName +" (" + displayName + ")";
        }
        return displayName;
    }

    /**
     *  Returns true if the issue type/module has issues associated with it.
     */
    public boolean hasIssues(Module module)
        throws TorqueException, DataSetException
    {
        Criteria crit = new Criteria();
        crit.add(IssuePeer.TYPE_ID, getIssueTypeId());
        if (module != null)
        {
            crit.add(IssuePeer.MODULE_ID, module.getModuleId());
        }
        return (IssuePeer.count(crit) > 0);
    }        

    /**
     * Get the IssueType using a issue type name
     */
    public static IssueType getInstance(final String issueTypeName)
        throws TorqueException,ScarabException
    {
        IssueType result = null;
        Object obj = ScarabCache.get(ISSUE_TYPE, GET_INSTANCE, issueTypeName); 
        if (obj == null) 
        {        
            final Criteria crit = new Criteria();
            crit.add(IssueTypePeer.NAME, issueTypeName);
            final List issueTypes = IssueTypePeer.doSelect(crit);
            if(issueTypes == null || issueTypes.size() == 0)
            {
                throw new ScarabException(L10NKeySet.ExceptionInvalidIssueType,
                                          issueTypeName);
            }
            result = (IssueType)issueTypes.get(0);
            ScarabCache.put(result, ISSUE_TYPE, GET_INSTANCE, issueTypeName);
        }
        else 
        {
            result = (IssueType)obj;
        }
        return result;
    }

    /**
     * Copy the IssueType and its corresponding template type 
     */
    public IssueType copyIssueType()
        throws TorqueException, ScarabException
    {
        final IssueType newIssueType = new IssueType();
        newIssueType.setName(getName() + " (copy)");
        newIssueType.setDescription(getDescription());
        newIssueType.setParentId(ScarabConstants.INTEGER_0);
        newIssueType.save();
        final Integer newId = newIssueType.getIssueTypeId();

        // Copy template type
        final IssueType template = IssueTypePeer
              .retrieveByPK(getTemplateId());
        final IssueType newTemplate = new IssueType();
        newTemplate.setName(template.getName());
        newTemplate.setParentId(newId);
        newTemplate.save();

        // Copy user attributes
        final List userRIAs = getRIssueTypeAttributes(false, USER);
        for (int m=0; m<userRIAs.size(); m++)
        {
            final RIssueTypeAttribute userRia = (RIssueTypeAttribute)userRIAs.get(m);
            final RIssueTypeAttribute newUserRia = userRia.copyRia();
            newUserRia.setIssueTypeId(newId);
            newUserRia.save();
        }

        // Copy attribute groups
        final List attrGroups = getAttributeGroups(false);
        for (int i = 0; i<attrGroups.size(); i++)
        {
            final AttributeGroup group = (AttributeGroup)attrGroups.get(i);
            final AttributeGroup newGroup = group.copyGroup();
            newGroup.setIssueTypeId(newId);
            newGroup.save();

            // add attributes
            final List attrs = group.getAttributes();
            if (attrs != null)
            {
                for (int j=0; j<attrs.size(); j++)
                {
                    // save attribute-attribute group maps
                    final Attribute attr = (Attribute)attrs.get(j);
                    final RAttributeAttributeGroup raag = group.getRAttributeAttributeGroup(attr);
                    final RAttributeAttributeGroup newRaag = new RAttributeAttributeGroup();
                    newRaag.setAttributeId(raag.getAttributeId());
                    newRaag.setOrder(raag.getOrder());
                    newRaag.setGroupId(newGroup.getAttributeGroupId());
                    newRaag.save();

                    // save attribute-issueType maps
                    final RIssueTypeAttribute ria = getRIssueTypeAttribute(attr);
                    final RIssueTypeAttribute newRia = ria.copyRia();
                    newRia.setIssueTypeId(newId);
                    newRia.save();

                    // save options
                    final List rios = getRIssueTypeOptions(attr, false);
                    if (rios != null)
                    {
                        for (int k=0; k<rios.size(); k++)
                        {
                            final RIssueTypeOption rio = (RIssueTypeOption)rios.get(k);
                            final RIssueTypeOption newRio = rio.copyRio();
                            newRio.setIssueTypeId(newId);
                            newRio.save();
                        }
                    }
                }
            }
        }

        // add workflow 
        WorkflowFactory.getInstance().copyIssueTypeWorkflows(this, newIssueType);

        return newIssueType;
    }

    /**
     * Delete mappings with all modules
     */
    public void deleteModuleMappings(final ScarabUser user)
        throws TorqueException, ScarabException
    {
        final Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                 getIssueTypeId());
        final List rmits = RModuleIssueTypePeer.doSelect(crit);
        for (int i=0; i<rmits.size(); i++)
        {
            final RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
            rmit.delete(user);
        }
        ScarabCache.clear();
    }


    /**
     * Create default groups upon issue type creation.
     */
    public void createDefaultGroups()
        throws TorqueException
    {
        AttributeGroup ag = createNewGroup();
        ag.setOrder(1);
        ag.setDedupe(true);
        ag.setDescription(null);
        ag.save();
        AttributeGroup ag2 = createNewGroup();
        ag2.setOrder(3);
        ag2.setDedupe(false);
        ag2.setDescription(null);
        ag2.save();
    }

    public List getAttributeGroups(Module module)
        throws TorqueException
    {
        return getAttributeGroups(module, false);
    }

    public List getAttributeGroups(boolean activeOnly)
        throws TorqueException
    {
        return getAttributeGroups(null, activeOnly);
    }

    /**
     * List of attribute groups associated with this module).
     */
    public List getAttributeGroups(Module module, boolean activeOnly)
        throws TorqueException
    {
        List groups = null;
        Boolean activeBool = activeOnly ? Boolean.TRUE : Boolean.FALSE;
        Object obj = getMethodResult().get(this, GET_ATTRIBUTE_GROUPS,
                                           module, activeBool);
        if (obj == null)
        {
            Criteria crit = new Criteria()
                .add(AttributeGroupPeer.ISSUE_TYPE_ID, getIssueTypeId())
                .addAscendingOrderByColumn(AttributeGroupPeer.PREFERRED_ORDER);
            if (activeOnly)
            {
                crit.add(AttributeGroupPeer.ACTIVE, true);
            }
            if (module != null)
            {
                crit.add(AttributeGroupPeer.MODULE_ID, module.getModuleId());
            }
            else
            {
                // TODO Change this to be crit.add(AttributeGroupPeer.MODULE_ID, Criteria.ISNULL) when torque is fixed
                crit.add(AttributeGroupPeer.MODULE_ID,
                         (Object)(AttributeGroupPeer.MODULE_ID + " IS NULL"),
                         Criteria.CUSTOM);
            }
            groups = AttributeGroupPeer.doSelect(crit);
            getMethodResult().put(groups, this, GET_ATTRIBUTE_GROUPS,
                                  module, activeBool);
        }
        else
        {
            groups = (List)obj;
        }
        return groups;
    }


    public AttributeGroup createNewGroup()
        throws TorqueException
    {
        return createNewGroup(null);
    }

    /**
     * Creates new attribute group.
     */
    public AttributeGroup createNewGroup(Module module)
        throws TorqueException
    {
        List groups = getAttributeGroups(module, false);
        AttributeGroup ag = new AttributeGroup();

        // Make default group name 'new attribute group' 
        ag.setName(Localization.getString("ScarabBundle",
                ScarabConstants.DEFAULT_LOCALE, "NewAttributeGroup"));
        ag.setActive(true);
        ag.setIssueTypeId(getIssueTypeId());
        if (module != null)
        {
            ag.setModuleId(module.getModuleId());
        }
        if (groups.size() == 0)
        {
            ag.setDedupe(true);
            ag.setOrder(groups.size() +1);
        }
        else 
        {
            ag.setDedupe(false);
            ag.setOrder(groups.size() +2);
        }
        ag.save();
        groups.add(ag);
        return ag;
    }

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     *
     * @see #getDedupeSequence(Module)
     */
    public int getDedupeSequence()
        throws TorqueException
    {
        return getDedupeSequence(null);
    }

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     *
     * @param module A specific Module to retrieve AttributeGroup
     * associations for, or <code>null</code> for groups associated
     * with the global issue type.
     */
    int getDedupeSequence(Module module)
        throws TorqueException
    {
        List groups = getAttributeGroups(module, false);
        int sequence = groups.size() + 1;
        for (int i = 1; i <= groups.size(); i++)
        {
            int order;
            int previousOrder;
            try
            {
                order = ((AttributeGroup) groups.get(i)).getOrder();
                previousOrder = ((AttributeGroup) groups.get(i - 1)).getOrder();
            }
            catch (Exception e)
            {
                Log.get().warn("Error accessing dedupe sequence for issue "
                               + "type '" + this + '\'', e);
                return sequence;
            }

            if (order != previousOrder + 1)
            {
                sequence = order - 1;
                break;
            }
        }
        return sequence;
    }    

    /**
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes()
    {
        List rias = null;
        try
        {
            rias = getRIssueTypeAttributes(false);
        }
        catch (Exception e)
        {
            Log.get().warn("Could not get RIA records for " + getName(), e);
        }
        return rias;
    }

    /**
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes(boolean activeOnly)
        throws TorqueException
    {
        return getRIssueTypeAttributes(activeOnly, "all");
    }


    /**
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes(boolean activeOnly,
                                        String attributeType)
        throws TorqueException
    {
        List rias = null;
        Boolean activeBool = (activeOnly ? Boolean.TRUE : Boolean.FALSE);
        Object obj = getMethodResult().get(this, GET_R_ISSUETYPE_ATTRIBUTES, 
                                           activeBool, attributeType); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria();
            crit.add(RIssueTypeAttributePeer.ISSUE_TYPE_ID, 
                     getIssueTypeId());
            crit.addAscendingOrderByColumn(
                RIssueTypeAttributePeer.PREFERRED_ORDER);
            
            if (activeOnly)
            {
                crit.add(RIssueTypeAttributePeer.ACTIVE, true);
            }
            
            if (USER.equals(attributeType))
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY);
                crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                         RIssueTypeAttributePeer.ATTRIBUTE_ID); 
            }
            else if (NON_USER.equals(attributeType))
            {
                crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                         RIssueTypeAttributePeer.ATTRIBUTE_ID); 
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY,
                         Criteria.NOT_EQUAL);
            }
            
            rias = RIssueTypeAttributePeer.doSelect(crit); 
            getMethodResult().put(rias, this, GET_R_ISSUETYPE_ATTRIBUTES, 
                                  activeBool, attributeType);
        }
        else 
        {
            rias = (List)obj;
        }
        return rias;
    }

    /**
     * Gets associated activeattributes.
     */
    public List getAttributes(String attributeType)
        throws TorqueException
    {
        ArrayList attrs = new ArrayList();
        List rias = getRIssueTypeAttributes(true, attributeType);
        for (int i=0; i<rias.size(); i++)
        {
            attrs.add(((RIssueTypeAttribute)rias.get(i)).getAttribute());
        }
        return attrs;
    }
         
    /**
     * Adds issuetype-attribute mapping to issue type.
     */
    public RIssueTypeAttribute addRIssueTypeAttribute(Attribute attribute)
        throws TorqueException
    {
        String attributeType = null;
        attributeType = (attribute.isUserAttribute() ? USER : NON_USER);

        RIssueTypeAttribute ria = new RIssueTypeAttribute();
        ria.setIssueTypeId(getIssueTypeId());
        ria.setAttributeId(attribute.getAttributeId());
        ria.setOrder(getLastAttribute(attributeType) + 1);
        ria.save();
        getRIssueTypeAttributes(false, attributeType).add(ria);
        return ria;
    }

    public RIssueTypeAttribute getRIssueTypeAttribute(Attribute attribute)
        throws TorqueException
    {
        RIssueTypeAttribute ria = null;
        List rias = null;
        if (attribute.isUserAttribute())
        {
            rias = getRIssueTypeAttributes(false, USER);
        }
        else
        {
            rias = getRIssueTypeAttributes(false, NON_USER);
        }
        Iterator i = rias.iterator();
        while (i.hasNext())
        {
            RIssueTypeAttribute tempRia = (RIssueTypeAttribute)i.next();
            if (tempRia.getAttribute().equals(attribute))
            {
                ria = tempRia;
                break;
            }
        }
        return ria;
    }

    /**
     * gets a list of all of the User Attributes in an issue type.
     */
    public List getUserAttributes()
        throws TorqueException
    {
        return getUserAttributes(true);
    }

    /**
     * gets a list of all of the User Attributes in an issue type.
     */
    public List getUserAttributes(boolean activeOnly)
        throws TorqueException
    {
        List rIssueTypeAttributes = getRIssueTypeAttributes(activeOnly, USER);
        List userAttributes = new ArrayList();

        for (int i=0; i<rIssueTypeAttributes.size(); i++)
        {
            Attribute att = ((RIssueTypeAttribute)rIssueTypeAttributes.get(i)).getAttribute();
            userAttributes.add(att);
        }
        return userAttributes;
    }

    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for issueType-attribute map
     * so that a new RIssueTypeAttribute can be added at the end.
     */
    public int getLastAttribute(String attributeType)
        throws TorqueException
    {
        List itAttributes = getRIssueTypeAttributes(false, attributeType);
        int last = 0;

        for (int i=0; i<itAttributes.size(); i++)
        {
               int order = ((RIssueTypeAttribute) itAttributes.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }


    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for module-attribute map
     * so that a new RIssueTypeOption can be added at the end.
     */
    public int getLastAttributeOption(Attribute attribute)
        throws TorqueException
    {
        List issueTypeOptions = getRIssueTypeOptions(attribute);
        int last = 0;

        for (int i=0; i<issueTypeOptions.size(); i++)
        {
               int order = ((RIssueTypeOption) issueTypeOptions.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }

    /**
     * Adds issuetype-attribute-option mapping to module.
     */
    public RIssueTypeOption addRIssueTypeOption(AttributeOption option)
        throws TorqueException
    {
        RIssueTypeOption rio = new RIssueTypeOption();
        rio.setIssueTypeId(getIssueTypeId());
        rio.setOptionId(option.getOptionId());
        rio.setOrder(getLastAttributeOption(option.getAttribute()) + 1);
        rio.save();
        getRIssueTypeOptions(option.getAttribute(), false).add(rio);
        return rio;
    }

    /**
     * Gets associated attribute options.
     */
    public List getRIssueTypeOptions(Attribute attribute)
        throws TorqueException
    {
        return getRIssueTypeOptions(attribute, true);
    }

    /**
     * Gets associated attribute options.
     */
    public List getRIssueTypeOptions(Attribute attribute, boolean activeOnly)
        throws TorqueException
    {
        List allRIssueTypeOptions = null;
        allRIssueTypeOptions = getAllRIssueTypeOptions(attribute);

        if (allRIssueTypeOptions != null)
        {
            if (activeOnly)
            {
                List activeRIssueTypeOptions =
                    new ArrayList(allRIssueTypeOptions.size());
                for (int i=0; i<allRIssueTypeOptions.size(); i++)
                {
                    RIssueTypeOption rio =
                        (RIssueTypeOption)allRIssueTypeOptions.get(i);
                    if (rio.getActive())
                    {
                        activeRIssueTypeOptions.add(rio);
                    }
                }
                allRIssueTypeOptions =  activeRIssueTypeOptions;
            }
        }
        return allRIssueTypeOptions;
    }
    

    private List getAllRIssueTypeOptions(Attribute attribute)
        throws TorqueException
    {
        List rIssueTypeOpts;
        Object obj = ScarabCache.get(this, GET_ALL_R_ISSUETYPE_OPTIONS, 
                                     attribute); 
        if (obj == null) 
        {
            List options = attribute.getAttributeOptions(false);
            Integer[] optIds = null;
            if (options == null)
            {
                optIds = new Integer[0];
            }
            else
            {
                optIds = new Integer[options.size()];
            }
            for (int i=optIds.length-1; i>=0; i--)
            {
                optIds[i] = ((AttributeOption)options.get(i)).getOptionId();
            }
            
            if (optIds.length > 0)
            { 
                Criteria crit = new Criteria();
                crit.add(RIssueTypeOptionPeer.ISSUE_TYPE_ID, getIssueTypeId());
                crit.addIn(RIssueTypeOptionPeer.OPTION_ID, optIds);
                crit.addJoin(RIssueTypeOptionPeer.OPTION_ID, AttributeOptionPeer.OPTION_ID);
                crit.addAscendingOrderByColumn(RIssueTypeOptionPeer.PREFERRED_ORDER);
                crit.addAscendingOrderByColumn(AttributeOptionPeer.OPTION_NAME);
                rIssueTypeOpts = RIssueTypeOptionPeer.doSelect(crit);
            }
            else
            {
                rIssueTypeOpts = new ArrayList(0);
            }
            ScarabCache.put(rIssueTypeOpts, this, GET_ALL_R_ISSUETYPE_OPTIONS, 
                            attribute);
        }
        else 
        {
            rIssueTypeOpts = (List)obj;
        }
        return rIssueTypeOpts;
    }

    public RIssueTypeOption getRIssueTypeOption(AttributeOption option)
        throws TorqueException
    {
        RIssueTypeOption rio = null;
        List rios = getRIssueTypeOptions(option.getAttribute(), false);
        Iterator i = rios.iterator();
        while (i.hasNext())
        {
            rio = (RIssueTypeOption)i.next();
            if (rio.getAttributeOption().equals(option))
            {
                break;
            }
        }

        return rio;
    }

    /**
     * Gets a list of all of the global Attributes that are not 
     * Associated with this issue type
     */
    public List getAvailableAttributes(String attributeType)
        throws TorqueException
    {
        List allAttributes = AttributePeer.getAttributes(attributeType);
        List availAttributes = new ArrayList();
        List rIssueTypeAttributes = getRIssueTypeAttributes(false,
                                                            attributeType);
        List attrs = new ArrayList();
        for (int i=0; i<rIssueTypeAttributes.size(); i++)
        {
            attrs.add(
               ((RIssueTypeAttribute) rIssueTypeAttributes.get(i)).getAttribute());
        }
        for (int i=0; i<allAttributes.size(); i++)
        {
            Attribute att = (Attribute)allAttributes.get(i);
            if (!attrs.contains(att))
            {
                availAttributes.add(att);
            }
        }
        return availAttributes;
    }


    /**
     * Gets a list of all of the global attributes options
     *  that are not associated with this issue type
     */
    public List getAvailableAttributeOptions(Attribute attribute)
        throws TorqueException
    {
        List rIssueTypeOptions = getRIssueTypeOptions(attribute, false);
        List issueTypeOptions = new ArrayList();
        if (rIssueTypeOptions != null)
        {
            for (int i=0; i<rIssueTypeOptions.size(); i++)
            {
                issueTypeOptions.add(
                   ((RIssueTypeOption) rIssueTypeOptions.get(i)).getAttributeOption());
            }
        }

        List allOptions = attribute.getAttributeOptions(false);
        List availOptions = new ArrayList();

        for (int i=0; i<allOptions.size(); i++)
        {
            AttributeOption option = (AttributeOption)allOptions.get(i);
            if (!issueTypeOptions.contains(option))
            {
                availOptions.add(option);
            }
        }
        return availOptions;
    }

    private MethodResultCache getMethodResult()
    {
        return IssueTypeManager.getMethodResult();
    }

    public String toString()
    {
        return '{' + super.toString() + ": name=" + getName() + '}';
    }

    /**
     * Gets a list of non-user AttributeValues which match a given Module.
     * It is used in the MoveIssue2.vm template
     */
    public List getMatchingAttributeValuesList(Module oldModule, Module newModule, 
                                               IssueType newIssueType)
          throws TorqueException
    {
        List matchingAttributes = new ArrayList();
        List srcActiveAttrs = getActiveAttributes(oldModule);
        List destActiveAttrs = newIssueType.getActiveAttributes(newModule);
        for (int i = 0; i<srcActiveAttrs.size(); i++)
        {
            Attribute attr = (Attribute)srcActiveAttrs.get(i);
                
            if (destActiveAttrs.contains(attr))
            {
                matchingAttributes.add(attr);
            } 
        }
        return matchingAttributes;
    }

    /**
     * Gets a list of Attributes which do not match a given Module.
     * It is used in the MoveIssue2.vm template
     */
    public List getOrphanAttributeValuesList(Module oldModule, Module newModule, 
                                             IssueType newIssueType)
          throws TorqueException
    {
        List orphanAttributes = new ArrayList();
        List srcActiveAttrs = getActiveAttributes(oldModule);
        List destActiveAttrs = newIssueType.getActiveAttributes(newModule);
        for (int i = 0; i<srcActiveAttrs.size(); i++)
        {
            Attribute attr = (Attribute)srcActiveAttrs.get(i);
            if (!destActiveAttrs.contains(attr))
            {
                orphanAttributes.add(attr);
            }
        }
        return orphanAttributes;
    }

    /**
     * Checks if this Issue Type is system defined.
     * Such Issue types are specified in "IssueTypeConfig.properties"
     * file in the format "<SCARAB_ISSUE_TYPE.NAME>=system"
     *
     * @return True if this Issue Type is System defined. False otherwise
     */
    public boolean isSystemDefined()
        throws TorqueException
    {
        boolean systemDefined = false;
        String name = getName();
        if (name != null)
        {
            systemDefined = "system".equalsIgnoreCase(SYSTEM_CONFIG.getProperty(name));
        }
        return systemDefined;
    }


    /**
     * if an RMA is the chosen attribute for email subjects then return it.
     * if not explicitly chosen, choose the highest ordered text attribute.
     *
     * @return the Attribute to use as the email subject,
     * or null if no suitable Attribute could be found. 
     */
    public Attribute getDefaultTextAttribute(Module module)
        throws TorqueException
    {
        Attribute result = null;
        Object obj = ScarabCache.get(this, GET_DEFAULT_TEXT_ATTRIBUTE); 
        if (obj == null) 
        {        
            // get related RMAs
            Criteria crit = new Criteria()
                .add(RModuleAttributePeer.MODULE_ID, 
                     module.getModuleId());
            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.PREFERRED_ORDER);
            List rmas = getRModuleAttributes(crit);
            
            // the code to find the correct attribute could be quite simple by
            // looping and calling RMA.isDefaultText().  The code from
            // that method can be restructured here to more efficiently
            // answer this question.
            Iterator i = rmas.iterator();
            while (i.hasNext()) 
            {
                RModuleAttribute rma = (RModuleAttribute)i.next();
                if (rma.getDefaultTextFlag()) 
                {
                    result = rma.getAttribute();
                    break;
                }
            }
            
            if (result == null) 
            {
                // locate the highest ranked text attribute
                i = rmas.iterator();
                while (i.hasNext()) 
                {
                    RModuleAttribute rma = (RModuleAttribute)i.next();
                    Attribute testAttr = rma.getAttribute();
                    if (testAttr.isTextAttribute() && 
                         getAttributeGroup(module, testAttr).getActive()) 
                    {
                        result = testAttr;
                        break;
                    }
                }
            }
            ScarabCache.put(result, this, GET_DEFAULT_TEXT_ATTRIBUTE);
        }
        else 
        {
            result = (Attribute)obj;
        }
        return result;
    }

    /**
     * Array of Attributes used for quick search.
     *
     * @return an <code>List</code> of Attribute objects
     */
    public List getQuickSearchAttributes(Module module)
        throws TorqueException
    {
        List attributes = null;
        Object obj = ScarabCache.get(this, GET_QUICK_SEARCH_ATTRIBUTES, 
                                     module); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.QUICK_SEARCH, true);
            addOrderByClause(crit, module);
            attributes = getAttributes(crit);
            ScarabCache.put(attributes, this, GET_QUICK_SEARCH_ATTRIBUTES, 
                            module);
        }
        else 
        {
            attributes = (List)obj;
        }
        return attributes;
    }

    /**
     * gets a list of all of the Attributes in a Module based on the Criteria.
     */
    private List getAttributes(final Criteria criteria)
        throws TorqueException
    {
        final List moduleAttributes = getRModuleAttributes(criteria);
        final List attributes = new ArrayList(moduleAttributes.size());
        for (int i=0; i<moduleAttributes.size(); i++)
        {
            attributes.add(
               ((RModuleAttribute) moduleAttributes.get(i)).getAttribute());
        }
        return attributes;
    }
    
    /**
     * Checks whether the current user can create issues of this issueType
     * in the given module. Currently we only check whether the user is
     * allowed to create all necessary input (i.e. the required attributes).
     * If at least one attribute can not be set by the user due to transition
     * constraints, this method returns false, otherwise true.
     * @param user
     * @param module
     * @return
     * @throws TorqueException
     * @throws ScarabException
     */
    public boolean canCreateIssueInScope(ScarabUser user, Module module) throws TorqueException, ScarabException
    {
        boolean result = true;
        List requiredAttributes = getRequiredAttributes(module);
        Iterator iter = requiredAttributes.iterator();
        while(iter.hasNext())
        {
            Attribute attribute = (Attribute)iter.next();
            Workflow workflow = WorkflowFactory.getInstance();
            if(attribute.isOptionAttribute())
            {
                boolean canDoPartial = workflow.canMakeTransitionsFrom(user, this, attribute, null);
                if(!canDoPartial)
                {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Checks whether the current user is allowed to set the given attribute
     * in the given module and in this issueType. If due to transition rules
     * the user is not allowed to set the attribute value, this method returns
     * false, otherwise true.
     * @param user
     * @param module
     * @param attribute
     * @return
     * @throws TorqueException
     * @throws ScarabException
     */
    public boolean canCreateAttributeInScope(ScarabUser user, Module module, Attribute attribute) throws TorqueException, ScarabException
    {
        boolean result = true;
        Workflow workflow = WorkflowFactory.getInstance();
        if(attribute.isOptionAttribute())
        {
            result = workflow.canMakeTransitionsFrom(user, this, attribute, null);
        }
        return result;
    }
    
    

    /**
     * Array of Attributes which are active and required by this module.
     * Whose attribute group's are also active.
     * @return an <code>List</code> of Attribute objects
     */
    public List getRequiredAttributes(Module module)
        throws TorqueException
    {

        List attributes = null;
        Object obj = ScarabCache.get(this, GET_REQUIRED_ATTRIBUTES, 
                                     module); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria(3)
                .add(RModuleAttributePeer.REQUIRED, true);
            crit.add(RModuleAttributePeer.ACTIVE, true);
            addOrderByClause(crit, module);
            List temp =  getAttributes(crit);
            List requiredAttributes  = new ArrayList();
            for (int i=0; i <temp.size(); i++)
            {
                Attribute att = (Attribute)temp.get(i);
                AttributeGroup group = getAttributeGroup(module, att);
                if (group != null && group.getActive())
                {
                    requiredAttributes.add(att);
                }
            }
            attributes = requiredAttributes;
            ScarabCache.put(attributes, this, GET_REQUIRED_ATTRIBUTES, 
                            module);
        }
        else 
        {
            attributes = (List)obj;
        }
        return attributes;

    }

    /**
     * Array of active Attributes for an issue type.
     *
     * @return an <code>List</code> of Attribute objects
     */
    public List getActiveAttributes(final Module module)
        throws TorqueException
    {
        List attributes = null;
        Object obj = ScarabCache.get(this, GET_ACTIVE_ATTRIBUTES, module);
        if (obj == null)
        {
            final Criteria crit = new Criteria(2);
            crit.add(RModuleAttributePeer.ACTIVE, true);
            addOrderByClause(crit, module);
            attributes = getAttributes(crit);
            ScarabCache.put(attributes, this, GET_ACTIVE_ATTRIBUTES, 
                            module);
        }
        else
        {
            attributes = (List)obj;
        }
        return attributes;
    }

    private void addOrderByClause(Criteria crit, Module module)
    {
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
        crit.add(RModuleAttributePeer.MODULE_ID, module.getModuleId());
    }

    private AttributeGroup getAttributeGroup(Module module, 
                                             Attribute attribute)
        throws TorqueException
    {
        AttributeGroup group = null;
        Object obj = ScarabCache.get(this, GET_ATTRIBUTE_GROUP, 
                                     module, attribute); 
        if (obj == null)
        {
            Criteria crit = new Criteria()
                .add(AttributeGroupPeer.ISSUE_TYPE_ID, getIssueTypeId())
                .add(AttributeGroupPeer.MODULE_ID, 
                     module.getModuleId())
                .addJoin(RAttributeAttributeGroupPeer.GROUP_ID, 
                   AttributeGroupPeer.ATTRIBUTE_GROUP_ID)
                .add(RAttributeAttributeGroupPeer.ATTRIBUTE_ID, 
                     attribute.getAttributeId());
            List results = AttributeGroupPeer.doSelect(crit);
            if (results.size() > 0)
            {
                group = (AttributeGroup)results.get(0);
                ScarabCache.put(group, this, GET_ATTRIBUTE_GROUP, 
                                module, attribute);
            }
        }
        else 
        {
            group = (AttributeGroup)obj;
        }
        return group;
    }
}
