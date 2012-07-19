package org.tigris.scarab.util;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

/**
 * XML Transformer.
 * @author jhoechstaedter
 *
 */
public class XSLTransformer{
	
	/**
	 * XML transformation by XSLT style sheet.
	 * @param in : Instance of InputStream
	 * @param out : Instance of OutputStream
	 * @param xslPath : Transformation style sheet.
	 * @throws TransformerException
	 */
	public void transform(InputStream in, OutputStream out, String xslPath) throws TransformerException{
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		
	    Transformer transformer =
	      tFactory.newTransformer
	         (new javax.xml.transform.stream.StreamSource
	            (xslPath));
	
	    transformer.transform
	      (new javax.xml.transform.stream.StreamSource
	            (in),
	       new javax.xml.transform.stream.StreamResult
	            (out));		
		
	}
	
}
