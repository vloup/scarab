package org.tigris.scarab.cactus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * SampleServlet to be tested by the tests' test
 * 
 */
public class SampleServlet extends HttpServlet
{
    public void saveToSession(HttpServletRequest request)
    {
    	String testparam = request.getParameter("testparam");
    	request.getSession().setAttribute("testAttribute", testparam);
    }
}
