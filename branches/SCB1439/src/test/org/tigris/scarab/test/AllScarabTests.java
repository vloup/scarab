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

import junit.framework.TestSuite;

import org.tigris.scarab.SecurityTest;
import org.tigris.scarab.StartingTorqueTest;
import org.tigris.scarab.StartingTurbineTest;
import org.tigris.scarab.actions.RegisterTest;
import org.tigris.scarab.da.AttributeAccessTest;
import org.tigris.scarab.om.ActivitySetTest;
import org.tigris.scarab.om.ActivityTest;
import org.tigris.scarab.om.AttachmentTest;
import org.tigris.scarab.om.AttributeTest;
import org.tigris.scarab.om.IssueTest;
import org.tigris.scarab.om.IssueTypeTest;
import org.tigris.scarab.om.QueryTest;
import org.tigris.scarab.om.RModuleAttributeTest;
import org.tigris.scarab.om.RModuleIssueTypeTest;
import org.tigris.scarab.om.RModuleOptionTest;
import org.tigris.scarab.om.ScarabUserTest;
import org.tigris.scarab.services.email.VelocityEmailServiceTest;
import org.tigris.scarab.services.hsql.HSQLServiceTest;
import org.tigris.scarab.services.yaaficomponent.YaafiComponentServiceTest;
import org.tigris.scarab.util.EmailLinkTest;
import org.tigris.scarab.util.ScarabUtilTest;
import org.tigris.scarab.util.SubsetIteratorTest;
import org.tigris.scarab.util.SubsetIteratorWithSizeTest;
import org.tigris.scarab.util.word.IssueSearchTest;
import org.tigris.scarab.util.xmlissues.ImportIssuesTest;

/**
 * @author pti
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllScarabTests extends BaseScarabOMTestCase {
	static public TestSuite suite() {
		TestSuite suite = new TestSuite("Scarab Tests");
		
		// org.tigris.scarab tests 
		suite.addTestSuite(SecurityTest.class);
		suite.addTestSuite(StartingTorqueTest.class);
		suite.addTestSuite(StartingTurbineTest.class);
		
		// org.tigris.scarab.actions tests 
		suite.addTestSuite(RegisterTest.class);

		// org.tigris.scarab.da tests 
		suite.addTestSuite(AttributeAccessTest.class);

		// org.tigris.scarab.om tests 
        suite.addTestSuite(ActivitySetTest.class);
		suite.addTestSuite(ActivityTest.class);		
		suite.addTestSuite(AttachmentTest.class);        	
        // suite.addTestSuite(AttributeGroupTest.class);
        // suite.addTestSuite(AttributeOptionTest.class);        
		//suite.addTestSuite(AttributeValueTest.class);
        suite.addTestSuite(AttributeTest.class);
		suite.addTestSuite(IssueTest.class);
		suite.addTestSuite(IssueTypeTest.class);
		suite.addTestSuite(QueryTest.class);
		suite.addTestSuite(RModuleAttributeTest.class);
		suite.addTestSuite(RModuleIssueTypeTest.class);
		suite.addTestSuite(RModuleOptionTest.class);
		//suite.addTestSuite(ScarabModuleTest.class);
		suite.addTestSuite(ScarabUserTest.class);
		
		// org.tigris.scarab.services.email tests 
		suite.addTestSuite(VelocityEmailServiceTest.class);
        
        // org.tigris.scarab.services.hsql tests 
        //suite.addTestSuite(HSQLServiceTest.class);
        
        // org.tigris.scarab.services.yaaficomponent tests 
        suite.addTestSuite(YaafiComponentServiceTest.class);        

		// org.tigris.scarab.util
		suite.addTestSuite(EmailLinkTest.class);
		suite.addTestSuite(ScarabUtilTest.class);
		suite.addTestSuite(SubsetIteratorTest.class);
		suite.addTestSuite(SubsetIteratorWithSizeTest.class);
		
		// org.tigris.scarab.util.word
		//suite.addTestSuite(IssueSearchFactoryTest.class);  // Seems to kill thigns
		suite.addTestSuite(IssueSearchTest.class);
		
		// org.tigris.scarab.util.xmlissues
		suite.addTestSuite(ImportIssuesTest.class);
		
		return suite;
	}
}