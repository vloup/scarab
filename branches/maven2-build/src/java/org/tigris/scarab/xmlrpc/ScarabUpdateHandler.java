package org.tigris.scarab.xmlrpc;

import org.apache.log4j.Logger;

import java.util.Vector;

import org.apache.torque.TorqueException;

public class ScarabUpdateHandler {
    /**
     * Logger for this class
     */
    private static final Logger log = Logger
            .getLogger(ScarabUpdateHandler.class);
    
    
    public Vector addAttributeOption(String attr, String opt)
    {
        ScarabUpdateHelper helper = new ScarabUpdateHelper();
        
        Vector rslt =  new Vector();
        
        try {
            helper.addAttributeOption(attr, opt);
            helper.mapAttributeOptionToAllModuleIssueTypes(attr, opt);
            rslt.add(new Boolean(true));
            rslt.add("OK");
        } catch (Exception e) {
            log.error("addAttributeOption(String, String) -  : attr=" + attr 
                    + ", opt=" + opt + ", rslt=" + rslt, e);

            rslt.add(new Boolean(false));
            rslt.add("Exception : " + e);
        }
        return rslt;
    }

    public Vector updateAttributeOptions(String attr, Vector options)
    {
        ScarabUpdateHelper helper = new ScarabUpdateHelper();
        
        Vector rslt =  new Vector();
        
        try {
            helper.updateAttributeOptions(attr, options);
            rslt.add(new Boolean(true));
            rslt.add("OK");
        } catch (Exception e) {
            log.error("updateAttributeOption(String, Vector) -  : attr=" + attr 
                    + ", options=" + options + ", rslt=" + rslt, e);
            
            rslt.add(new Boolean(false));
            rslt.add("Exception : " + e);
        }
        return rslt;
    }
    
    public Vector sortAttributeOptions(String attr)
    {
        ScarabUpdateHelper helper = new ScarabUpdateHelper();
        
        Vector rslt =  new Vector();
        
        try {
            helper.sortAttributeOptions(attr);
            rslt.add(new Boolean(true));
            rslt.add("OK");
        } catch (Exception e) {
            log.error("updateAttributeOption(String, Vector) -  : attr=" + attr 
                    + ", rslt=" + rslt, e);
            
            rslt.add(new Boolean(false));
            rslt.add("Exception : " + e);
        }
        return rslt;
    }

}
