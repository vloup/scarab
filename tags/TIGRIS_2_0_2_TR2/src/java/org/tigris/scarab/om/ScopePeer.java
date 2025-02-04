package org.tigris.scarab.om;

import java.util.List;
import org.apache.torque.om.NumberKey;
import com.workingdogs.village.*;
import org.apache.torque.map.*;
import org.apache.torque.util.Criteria;

// Local classes
import org.tigris.scarab.om.map.*;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class ScopePeer 
    extends org.tigris.scarab.om.BaseScopePeer
{

    /**
     *  Gets a List of all of the scopes.
     */
    public static List getAllScopes()
        throws Exception
    {
        return doSelect(new Criteria());
    }
}
