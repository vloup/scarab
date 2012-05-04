package org.tigris.scarab.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.turbine.util.RunData;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.NotificationStatusManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.word.IssueSearch;

public class ScarabSystemTool 
{

    /**
     * Return the system character set
     */
    public static String getDefaultCharacterSet()
    {
        return System.getProperty("file.encoding");
    }
    
}
