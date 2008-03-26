package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

public class AttributeTestObjectFactory
{

    private Attribute platformAttribute = null;
    private Attribute voteAttribute = null;
    private Attribute assignAttribute = null;

    public Attribute getPlatformAttribute() throws Exception
    {
        if (platformAttribute == null)
        {
            platformAttribute = AttributeManager.getInstance(new NumberKey(5));
        }
        return platformAttribute;
    }
    
    public Attribute getVoteAttribute() throws Exception
    {
        if (voteAttribute == null)
        {
            voteAttribute = AttributeManager.getInstance(new NumberKey(8));
        }
        return voteAttribute;
    }

    public Attribute getAssignAttribute() throws Exception
    {
        if (assignAttribute == null)
        {
            assignAttribute = AttributeManager.getInstance(new NumberKey(2));
        }
        return assignAttribute;
    }

}
