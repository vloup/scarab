package org.tigris.scarab.util.word;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllIssueSearchTests
{
    static public Test suite() {
        TestSuite suite = new TestSuite(AllIssueSearchTests.class.getName());
        
        suite.addTestSuite(IssueSearchTest.class);
        suite.addTestSuite(IssueSearchFactoryTest.class);
        return suite;
    }

}
