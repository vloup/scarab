

package org.tigris.scarab.om;

import java.util.List;
import java.util.LinkedList;
import java.io.Serializable;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.manager.CacheListener;
import org.apache.torque.manager.MethodResultCache;

/** 
 * This class manages Module objects.  
 * The skeleton for this class was autogenerated by Torque  * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public class ModuleManager
    extends BaseModuleManager
    implements CacheListener
{
    /**
     * Creates a new <code>ModuleManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ModuleManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    protected Module getInstanceImpl()
    {
        return new ScarabModule();
    }


    /**
     * Notify other managers with relevant CacheEvents.
     */
    protected void registerAsListener()
    {
        RModuleIssueTypeManager.addCacheListener(this);
        RModuleAttributeManager.addCacheListener(this);
        AttributeGroupManager.addCacheListener(this);
        RModuleOptionManager.addCacheListener(this);
    }

    // -------------------------------------------------------------------
    // CacheListener implementation

    public void addedObject(Persistent om)
    {
        if (om instanceof RModuleAttribute)
        {
            RModuleAttribute castom = (RModuleAttribute)om;
            ObjectKey key = castom.getModuleId();
            Serializable obj = (Serializable)cacheGet(key);
            if (obj != null) 
            {
                getMethodResult().removeAll(obj, 
                    AbstractScarabModule.GET_R_MODULE_ATTRIBUTES);
            }
        }
        else if (om instanceof RModuleOption)
        {
            RModuleOption castom = (RModuleOption)om;
            ObjectKey key = castom.getModuleId();
            Serializable obj = (Serializable)cacheGet(key);
            if (obj != null) 
            {
                getMethodResult().removeAll(obj, 
                    AbstractScarabModule.GET_LEAF_R_MODULE_OPTIONS);
            }
        }
        else if (om instanceof RModuleIssueType) 
        {
            RModuleIssueType castom = (RModuleIssueType)om;
            ObjectKey key = castom.getModuleId();
            Serializable obj = (Serializable)cacheGet(key);
            if (obj != null) 
            {
                getMethodResult().remove(obj, 
                    AbstractScarabModule.GET_NAV_ISSUE_TYPES);
            }
        }
        else if (om instanceof AttributeGroup)
        {
            AttributeGroup castom = (AttributeGroup)om;
            ObjectKey key = castom.getModuleId();
            Serializable obj = (Serializable)cacheGet(key);
            if (obj != null) 
            {
                getMethodResult().removeAll(obj, 
                    AbstractScarabModule.GET_ATTRIBUTE_GROUPS);
            }
        }
    }

    public void refreshedObject(Persistent om)
    {
        addedObject(om);
    }

    /** fields which interest us with respect to cache events */
    public List getInterestedFields()
    {
        List interestedCacheFields = new LinkedList();
        interestedCacheFields.add(RModuleOptionPeer.MODULE_ID);
        interestedCacheFields.add(RModuleAttributePeer.MODULE_ID);
        interestedCacheFields.add(RModuleIssueTypePeer.MODULE_ID);
        interestedCacheFields.add(AttributeGroupPeer.MODULE_ID);
        return interestedCacheFields;
    }
}
