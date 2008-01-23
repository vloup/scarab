package org.tigris.scarab.om;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllOmTests
{
    static public Test suite()
    {
        TestSuite suite = new TestSuite(AllOmTests.class.getName());
        
        suite.addTestSuite(ActivitySetTest.class);
        suite.addTestSuite(ActivityTest.class);
        suite.addTestSuite(AttachmentTest.class);
        suite.addTestSuite(AttributeGroupTest.class);
        suite.addTestSuite(AttributeOptionTest.class);
        suite.addTestSuite(AttributeValueTest.class);
        suite.addTestSuite(IssueTest.class);
        suite.addTestSuite(IssueTypeTest.class);
        suite.addTestSuite(QueryTest.class);
        suite.addTestSuite(RModuleAttributeTest.class);
        suite.addTestSuite(RModuleIssueTypeTest.class);
        suite.addTestSuite(RModuleOptionTest.class);
        suite.addTestSuite(ScarabModuleTest.class);
        suite.addTestSuite(ScarabUserTest.class);
        
        return suite;
    }
}
