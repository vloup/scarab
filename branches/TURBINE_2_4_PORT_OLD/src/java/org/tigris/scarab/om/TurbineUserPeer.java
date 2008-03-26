package org.tigris.scarab.om;

import org.apache.torque.util.Criteria;

/**
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class TurbineUserPeer
    extends org.tigris.scarab.om.BaseTurbineUserPeer
{
    /**
     * Returns the number of rows returned from the criteria. The criteria should have an unique SelectColumn
     * (use crit.addSelectColumn("COUNT(*)").
     */
    public static int getUsersCount(Criteria critCount) throws Exception
    {
        java.util.List resultCount = TurbineUserPeer.doSelectVillageRecords(critCount);
        com.workingdogs.village.Record record = (com.workingdogs.village.Record) resultCount.get(0);                
        return record.getValue(1).asInt();        
    }    
}
