/**
 * 
 */
package org.tigris.scarab.xmlrpc;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.test.BaseScarabTestCase;
import org.tigris.scarab.util.ScarabException;

import junit.framework.TestCase;

/**
 * @author pti
 *
 */
public class NewTicketHandlerTest extends BaseScarabTestCase {

	NewTicketHandler nth;
	SimpleHandler sh;
	private String module;
	private String user;
	private Hashtable attribs;
	private String issueType;
	private Integer statusId;
	private Integer newId;
	
	/**
	 * @param arg0
	 * @throws Exception 
	 */
	public NewTicketHandlerTest(String arg0) throws Exception {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		nth = new NewTicketHandler();
		sh = new SimpleHandler();
		
		module = "PAC";
		issueType = "Defect";
		user = "jon@latchkey.com";
		attribs = new Hashtable();
		statusId = new Integer(3);
		newId = new Integer(2);
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNewTicketTest() throws ScarabException, TorqueException {
		String ticketId = nth.createNewTicket(module, issueType, user, attribs);
		assertNotNull("TicketId should not be null", ticketId);
		assertTrue(ticketId.matches("^PAC[0-9]+"));
		Issue issue = IssueManager.getIssueById(ticketId);
		assertNotNull(issue);
	}
	
	public void testNewTicketChangeAttributeOption() throws Exception {
		
		// Known attributes are mapped to their text representation
		attribs.put("Status","New");
		attribs.put("Description","Some interesting description.");
		attribs.put("AssignedTo", user);
		
		// unknown attributes are silently discarded.
		attribs.put("Brol", user);
		String ticketId = nth.createNewTicket(module, issueType, user, attribs);
	    
		Issue issue = IssueManager.getIssueById(ticketId);
		
		AttributeValue attributeValue;
		attributeValue = issue.getAttributeValue("Status");
		assertEquals("New",attributeValue.getValue());
		attributeValue = issue.getAttributeValue("Description");
		assertEquals("Some interesting description.",attributeValue.getValue());
		attributeValue = issue.getAttributeValue("AssignedTo");
		assertEquals(user,attributeValue.getValue());
		
		attributeValue = issue.getAttributeValue("Brol");
		assertNull(attributeValue);
		
	}
}
