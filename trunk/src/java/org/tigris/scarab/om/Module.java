package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.services.db.om.*;
import org.apache.turbine.om.security.*;
import org.apache.turbine.services.db.om.peer.BasePeer;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.services.db.util.IDBroker;
import org.apache.turbine.services.cache.*;
import org.apache.turbine.services.*;

import org.tigris.scarab.util.*;
//import org.tigris.scarab.services.module.ModuleService;
import org.tigris.scarab.services.module.ModuleEntity;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public abstract class Module 
    extends BaseModule
    implements Persistent, ModuleEntity
{
    protected static final NumberKey ROOT_ID = new NumberKey("0");

    private Attribute[] activeAttributes;
    private Attribute[] dedupeAttributes;
    private Attribute[] quicksearchAttributes;
    private List allRModuleAttributes;
    private List activeRModuleAttributes;


    /* *
     * Get a fresh instance of a Module
     * /
    public static ModuleEntity getInstance()
        throws Exception
    {
        return ((ModuleService)TurbineServices
                .getInstance().getService(ModuleService.SERVICE_NAME))
                .getInstance();
    }

    /* *
     * Return an instance of Module based on the passed in module id
     * /
    public static ModuleEntity getInstance(ObjectKey modId) 
        throws Exception
    {
        return ((ModuleService)TurbineServices
                .getInstance().getService(ModuleService.SERVICE_NAME))
                .getInstance(modId);
    }
    */
    
    /**
     * Wrapper method to perform the proper cast to the BaseModule method
     * of the same name.
     */
    public void setModuleRelatedByParentId(ModuleEntity v) throws Exception
    {
        super.setModuleRelatedByParentId((Module)v);
    }

    public ModuleEntity getModuleRelatedByParentIdCast() throws Exception
    {
        return (ModuleEntity) super.getModuleRelatedByParentId();
    }

    /**
     * Creates a new Issue.
     *
     */
    public Issue getNewIssue(ScarabUser user)
        throws Exception
    {
        Issue issue = new Issue();
        issue.setModuleCast( (ModuleEntity) this );
//        issue.setModifiedBy((NumberKey)user.getUserId());
//        issue.setCreatedBy((NumberKey)user.getUserId());
        java.util.Date now = new java.util.Date();
        issue.setModifiedDate(now);
        issue.setCreatedDate(now);
        issue.setDeleted(false);
        return issue;
    }

    /**
     * gets a list of all of the Attributes in a Module based on the Criteria.
     */
    public Attribute[] getAttributes(Criteria criteria)
        throws Exception
    {
        List moduleAttributes = 
            getRModuleAttributes(criteria);

        Attribute[] attributes = new Attribute[moduleAttributes.size()];
        for ( int i=0; i<moduleAttributes.size(); i++ ) 
        {
            attributes[i] = 
               ((RModuleAttribute) moduleAttributes.get(i)).getAttribute();
        }
        return attributes;
    }

    /**
     * Array of Attributes used for deduping.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getDedupeAttributes()
        throws Exception
    {
        if ( dedupeAttributes == null ) 
        {
            Criteria dedupeCriteria = new Criteria(3)
                .add(RModuleAttributePeer.DEDUPE, true)        
                .add(RModuleAttributePeer.ACTIVE, true)
                .addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER)
                .addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
            dedupeAttributes = getAttributes(dedupeCriteria);
        }
        
        return dedupeAttributes;
    }

    /**
     * Array of Attributes used for quick search.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getQuickSearchAttributes()
        throws Exception
    {
        if ( quicksearchAttributes == null ) 
        {
            Criteria quicksearchCriteria = new Criteria(3)
                .add(RModuleAttributePeer.QUICK_SEARCH, true)        
                .add(RModuleAttributePeer.ACTIVE, true)
                .addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER)
                .addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
            quicksearchAttributes = getAttributes(quicksearchCriteria);
        }

        return quicksearchAttributes;
    }

    /**
     * Array of active Attributes.
     *
     * @return an <code>Attribute[]</code> value
     */
    public Attribute[] getActiveAttributes()
        throws Exception
    {
        if ( activeAttributes == null ) 
        {
            Criteria activeCriteria = new Criteria(2)
                .add(RModuleAttributePeer.ACTIVE, true)        
                .addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER)
                .addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);
            activeAttributes = getAttributes(activeCriteria);
        }

        return activeAttributes;
    }

    public RModuleAttribute getRModuleAttribute(Attribute attribute)
        throws Exception
    {
        RModuleAttribute rma = null;
        List rmas = getRModuleAttributes(false);
        Iterator i = rmas.iterator();
        while ( i.hasNext() ) 
        {
            rma = (RModuleAttribute)i.next();
            if ( rma.getAttribute().equals(attribute) ) 
            {
                break;
            }
        }
        
        return rma;
    }

    public List getRModuleAttributes(boolean activeOnly)
        throws Exception
    {
        List allRModuleAttributes = null;
        List activeRModuleAttributes = null;
        // note this code could potentially read information from the
        // db multiple times (MT), but this is okay
        if ( this.allRModuleAttributes == null ) 
        {
            allRModuleAttributes = getAllRModuleAttributes();
            this.allRModuleAttributes = allRModuleAttributes; 
        }
        else 
        {
            allRModuleAttributes = this.allRModuleAttributes; 
        }

        if ( activeOnly ) 
        {
            if ( this.activeRModuleAttributes == null ) 
            {
                activeRModuleAttributes = 
                    new ArrayList(allRModuleAttributes.size());
                for ( int i=0; i<allRModuleAttributes.size(); i++ ) 
                {
                    RModuleAttribute rma = 
                        (RModuleAttribute)allRModuleAttributes.get(i);
                    if ( rma.getActive() ) 
                    {
                        activeRModuleAttributes.add(rma);
                    }
                }
                
                this.activeRModuleAttributes = activeRModuleAttributes; 
            }
            else 
            {
                activeRModuleAttributes = this.activeRModuleAttributes; 
            }
               
            return activeRModuleAttributes;
        }
        else 
        {
            return allRModuleAttributes;
        }        
    }
    
    private List getAllRModuleAttributes()
        throws Exception
    {
        Criteria crit = new Criteria(0);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleAttributePeer.DISPLAY_VALUE);

        List rModAtts = null;
        ModuleEntity module = this;
        ModuleEntity prevModule = null;
        do
        {
            rModAtts = module.getRModuleAttributes(crit);
            prevModule = module;
            module = (ModuleEntity) prevModule.getModuleRelatedByParentIdCast();
        }
        while ( rModAtts.size() == 0 && 
               !ROOT_ID.equals(prevModule.getModuleId()));
        return rModAtts;        
    }

    /**
     * gets a list of all of the Attributes.
     */
    public Attribute[] getAllAttributes()
        throws Exception
    {
        return getAttributes(new Criteria());
    }

    public List getRModuleOptions(Attribute attribute)
        throws Exception
    {
        return getRModuleOptions(attribute, true);
    }

    private Map allRModuleOptionsMap = new HashMap();
    private Map activeRModuleOptionsMap = new HashMap();

    public List 
        getRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List allRModuleOptions = (List)allRModuleOptionsMap.get(attribute);
        if ( allRModuleOptions == null ) 
        {
            allRModuleOptions = getAllRModuleOptions(attribute);
            allRModuleOptionsMap.put(attribute, allRModuleAttributes); 
        }

        if ( activeOnly ) 
        {
            List activeRModuleOptions = 
                (List)activeRModuleOptionsMap.get(attribute);
            if ( activeRModuleOptions == null ) 
            {
                activeRModuleOptions = 
                    new ArrayList(allRModuleOptions.size());
                for ( int i=0; i<allRModuleOptions.size(); i++ ) 
                {
                    RModuleOption rmo = 
                        (RModuleOption)allRModuleOptions.get(i);
                    if ( rmo.getActive() ) 
                    {
                        activeRModuleOptions.add(rmo);
                    }
                }
                
                activeRModuleOptionsMap
                    .put(attribute, activeRModuleOptions); 
            }

            return activeRModuleOptions;
        }
        else 
        {
            return allRModuleOptions;
        }        
    }

    private List getAllRModuleOptions(Attribute attribute)
        throws Exception
    {
        AttributeOption[] options = attribute.getAttributeOptions(false);
        NumberKey[] optIds = null;
        if (options == null)
        {
            optIds = new NumberKey[0];
        }
        else
        {
            optIds = new NumberKey[options.length];
        }
        for ( int i=optIds.length-1; i>=0; i-- ) 
        {
            optIds[i] = options[i].getOptionId();
        }
        
        Criteria crit = new Criteria(2);
        crit.addIn(RModuleOptionPeer.OPTION_ID, optIds);
        crit.addAscendingOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        crit.addAscendingOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);

        List rModOpts = null;
        Module module = this;
        Module prevModule = null;
        do
        {
            rModOpts = module.getRModuleOptions(crit);
            prevModule = module;
            module = prevModule.getModuleRelatedByParentId();
        }
        while ( rModOpts.size() == 0 && 
               !ROOT_ID.equals((NumberKey)prevModule.getPrimaryKey()));
        return rModOpts;
    }

    public List getLeafRModuleOptions(Attribute attribute)
        throws Exception
    {
        try
        {
        return getLeafRModuleOptions(attribute, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List getLeafRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List rModOpts = getRModuleOptions(attribute, activeOnly);

        // put options in a map for searching
        Map optionsMap = new HashMap((int)(rModOpts.size()*1.5));
        for ( int i=rModOpts.size()-1; i>=0; i-- ) 
        {
            RModuleOption rmo = (RModuleOption)rModOpts.get(i);
            optionsMap.put(rmo.getOptionId(), null);
        }

        // remove options with descendants in the list
        for ( int i=rModOpts.size()-1; i>=0; i-- ) 
        {
            AttributeOption option = 
                ((RModuleOption)rModOpts.get(i)).getAttributeOption();
            List descendants = option.getDescendants();
            if ( descendants != null ) 
            {
                for ( int j=descendants.size()-1; j>=0; j-- ) 
                {
                    AttributeOption descendant = 
                        (AttributeOption)descendants.get(j);
                    if ( optionsMap.containsKey(descendant.getOptionId()) ) 
                    {
                        rModOpts.remove(i);
                        break;
                    }
                }
            }
        }
        
        return rModOpts;
    }

    /**
     * Gets a list of active RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionTree(Attribute attribute)
        throws Exception
    {
        return getOptionTree(attribute, true);
    }

    /**
     * Gets a list of RModuleOptions which have had their level
     * within the options for this module set.
     *
     * @param attribute an <code>Attribute</code> value
     * @param activeOnly a <code>boolean</code> value
     * @return a <code>List</code> value
     * @exception Exception if an error occurs
     */
    public List getOptionTree(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List moduleOptions = null;
try{
        moduleOptions = getRModuleOptions(attribute, activeOnly);
        int size = moduleOptions.size();
        List[] ancestors = new List[size];

        // put option id's in a map for searching and find all ancestors
        Map optionsMap = new HashMap((int)(size*1.5));
        for ( int i=size-1; i>=0; i-- ) 
        {
            AttributeOption option =
                ((RModuleOption)moduleOptions.get(i)).getAttributeOption();
            optionsMap.put(option.getOptionId(), null);

            List moduleOptionAncestors = option.getAncestors();
            ancestors[i] = moduleOptionAncestors;
        }

        for ( int i=0; i<size; i++ ) 
        {
            RModuleOption moduleOption = (RModuleOption)moduleOptions.get(i);
            AttributeOption attributeOption = 
                moduleOption.getAttributeOption();

            int level = 1;
            if ( ancestors[i] != null ) 
            {
                for ( int j=ancestors[i].size()-1; j>=0; j-- ) 
                {
                    AttributeOption option = 
                        (AttributeOption)ancestors[i].get(j);
                    
                    if ( optionsMap.containsKey(option.getOptionId()) &&
                         !option.getOptionId()
                         .equals(moduleOption.getOptionId()) ) 
                    {
                        moduleOption.setLevel(level++);   
                    }
                }
            }            
        }
}catch (Exception e){e.printStackTrace();}

        return moduleOptions;

    }


    /**
     * Gets users which are currently associated (relationship has not 
     * been deleted) with this module who have the given permssion. 
     *
     * @param partialUserName username fragment to match against
     * @param permissions a <code>String[]</code> permission
     * @return a <code>List</code> of ScarabUsers
     * @exception Exception if an error occurs
     */
    public List getUsers(String permission)
        throws Exception
    {
        return getUsers(null, permission);
    }

    /**
     * Gets users which are currently associated (relationship has not 
     * been deleted) with this module who have the given permssion. 
     *
     * @param partialUserName username fragment to match against
     * @param permissions a <code>String[]</code> permission
     * @return a <code>List</code> of ScarabUsers
     * @exception Exception if an error occurs
     */
    public List getUsers(String partialUserName, String permission)
        throws Exception
    {
        String[] perms = new String[1];
        perms[0] = permission;
        return getUsers(partialUserName, perms);
    }

    /**
     * Determines whether this module allows users to vote many times for
     * the same issue.  This feature needs schema change to allow a 
     * configuration screen.  Currently only one vote per issue is supported
     *
     * @return false
     */
    public boolean allowsMultipleVoting()
    {
        return false;
    }

    /**
     * How many votes does the user have left to cast.  Currently always
     * returns 1, so a user has unlimited voting rights.  Should look to 
     * UserVote for the answer when implemented properly.
     */
    public int getUnusedVoteCount(ScarabUser user)
    {
        return 1;
    }

    /**
     * Gets users which are currently associated (relationship has not 
     * been deleted) with this module who have the given permssions. 
     *
     * @param partialUserName username fragment to match against
     * @param permissions a <code>String[]</code> permissions
     * @return a <code>List</code> of ScarabUsers
     * @exception Exception if an error occurs
     */
    public abstract List getUsers(String partialUserName, String[] permissions)
        throws Exception;

    /**
     * Saves the module into the database
     */
    public void save() throws Exception
    {
        // if new, make sure the code has a value.
        if ( isNew() ) 
        {
            String code = getCode();
            if ( code == null || code.length() == 0 ) 
            {
                if ( getParentId().equals(ROOT_ID) ) 
                {
                    throw new ScarabException("A top level module addition was"
                        + " attempted without assigning a Code");
                }
            
                setCode(getModuleRelatedByParentId().getCode());

                // insert a row into the id_table.
                Criteria criteria = new Criteria(
                    ModulePeer.getTableMap().getDatabaseMap().getName(), 5)
                    .add(IDBroker.TABLE_NAME, getCode())
                    .add(IDBroker.NEXT_ID, 1)
                    .add(IDBroker.QUANTITY, 1);
                BasePeer.doInsert(criteria);
            }            
        }
        super.save();        
    }

    /*
    public class OptionInList
    {
        public int Level;
        private AttributeOption attOption;
        private RModuleOption modOption;

        public OptionInList(int level, AttributeOption option)
        {
            Level = level;
            attOption = option;
        }

        public OptionInList(int level, RModuleOption option)
        {
            Level = level;
            modOption = option;
        }
         
        public boolean equals(Object obj)
        {
            OptionInList oil = (OptionInList)obj;
            return Level == oil.Level 
                && (attOption == null || attOption.equals(oil.attOption)) 
                && (modOption == null || modOption.equals(oil.modOption)); 
        }

        public NumberKey getOptionId()
        {
            if ( attOption != null ) 
            {
                return attOption.getOptionId();
            }
            else 
            {
                return modOption.getOptionId();
            }
            
        }

        public String getDisplayValue()
        {
            if ( attOption != null ) 
            {
                return attOption.getName();
            }
            else 
            {
                return modOption.getDisplayValue();
            }
        }
    }
    */
}
