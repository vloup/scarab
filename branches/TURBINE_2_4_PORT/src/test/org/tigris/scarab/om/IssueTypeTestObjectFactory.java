package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

public class IssueTypeTestObjectFactory
{
    private static IssueType defaultIssueType = null;

    public IssueType getDefaultIssueType()
        throws Exception
    {
        if(defaultIssueType==null)
            defaultIssueType = IssueTypeManager
            .getInstance(new NumberKey(1), false);
        return defaultIssueType;
    }

}
