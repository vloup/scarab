/*
 * Created on Mar 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigris.scarab.services.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.fulcrum.InitializationException;

/**
 * @author pti
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JasperReportServiceImpl extends BaseReportService {

	private String applicationRoot;
	/**
	 * 
	 */
	public JasperReportServiceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.fulcrum.Service#init()
	 */
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		super.initialize();
		if (getLogger().isInfoEnabled()) {
			getLogger().info("JasperReportService is intialised.");
			
		}
	}

    public void contextualize(Context context) throws ContextException {
        this.applicationRoot = context.get( "urn:avalon:home" ).toString();
    }


	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#generateReport(java.lang.String, java.lang.String, java.util.Map, java.lang.Object)
	 */
	public byte[] generateReport(String type, String reportName,
			Map params, Object recordSource) {
		
		if (params == null) 
			params = new HashMap();
					
		byte[] bytes = null;

		try
		{
			File reportFile = new File(applicationRoot + "/reports/" + reportName + ".jasper");

			params.put("BaseDir", reportFile.getParentFile());
			
			JRDataSource ds = getDataSource(recordSource);
			StringBuffer sbuffer = new StringBuffer();

			JasperReport jasperReport = (JasperReport)JRLoader.loadObject(reportFile);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, ds);

			JRExporter exporter = null;
			
			// create an exported for the desired type and define type specific
			// parameters
			if (type == PDF) {
				exporter = new JRPdfExporter();
			} else if (type == HTML) {
				exporter = new JRHtmlExporter(); 
			} else if (type == XML) {
				exporter = new JRXmlExporter(); 
			} else if (type == CSV) {
				exporter = new JRCsvExporter(); 
			} else if (type == TSV) {
				exporter = new JRCsvExporter();
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER,"\t");
			} else if (type == XLS) {
				exporter = new JRXlsExporter(); 
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STRING_BUFFER, sbuffer);
			
			exporter.exportReport();

			return sbuffer.toString().getBytes();

		}
		catch (JRException e)
		{
			getLogger().error("Unable to generate report.",e);			
//		} catch (FileNotFoundException e) {
//			getLogger().error("Unable to find report definition file.",e);
		}

		if ((bytes == null) || (bytes.length == 0))
		{
			getLogger().error("Report returned invalid data.");
			bytes = null;
		}

		return bytes;
	}
	/**
	 * Convert the input data to a form recognizable by JasperReports
	 * 
	 * @param recordSource
	 * @return JRDataSource
	 */
	private JRDataSource getDataSource(Object recordSource) {
		JRDataSource rslt = null;
		if (recordSource == null) {
			getLogger().warn("RecordSource was null, returning empty dataset.");
			rslt = new JREmptyDataSource();
		} if (recordSource instanceof Object[]) {
			Object[] oa = (Object[]) recordSource;
			if (oa[0] instanceof Map) {
				rslt = new JRMapArrayDataSource(oa);
			} else {
				rslt = new JRBeanArrayDataSource(oa);
			}
		} if (recordSource instanceof Collection) {
			Collection c = (Collection) recordSource;
			// TODO: (pti) this might cause harm to collections which can only be 
			// iterated once.
			if (c.iterator().next() instanceof Map) {
				rslt = new JRMapCollectionDataSource(c);
			} else {
				rslt = new JRBeanCollectionDataSource(c);
			}
		} else {
			getLogger().error("Unsupported recordSource type : " + recordSource.getClass());
		}
			
		return rslt;
	}

	/* (non-Javadoc)
	 * @see org.tigris.scarab.services.reports.ReportService#supportsReportType(java.lang.String)
	 */
	public boolean supportsReportType(String type) {
		// TODO Auto-generated method stub
		if ((type == PDF) 
		     || (type == HTML)
		     || (type == XML)
		     || (type == CSV)
		     || (type == TSV)
		     || (type == XLS)
			 ) {
			return true;
		}
		return false;
	}
}
