package org.tigris.scarab.xmlrpc;

import java.net.URL;
import java.util.Vector;

import org.apache.fulcrum.xmlrpc.DefaultXmlRpcClientComponent;
import org.apache.fulcrum.xmlrpc.XmlRpcClientComponent;

/**
 * This class is just an example of invoking the SimpleHandler class via
 * the XmlRpc service.
 * 
 * @author jorgeuriarte
 * @see org.tigris.scarab.util.SimpleHandler
 *
 */
public class ScarabUpdateHandlerClient
{

    public void testXmlRpc() throws Exception
    {
        XmlRpcClientComponent cli = new DefaultXmlRpcClientComponent();
        Vector v = new Vector();
        Vector o = new Vector();
        v.add("Status");            // Module
        
        o.add("Newer");                  // IssueID
        o.add("Closed");                  // IssueID
        o.add("Killed");                  // IssueID
        o.add("Dunno");                  // IssueID
        v.add(o);
        
        Vector vRdo = (Vector)cli.executeRpc(new URL("http://localhost:12345/dmm"), "dmm.updateAttributeOptions", v);
        System.out.println("The call was successful: " + (vRdo.get(0)));
        System.out.println("The comment was added: " + vRdo.get(1));

        
        v = new Vector();
        v.add("Status");            // Module
        vRdo = (Vector)cli.executeRpc(new URL("http://localhost:12345/dmm"), "dmm.sortAttributeOptions", v);

        System.out.println("The call was successful: " + (vRdo.get(0)));
        System.out.println("The comment was added: " + vRdo.get(1));
    }
    
    public static void main(String args[])
    {
        ScarabUpdateHandlerClient shc = new ScarabUpdateHandlerClient();
        try
        {
            shc.testXmlRpc();
        }
        catch (Exception e)
        {
            System.err.println("Error invoking service: " + e);
        }
    }
} 