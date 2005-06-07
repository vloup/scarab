/*
 * Created on Mar 29, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.services.reports;

import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.ServiceBroker;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.yaafi.service.baseservice.BaseServiceImpl;
import org.apache.log4j.Category;

/**
 * @author pti
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BaseReportService  
extends AbstractLogEnabled 
implements ReportService, Configurable, Initializable, Contextualizable {

	/**
	 * 
	 */
	public BaseReportService() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateReport(java.lang.String)
	 */
	public byte[] generateReport(String type, String reportName, Map params, Object recordSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generatePDFReport()
	 */
	public byte[] generatePDFReport(String reportName, Map params, Object recordSource) {
		return generateReport(PDF,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateHTMLReport()
	 */
	public byte[] generateHTMLReport(String reportName, Map params, Object recordSource) {
		return generateReport(HTML,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateXMLReport()
	 */
	public byte[] generateXMLReport(String reportName, Map params, Object recordSource) {
		return generateReport(XML,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateCSVReport()
	 */
	public byte[] generateCSVReport(String reportName, Map params, Object recordSource) {
		return generateReport(CSV,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateTSVReport()
	 */
	public byte[] generateTSVReport(String reportName, Map params, Object recordSource) {
		return generateReport(TSV,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateXLSReport()
	 */
	public byte[] generateXLSReport(String reportName, Map params, Object recordSource) {
		return generateReport(XLS,reportName, params, recordSource);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateReport(java.lang.String)
	 */
	public boolean generateReportType(String type) {
		// BaseReport does not supoort any format
		return false;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#supportsReportType(java.lang.String)
	 */
	public boolean supportsReportType(String type) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsPDFReportType()
	 */
	public boolean supportsPDFReportType() {
		return supportsReportType(PDF);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsHTMLReportType()
	 */
	public boolean supportsHTMLReportType() {
		return supportsReportType(HTML);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsXMLReportType()
	 */
	public boolean supportsXMLReportType() {
		return supportsReportType(XML);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsCSVReportType()
	 */
	public boolean supportsCSVReportType() {
		return supportsReportType(CSV);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsTSVReportType()
	 */
	public boolean supportsTSVReportType() {
		return supportsReportType(TSV);
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportTypeService#supportsXLSReportType()
	 */
	public boolean supportsXLSReportType() {
		return supportsReportType(XLS);
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
	 */
	public void configure(org.apache.avalon.framework.configuration.Configuration arg0) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Initializable#initialize()
	 */
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context arg0) throws ContextException {
		// TODO Auto-generated method stub
		
	}


}
