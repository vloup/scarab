package org.tigris.scarab.cactus;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebRequest;


/**
 * 
 * Sample test, just to test the tests ;-)
 *
 */
public class TestSampleServlet extends ServletTestCase
{
    public TestSampleServlet(String theName)
    {
        super(theName);
    }

    public static Test suite()
    {
        return new TestSuite(TestSampleServlet.class);
    }

    public void beginSaveToSessionOK(WebRequest webRequest)
    {
        webRequest.addParameter("testparam", "it works!");
    }

    public void testSaveToSessionOK()
    {
        SampleServlet servlet = new SampleServlet();
        servlet.saveToSession(request);
        assertEquals("it works!", session.getAttribute("testAttribute"));
    }
}
