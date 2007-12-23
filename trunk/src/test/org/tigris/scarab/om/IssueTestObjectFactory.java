package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

public class IssueTestObjectFactory
{
    private Issue issue0 = null;

    public Issue getIssue0() throws Exception
    {
        if (issue0 == null)
        {
            issue0 = IssueManager.getInstance(new NumberKey(1), false);
        }
        return issue0;
    }

}
