package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

public class ScarabUserTestObjectFactory
{

    private ScarabUser user0 = null;
    private ScarabUser user1 = null;
    private ScarabUser user2 = null;

    public ScarabUser getUser1() throws Exception
    {
        if (user1 == null)
        {
            user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
        }
        return user1;
    }

    public ScarabUser getUser2() throws Exception
    {
        if (user2 == null)
        {
            user2 = ScarabUserManager.getInstance(new NumberKey(2), false);

        }
        return user2;
    }

    public ScarabUser getUser5() throws Exception
    {
        if (user0 == null)
        {
            user0 = ScarabUserManager.getInstance(new NumberKey(5), false);
        }
        return user0;
    }

}
