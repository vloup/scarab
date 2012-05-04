/**
 * 
 */
package org.tigris.scarab.xmlrpc;

import java.util.Hashtable;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.test.BaseTurbineTestCase;
import org.tigris.scarab.util.ScarabException;

/**
 * @author pti
 *
 */
public class NewTicketHandlerTest extends BaseTurbineTestCase {

	NewTicketHandler nth;
	SimpleHandler sh;
	private String module;
	private String user;
	private Hashtable attribs;
	private String issueType;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		nth = new NewTicketHandler();
		sh = new SimpleHandler();
		
		module = "PAC";
		issueType = "Defect";
		user = "jon@foobarack.com";
		attribs = new Hashtable();
		
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
