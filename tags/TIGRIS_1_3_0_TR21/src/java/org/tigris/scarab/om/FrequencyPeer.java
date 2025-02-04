package org.tigris.scarab.om;

import java.util.*;
import com.workingdogs.village.*;
import org.apache.torque.map.*;
import org.apache.torque.pool.DBConnection;
import org.apache.torque.util.Criteria;

// Local classes
import org.tigris.scarab.om.map.*;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class FrequencyPeer 
    extends org.tigris.scarab.om.BaseFrequencyPeer
{

    /**
     * Returns list of all frequency values.
     */
    public static List getFrequencies() throws Exception
    {
        return doSelect(new Criteria());
    }

}
