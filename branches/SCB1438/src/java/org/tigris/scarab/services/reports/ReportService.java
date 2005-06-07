/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.services.reports;

import java.util.Map;

import org.apache.fulcrum.Service;

/**
 * @author pti
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ReportService {

    public static final String PDF = "application/pdf";
    public static final String HTML = "text/html";
    public static final String XML = "text/xml";
    public static final String CSV = "text/comma-separated-values";
    public static final String TSV = "text/tab-separated-values";
    public static final String XLS = "application/excel";
	
    public byte[] generateReport(String type, String reportName, Map params, 
							    Object recordSource);
    public byte[] generatePDFReport(String reportName, Map params,
			                    Object recordSource);
    public byte[] generateHTMLReport(String reportName, Map params,
    Object recordSource);
    public byte[] generateXMLReport(String reportName, Map params,
    Object recordSource);
    public byte[] generateCSVReport(String reportName, Map params,
    Object recordSource);
    public byte[] generateTSVReport(String reportName, Map params,
    Object recordSource);
    public byte[] generateXLSReport(String reportName, Map params,
    Object recordSource);
	
    public boolean supportsReportType(String type);
    public boolean supportsPDFReportType();
    public boolean supportsHTMLReportType();
    public boolean supportsXMLReportType();
    public boolean supportsCSVReportType();
    public boolean supportsTSVReportType();
    public boolean supportsXLSReportType();
	
}
