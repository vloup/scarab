package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Turbine classes
import org.apache.turbine.om.*;
import org.apache.turbine.om.peer.BasePeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.db.pool.DBConnection;
import org.apache.turbine.util.db.IDBroker;

import org.tigris.scarab.util.ScarabException;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  * You should add additional methods to this class to meet the
  * application requirements.  This class will only be generated as
  * long as it does not already exist in the output directory.

  */
public class Module 
    extends BaseModule
    implements Persistent
{
    private static final NumberKey ROOT_ID = new NumberKey("0");

    // private Map attributeOptions = new HashMap(); 


    /**
     * Creates a new Issue.
     *
     */
    public Issue getNewIssue(ScarabUser user)
        throws Exception
    {
        Issue issue = new Issue();
        issue.setModule( this );
        issue.setModifiedBy((NumberKey)user.getPrimaryKey());
        issue.setCreatedBy((NumberKey)user.getPrimaryKey());
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
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.DEDUPE, true)        
            .add(RModuleAttributePeer.ACTIVE, true);        
        return getAttributes(crit);
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

    public List getRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        Criteria crit = new Criteria(2);
        if ( activeOnly ) 
        {
            crit.add(RModuleOptionPeer.ACTIVE, true);
        }
        crit.add(AttributeOptionPeer.ATTRIBUTE_ID, attribute.getAttributeId());
        crit.addOrderByColumn(RModuleOptionPeer.PREFERRED_ORDER);
        crit.addOrderByColumn(AttributeOptionPeer.NUMERIC_VALUE);
        crit.addOrderByColumn(RModuleOptionPeer.DISPLAY_VALUE);
        crit.addOrderByColumn(AttributeOptionPeer.OPTION_NAME);

        List rModOpts = null;
        Module module = this;
        Module prevModule = null;
        do
        {
            rModOpts = module.getRModuleOptionsJoinAttributeOption(crit);
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
        return getLeafRModuleOptions(attribute, true);
    }

    public List getLeafRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List rModOpts = getRModuleOptions(attribute, activeOnly);

        // put options in a map for searching
        Map optionsMap = new HashMap((int)(rModOpts.size()*1.5));
        for ( int i=rModOpts.size()-1; i>=0; i-- ) 
        {
            AttributeOption option = ((RModuleOption)rModOpts.get(i))
                .getAttributeOption();
            optionsMap.put(option.getOptionId(), null);
        }

        // remove options with descendants in the list
        for ( int i=rModOpts.size()-1; i>=0; i-- ) 
        {
            RModuleOption modOpt = (RModuleOption)rModOpts.get(i);
            List descendants = modOpt.getAttributeOption().getDescendants();
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
            AttributeOption option = ((RModuleOption)moduleOptions.get(i))
                .getAttributeOption();
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
     * Saves the module into the database
     */
    public void save() throws Exception
    {
        // if new, relate the Module to the user who created it.
        if ( isNew() ) 
        {
            RModuleUser relation = new RModuleUser();
            if ( getOwnerId() == null ) 
            {
                throw new ScarabException("Can't save a project without" + 
                    "first assigning an owner.");
            }         
            relation.setUserId(getOwnerId());
            relation.setDeleted(false);
            addRModuleUser(relation);

            // make sure the code has a value;
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

    /**
        calls the doPopulate() method with validation false
    */
    public Module doPopulate(RunData data)
        throws Exception
    {
        return doPopulate(data, false);
    }

    /**
        populates project based on the existing project data from POST
    */
    public Module doPopulate(RunData data, boolean validate)
        throws Exception
    {
        String prefix = ""; //getQueryKey().toLowerCase();

        if ( isNew() ) 
        {
            String project_id = data.getParameters()
                .getString(prefix + "id", null); 
            if (validate)
            {
                if (project_id == null)
                    throw new Exception ( "Missing project_id!" );
            }
            setPrimaryKey(new NumberKey(project_id));
            // setCreatedBy( ((ScarabUser)data.getUser()).getPrimaryKey() );
            // setCreatedDate( new Date() );
        }

        String name = data.getParameters().getString(prefix + "name",null);
        String desc = data.getParameters()
            .getString(prefix + "description",null);
        
        if (validate)
        {
            if (! StringUtils.isValid(name))
                throw new Exception ( "Missing project name!" );
            if (! StringUtils.isValid(desc))
                throw new Exception ( "Missing project description!" );
        }

        setName( StringUtils.makeString( name ));
        setDescription( StringUtils.makeString( desc ));
        setUrl( StringUtils.makeString(
            data.getParameters().getString(prefix + "url") ));
        setOwnerId( new NumberKey(data.getParameters().getString(prefix + "ownerid") ));
        setQaContactId( new NumberKey(data.getParameters()
                        .getString(prefix + "qacontactid") ));
        return this;
    }

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



}
