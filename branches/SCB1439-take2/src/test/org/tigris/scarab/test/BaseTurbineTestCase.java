package org.tigris.scarab.test;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.fulcrum.TurbineServices;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConfig;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.TurbineXmlConfig;
/**
 * Test case that just starts up Turbine.  All Scarab specific
 * logic needs to be implemented in your own test cases.
 * 
 * @author     <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class BaseTurbineTestCase extends TestCase {
	private static final String SCARAB_APPLICATION_ROOT = "target/scarab";

    private static TurbineConfig tc = null;
	
	private static boolean initialized = false;

	public BaseTurbineTestCase() {
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		if (!initialized) {
			initTurbine();
			initialized=true;
		}
	}
	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (Turbine.getConfiguration() != null) {	
			// stopping turbine on each test iteration has a major performance impact
			// checking for this parameter allows this to be skipped for a fast run of
			// all tests in a single turbine instance.
			boolean stop = Turbine.getConfiguration().getBoolean("unittest.teardown.shutdownServices", true);
			
			if (stop) { 
				TurbineServices.getInstance().shutdownServices();
				initialized=false;
			}
		}
	}

	private void initTurbine() throws Exception {
        Map params = new HashMap();
        params.put(TurbineXmlConfig.CONFIGURATION_PATH_KEY,"../../src/test/TestTurbineConfiguration.xml");
        params.put(TurbineConstants.APPLICATION_ROOT,SCARAB_APPLICATION_ROOT);
        tc = new TurbineXmlConfig(SCARAB_APPLICATION_ROOT,params);
		tc.init();
	}

	
}
