package org.tigris.scarab.om;

import java.util.*;
import com.workingdogs.village.*;

// Turbine classes
import org.apache.torque.om.ObjectKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.pool.DBConnection;

// Scarab classes
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.ScarabException;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class ReportPeer 
    extends org.tigris.scarab.om.BaseReportPeer
{
    private static final String REPORT_PEER = 
        "ReportPeer";
    private static final String RETRIEVE_BY_PK = 
        "retrieveByPK";

    /**
     * Does a saved report exist under the given name.
     *
     * @param name a <code>String</code> report name
     * @return true if a report by the given name exists
     */
    public static boolean exists(String name)
        throws Exception
    {
        return retrieveByName(name) != null;
    }

    /**
     * gets the active report saved under the given name
     *
     * @param name a <code>String</code> value
     * @return a <code>Report</code> value
     * @exception Exception if an error occurs
     */
    public static Report retrieveByName(String name)
        throws Exception
    {
        Report report = null;
        Criteria crit = new Criteria()
            .add(NAME, name)
            .add(DELETED, false);
        List reports = doSelect(crit);
        if ( reports.size() == 1 ) 
        {
            report = (Report)reports.get(0);
        }
        else if ( reports.size() > 1 ) 
        {
            throw new ScarabException(
                "Multiple reports are active under the name, " + name + 
                ".  Application error, please notify the developers.");
        }
        
        return report;
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param ObjectKey pk
     */
    public static Report retrieveByPK( ObjectKey pk )
        throws Exception
    {
        Report result = null;
        Object obj = ScarabCache.get(REPORT_PEER, RETRIEVE_BY_PK, pk); 
        if ( obj == null ) 
        {        
            result = BaseReportPeer.retrieveByPK(pk);
            ScarabCache.put(result, REPORT_PEER, RETRIEVE_BY_PK, pk);
        }
        else 
        {
            result = (Report)obj;
        }
        return result;
    }
}
