package org.tigris.scarab.om;

// JDK classes
import java.util.*;

// Village classes
import com.workingdogs.village.*;

// Turbine classes
import org.apache.turbine.om.peer.*;
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.db.map.*;
import org.apache.turbine.util.db.pool.DBConnection;

// Local classes
import org.tigris.scarab.om.map.*;
import org.tigris.scarab.services.module.ModuleManager;

/** 
  * The skeleton for this class was autogenerated by Torque on:
  *
  * [Wed Feb 28 16:36:26 PST 2001]
  *
  *  You should add additional methods to this class to meet the
  *  application requirements.  This class will only be generated as
  *  long as it does not already exist in the output directory.
  */
public class ModulePeer 
    extends org.tigris.scarab.om.BaseModulePeer
{
    public static Class getOMClass()
        throws Exception
    {
        return ModuleManager.getModuleClass();
    }
}

