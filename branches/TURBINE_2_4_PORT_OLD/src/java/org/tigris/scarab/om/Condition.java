
package org.tigris.scarab.om;


import org.apache.torque.om.Persistent;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class Condition
    extends org.tigris.scarab.om.BaseCondition
    implements Persistent
{
    public boolean equals(Condition cond)
    {
        boolean bRdo = false;
        Integer attr1 = this.getAttributeId();
        Integer attr2 = cond.getAttributeId();
        if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                attr1 == attr2)
        {
            attr1 = this.getOptionId();
            attr2 = cond.getOptionId();
            if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                    attr1 == attr2)
            {
                attr1 = this.getModuleId();
                attr2 = cond.getModuleId();
                if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                        attr1 == attr2)
                {
                    attr1 = this.getIssueTypeId();
                    attr2 = cond.getIssueTypeId();
                    if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                            attr1 == attr2)
                    {
                        bRdo = true;
                    }
                }
            }
        }
        return bRdo;
    }
    
    public boolean equals(Object obj)
    {
        return this.equals((Condition)obj);
    }
}
