/*
 * Created on Mar 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.services.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.fulcrum.TurbineServices;
import org.apache.turbine.services.yaaficomponent.YaafiComponentService;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.test.BaseScarabTestCase;

import junit.framework.TestCase;

/**
 * @author pti
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestJasperReportService extends BaseScarabTestCase {

	private JasperReportServiceImpl jrs;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
        
		YaafiComponentService yaafi = (YaafiComponentService)TurbineServices.getInstance().getService(YaafiComponentService.SERVICE_NAME);

		jrs = (JasperReportServiceImpl)yaafi.lookup(ReportService.class.getName());
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for TestJasperReportService.
	 * @param arg0
	 * @throws Exception
	 */
	public TestJasperReportService(String arg0) throws Exception {
		super(arg0);
	}

	public void testGenerateReport() throws Exception {
		Issue issue = getIssue0();
		ArrayList issueList = new ArrayList();
	    issueList.add(issue.getAllAttributeValuesMap());
		byte[] bytes = jrs.generateXMLReport("test/testGenerateReport",new HashMap(), issueList);
		assertNotNull("Report should not return a null bytearray.",bytes);
		String xmlfile = new String(bytes);
		System.out.println(xmlfile);
	}

	public void testSupportsReportType() {
		//TODO Implement supportsReportType().
		assertTrue("Should support PDF", jrs.supportsPDFReportType());
		assertTrue("Should support HTML", jrs.supportsHTMLReportType());
		assertTrue("Should support XML", jrs.supportsXMLReportType());
		assertTrue("Should support CSV", jrs.supportsCSVReportType());
		assertTrue("Should support TSV", jrs.supportsTSVReportType());
		assertTrue("Should support XLS", jrs.supportsXLSReportType());
	}

	public void testContextualize() {
		//TODO Implement contextualize().
	}

}
