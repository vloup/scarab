package org.tigris.scarab.om;

// JDK classes
import java.util.*;
import java.sql.Connection;

// Turbine classes
import org.apache.turbine.services.db.om.*;
// import org.apache.turbine.services.db.om.peer.BasePeer;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.ObjectUtils;
import org.apache.turbine.services.resources.TurbineResources;
import org.apache.turbine.util.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;
import org.apache.turbine.util.Log;
import org.apache.turbine.util.SequencedHashtable;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.services.db.pool.DBConnection;
import org.apache.turbine.services.db.map.DatabaseMap;

import org.tigris.scarab.util.ScarabConstants;
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
public class Issue 
    extends BaseIssue
    implements Persistent
{

    public String getUniqueId() // throws Exception
    {
        return getIdPrefix() + getIdCount();
    }

    public String getFederatedId()
    {
        if ( getIdDomain() != null ) 
        {
            return getIdDomain() + getUniqueId();
        }
        return getUniqueId();
    }

    public void setFederatedId(String id)
    {
        FederatedId fid = new FederatedId(id);
        setIdDomain(fid.getDomain());
        setIdPrefix(fid.getPrefix());
        setIdCount(fid.getCount());
    }
     
    public static class FederatedId
    {
        String domainId;
        String prefix;
        int count;

        public FederatedId(String id)
        {
            int dash = id.indexOf('-');
            if ( dash > 0 ) 
            {
                domainId = id.substring(0, dash);
                setUniqueId(id.substring(dash+1));
            }
            else 
            {
                setUniqueId(id);
            }
        }

        public void setUniqueId(String id)
        {
            // we could start at 1 here, if the spec says one char is 
            // required, will keep it safe for now.
            StringBuffer code = new StringBuffer(4);
            for ( int i=0; i<4; i++) 
            {
                char c = id.charAt(i);
                if ( c != '0' && c != '1' && c != '2' && c != '3' && c != '4'
                     && c != '5' && c != '6' && c != '7' && c!='8' && c!='9' )
                {
                    code.append(c);
                }
            }
            prefix = code.toString();
            count = Integer.parseInt( id.substring(code.length()) );
            
        }

        /**
         * Get the Prefix
         * @return String
         */
        public String getPrefix()
        {
            return prefix;
        }
        
        
        /**
         * Get the Count
         * @return int
         */
        public int getCount()
        {
            return count;
        }
        
        /**
         * Get the IdInstance
         * @return String
         */
        public String getDomain()
        {
            return domainId;
        }
    }

    public static Issue getIssueById(String id)
    {
        FederatedId fid = new FederatedId(id);
        return getIssueById(fid);
    }

    public static Issue getIssueById(FederatedId fid)
    {
        Criteria crit = new Criteria(5)
            .add(IssuePeer.ID_PREFIX, fid.getPrefix())
            .add(IssuePeer.ID_COUNT, fid.getCount());

        if (  fid.getDomain() != null ) 
        {
            crit.add(IssuePeer.ID_DOMAIN, fid.getDomain());    
        }
        
        Issue issue = null;
        try
        {
            issue = (Issue)IssuePeer.doSelect(crit).get(0);
        }
        catch (Exception e) 
        {
            // return null
        }
        return issue;
    }

    /**
     * AttributeValues that are relevant to the issue's current module.
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.  The values are ordered
     * according to the module's preference
     */
    public SequencedHashtable getModuleAttributeValuesMap() throws Exception
    {
        SequencedHashtable map = null; 

        try{
        Attribute[] attributes = null;
        HashMap siaValuesMap = null;
        // this exception is getting lost 
        attributes = getModule().getActiveAttributes();
        siaValuesMap = getAttributeValuesMap();

        map = new SequencedHashtable( (int)(1.25*attributes.length + 1) );
        for ( int i=0; i<attributes.length; i++ ) 
        {
            String key = attributes[i].getName().toUpperCase();

            if ( siaValuesMap.containsKey(key) ) 
            {
                map.put( key, siaValuesMap.get(key) );
            }
            else 
            {
                AttributeValue aval = AttributeValue
                    .getNewInstance(attributes[i], this);
                addAttributeValue(aval);
                map.put( key, aval );
            }
        }
        }catch (Exception e){e.printStackTrace();}
        return map;
    }

    public AttributeValue getAttributeValue(Attribute attribute)
       throws Exception
    {
        Criteria crit = new Criteria(2)
            .add(AttributeValuePeer.DELETED, false)        
            .add(AttributeValuePeer.ATTRIBUTE_ID, attribute.getAttributeId());

        List avals = getAttributeValues(crit);
        AttributeValue aval = null;
        if ( avals.size() == 1 ) 
        {
            aval = (AttributeValue)avals.get(0);
        }

        return aval;
    }


    /**
     * AttributeValues that are set for this Issue in the order
     * that is preferred for this module
     */
    public AttributeValue[] getOrderedAttributeValues() throws Exception
    {        
        Map values = getAttributeValuesMap();
        Attribute[] attributes = getModule().getActiveAttributes();

        return orderAttributeValues(values, attributes);
    }


    /**
     * Extract the AttributeValues from the Map according to the 
     * order in the Attribute[]
     */
    private AttributeValue[] orderAttributeValues(Map values, 
                                                  Attribute[] attributes) 
        throws Exception
    {
        AttributeValue[] orderedValues = new AttributeValue[values.size()];
        try{

        int i=0;
        for ( int j=0; j<attributes.length; j++ ) 
        {
            AttributeValue av = (AttributeValue) values
                .remove( attributes[j].getName().toUpperCase() );
            if ( av != null ) 
            {
                orderedValues[i++] = av;                
            }
        }
        Iterator iter = values.values().iterator();
        while ( iter.hasNext() ) 
        {
            orderedValues[i++] = (AttributeValue)iter.next();
        }

        }catch (Exception e){e.printStackTrace();}

        for ( int j=0; j<orderedValues.length; j++ ) 
        {
            
        }
        return orderedValues;
    }


    /**
     * AttributeValues that are set for this Issue
     */
    public HashMap getAttributeValuesMap() throws Exception
    {
        Criteria crit = new Criteria(2)
            .add(AttributeValuePeer.DELETED, false);        
        List siaValues = getAttributeValues(crit);
        HashMap map = new HashMap( (int)(1.25*siaValues.size() + 1) );
        for ( int i=0; i<siaValues.size(); i++ ) 
        {
            AttributeValue att = (AttributeValue) siaValues.get(i);
            String name = att.getAttribute().getName();
            map.put(name.toUpperCase(), att);
        }

        return map;
    }


    /**
     * AttributeValues that are set for this issue and
     * Empty AttributeValues that are relevant for the module, but have 
     * not been set for the issue are included.
     */
    public HashMap getAllAttributeValuesMap() throws Exception
    {
        Map moduleAtts = getModuleAttributeValuesMap();
        Map issueAtts = getAttributeValuesMap();
        HashMap allValuesMap = new HashMap( (int)(1.25*(moduleAtts.size() + 
                                            issueAtts.size())+1) );

        allValuesMap.putAll(moduleAtts);
        allValuesMap.putAll(issueAtts);
        return allValuesMap;
    }


    public boolean containsMinimumAttributeValues()
        throws Exception
    {
        Criteria crit = new Criteria(3)
            .add(RModuleAttributePeer.ACTIVE, true)        
            .add(RModuleAttributePeer.REQUIRED, true);        
        Attribute[] attributes = getModule().getAttributes(crit);
        //        Vector moduleAttributes = 
        //    getModule().getRModuleAttributes(crit);
        
        boolean result = true;
        SequencedHashtable avMap = getModuleAttributeValuesMap(); 
        Iterator i = avMap.iterator();
        while (i.hasNext()) 
        {
            AttributeValue aval = (AttributeValue)avMap.get(i.next());
            
            if ( aval.getOptionId() == null && aval.getValue() == null ) 
            {
                for ( int j=attributes.length-1; j>=0; j-- ) 
                {
                    if ( aval.getAttribute().getPrimaryKey().equals(
                         attributes[j].getPrimaryKey() )) 
                    {
                        result = false;
                        break;
                    }                    
                }
                if ( !result ) 
                {
                    break;
                }
            }
        }

        return result;
    }       

    /**
     * Returns userid, the value of the "AssignedTo" Attribute 
     */
    public List getAssignedTo() throws Exception
    {
        ArrayList assignees = new ArrayList();
        Criteria crit = new Criteria()
            .addJoin(AttributeValuePeer.ATTRIBUTE_ID, 
                     AttributePeer.ATTRIBUTE_ID)        
            .add(AttributePeer.ATTRIBUTE_NAME, "Assigned To");
        List attValues = getAttributeValues(crit);
        for ( int i=0; i<attValues.size(); i++ ) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            assignees.add(attVal.getValue());
        }
        return assignees;
    }

        
    /**
     * Returns a list of Attachment objects with type "Comment"
     * That are associated with this issue.
     * TODO: get attachment type by name instead of by id
     */
    public Vector getComments() throws Exception
    {
        Criteria crit = new Criteria()
            .add(AttachmentPeer.ISSUE_ID, getIssueId())
            .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                     AttachmentPeer.ATTACHMENT_TYPE_ID)
            .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 2);
        return  AttachmentPeer.doSelect(crit);
    }

    /**
     * Returns a list of Attachment objects with type "URL"
     * That are associated with this issue.
     * TODO: get attachment type by name instead of by id
     */
    public Vector getUrls() throws Exception
    {
        Criteria crit = new Criteria()
            .add(AttachmentPeer.ISSUE_ID, getIssueId())
            .addJoin(AttachmentTypePeer.ATTACHMENT_TYPE_ID,
                     AttachmentPeer.ATTACHMENT_TYPE_ID)
            .add(AttachmentTypePeer.ATTACHMENT_TYPE_ID, 3);
        return  AttachmentPeer.doSelect(crit);
    }

    /**
     * Returns list of Activity objects associated with this Issue.
     */
    public Vector getActivity() throws Exception  
    {
        Criteria crit = new Criteria()
            .add(ActivityPeer.ISSUE_ID, getIssueId());
        return ActivityPeer.doSelect(crit);
    }

    /**
     * Returns list of child issues
     * i.e., related to this issue through the DEPEND table.
     */
    public List getChildren() throws Exception  
    {
        ArrayList children = new ArrayList();
        Criteria crit = new Criteria()
            .add(DependPeer.OBSERVED_ID, getIssueId());
        Vector depends = DependPeer.doSelect(crit);
        for ( int i=0; i<depends.size(); i++ ) 
        {
            Depend depend = (Depend) depends.get(i); 
            Issue childIssue = (Issue) IssuePeer.retrieveByPK(new NumberKey(depend.getObserverId()));
            children.add(childIssue);
        }
        return children;
    }

    /**
     * Returns list of parent issues
     * i.e., related to this issue through the DEPEND table.
     */
    public List getParents() throws Exception  
    {
        ArrayList parents = new ArrayList();
        Criteria crit = new Criteria()
            .add(DependPeer.OBSERVER_ID, getIssueId());
        Vector depends = DependPeer.doSelect(crit);
        for ( int i=0; i<depends.size(); i++ ) 
        {
            Depend depend = (Depend) depends.get(i); 
            Issue parentIssue = (Issue) IssuePeer.retrieveByPK(new NumberKey(depend.getObservedId()));
            parents.add(parentIssue);
        }
        return parents;
    }
        
    /**
     * Returns list of all types of dependencies an issue can have
     * On another issue.
     */
    public Vector getAllDependencyTypes() throws Exception
    {
        return DependTypePeer.doSelect(new Criteria());
    }

    /**
     * Returns type of dependency the passed-in issue has on
     * This issue.
     */
    public Depend getDependency(Issue childIssue) throws Exception
    {
        Depend depend = null;
        Criteria crit = new Criteria(2)
            .add(DependPeer.OBSERVED_ID, getIssueId() )        
            .add(DependPeer.OBSERVER_ID, childIssue.getIssueId() );        
        Vector depends = DependPeer.doSelect(crit);
        if (depends.size() > 0) 
            depend = (Depend)depends.get(0);
        return depend;
    }

    /**
     * Removes any unset attributes and sets the issue # prior to saving
     * for the first time.  Calls super.save()
     *
     * @param dbCon a <code>DBConnection</code> value
     * @exception Exception if an error occurs
     */
    public void save(DBConnection dbCon)
        throws Exception
    {
        // remove unset AttributeValues before saving
        List attValues = getAttributeValues();
        // reverse order since removing from list
        for ( int i=attValues.size()-1; i>=0; i-- ) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if ( !attVal.isSet() ) 
            {
                attValues.remove(i);
            }
        }

        // set the issue id
        if ( isNew() ) 
        {
            String prefix = getModule().getCode();

            /* thinking of keeping this in separate column
            String instanceCode = TurbineResources
                .getString(ScarabConstants.INSTANCE_NAME);
            if ( instanceCode != null && instanceCode.length() > 0 ) 
            {
                prefix = instanceCode + "-" + prefix;
            }
            */

            DatabaseMap dbMap = IssuePeer.getTableMap().getDatabaseMap();
            Connection con = dbCon.getConnection();
            int numId = dbMap.getIDBroker().getIdAsInt(con, prefix);

            setIdPrefix(prefix);
            setIdCount(numId);
        }

        super.save(dbCon);
    }


    /**
     * Performs a search over an issue's attribute values.
     *
     * @param keywords a <code>String[]</code> value
     * @param useAnd, an AND search if true, otherwise OR
     * @return a <code>List</code> value
     */
    public static List searchKeywords(String[] keywords, boolean useAnd)
        throws Exception
    {
        Criteria c = new Criteria(0);
        return IssuePeer.doSelect(c);
    }


    /**
     * The Date when this issue was closed.
     *
     * @return a <code>Date</code> value, null if status has not been
     * set to Closed
     */
    public Date getClosedDate()
        throws Exception
    {
        Date closedDate = null;
        AttributeValue status = 
            getAttributeValue(Attribute.getInstance(Attribute.STATUS__PK));
        if ( status != null && 
             status.getOptionId().equals(AttributeOption.STATUS__CLOSED__PK) ) 
        {
            // the issue is currently in a closed state, we can get the date
            Criteria crit = new Criteria()
                .add(ActivityPeer.ISSUE_ID, getIssueId())
                .add(ActivityPeer.ATTRIBUTE_ID, Attribute.STATUS__PK)
                .addJoin(ActivityPeer.TRANSACTION_ID, 
                         TransactionPeer.TRANSACTION_ID)
                .add( ActivityPeer.NEW_VALUE, 
                      AttributeOption.STATUS__CLOSED__PK.toString() )
                .addDescendingOrderByColumn(TransactionPeer.CREATED_DATE);
 
            List transactions = TransactionPeer.doSelect(crit);
            if ( transactions.size() > 0 ) 
            {
                closedDate = ((Transaction)transactions.get(0))
                    .getCreatedDate();
            }
            else 
            {
                throw new ScarabException("Issue " + getIssueId() + 
                    " was in a closed state, but" +
                    "no transaction is associated with the change.");   
            }
        }
          
        return closedDate;
    }

    public void addVote()
    {
    }
}
