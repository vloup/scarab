package org.tigris.scarab;

import org.tigris.scarab.feeds.IssueFeedTest;
import org.tigris.scarab.feeds.QueryFeedTest;
import org.tigris.scarab.util.xmlissues.ImportIssuesTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllFailingScarabTests
{

    static public Test suite() {
        TestSuite suite = new TestSuite(AllFailingScarabTests.class.getCanonicalName());
        
        suite.addTestSuite(ImportIssuesTest.class);
        suite.addTestSuite(IssueFeedTest.class);
        suite.addTestSuite(QueryFeedTest.class);

        return suite;
    }
}
