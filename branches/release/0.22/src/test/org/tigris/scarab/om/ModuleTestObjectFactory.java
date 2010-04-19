package org.tigris.scarab.om;

import org.apache.torque.om.NumberKey;

public class ModuleTestObjectFactory
{

    private Module module;

    /**
     * If something like an Issue needs a mapping to a Module, then this is
     * Module #5 that you can use. For example, you should call:
     * issue.setModule(getModule()) in your Test before you use any of the rest
     * of the methods on the Issue object.
     */
    public Module getModule()
        throws Exception
    {
        if(module==null)
            module = ModuleManager.getInstance(new NumberKey(1005), false);
        return module;
    }

}
