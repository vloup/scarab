package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 *
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 *
 * 5. Products derived from this software may not use the "Tigris" or
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 * prior written permission of CollabNet.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */

import java.util.Arrays;
import java.util.List;
import java.io.Serializable;

import org.apache.torque.om.Persistent;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.Turbine;


/**
 * This class manages GlobalParameter objects.  Global is used a bit
 * loosely here.  Parameters can be module scoped as well.  for example,
 * the email parameters have a global set which is the default, if the
 * module does not provide alternatives.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:ronny.voelker@laxy.com">Ronny Völker</a>
 * @version $Id$
 */
public class GlobalParameterManager
    extends BaseGlobalParameterManager
{
    private static final String MANAGER_KEY = DEFAULT_MANAGER_CLASS;
    private static final String GET_STRING  = "getString";

    /**
     * Creates a new <code>GlobalParameterManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public GlobalParameterManager()
        throws TorqueException
    {
        super();
        setRegion(getClassName().replace('.', '_'));
    }

    private static boolean toBoolean(String booleanValue)
    {
    	return(
    	       booleanValue.equalsIgnoreCase("T") 
    	    || booleanValue.equalsIgnoreCase("TRUE")
        );
    }
    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        GlobalParameter gp = (GlobalParameter)om;
        Serializable moduleId = gp.getModuleId();
        String name = gp.getName();
        if (moduleId == null)
        {
            // if altering a global parameter, its possible the
            // module overrides are invalid.
            getMethodResult().removeAll(MANAGER_KEY, name);
        }
        else
        {
            getMethodResult().remove(MANAGER_KEY, name, GET_STRING,
                                     moduleId);
        }
        return oldOm;
    }

    private static GlobalParameter getInstance(String name)
        throws TorqueException
    {
        return getInstance(name, null);
    }

    private static GlobalParameter getInstance(String name, Module module)
        throws TorqueException
    {
        GlobalParameter result = null;
        Criteria crit = new Criteria();
        crit.add(GlobalParameterPeer.NAME, name);
        if (module == null)
        {
            crit.add(GlobalParameterPeer.MODULE_ID, null);
        }
        else
        {
            crit.add(GlobalParameterPeer.MODULE_ID, module.getModuleId());
        }
        List parameters = GlobalParameterPeer.doSelect(crit);
        if (!parameters.isEmpty())
        {
            result = (GlobalParameter)parameters.get(0);
        }
        return result;
    }

    private static String getStringModule(String name, Module module )
        throws TorqueException
    {
        String value = (String) getMethodResult()
                .get(MANAGER_KEY, name, GET_STRING, module.getModuleId());

        if(value == null)
        {
            GlobalParameter p = getInstance(name, module);

            if (p != null)
            {
                value = p.getValue();
            }
            if(value==null)
            {
                value = "";
            }
            getMethodResult().put(value, MANAGER_KEY, name, GET_STRING, module.getModuleId());
        }
        return value;
    }

    private static String getStringGlobal(String name)
        throws TorqueException
    {
        String value = (String) getMethodResult()
                .get(MANAGER_KEY, name, GET_STRING );

        if(value == null)
        {
            GlobalParameter p = getInstance(name);

            if (p != null)
            {
                value = p.getValue();
            }
            if(value==null || value.equals(""))
            {
                value = Turbine.getConfiguration().getString(name);
            }
            if(value==null)
            {
                value = "";
            }
            getMethodResult().put(value, MANAGER_KEY, name, GET_STRING);
        }
        return value;
    }

    /**
     * return the global value of parameter <name>
     *
     * @param name
     * @return "" if the parameter is not defined
     */
    public static String getString(String name)
        throws TorqueException
    {
        return getStringGlobal(name);
    }

    /**
     * return the value of parameter <name> for the module <module>
     * if it's not defined return the global value of this parameter
     *
     * @param name
     * @param module
     * @return "" if the parameter is not defined
     */
    public static String getString(String name, Module module)
        throws TorqueException
    {
        String value = getStringModule(name, module);
        if (value.equals(""))
        {
            value = getStringGlobal(name);
        }
        return value;
    }

    /**
     * Recursively look up for the existence of the name in the
     * module hierarchy. Backtrack towards the module root.
     * If no value was found, check for the existence of a
     * module-independent global parameter.
     * If still no value found, check for the Turbine
     * configuration property with the same name.
     * If still no definition found, return the parameter
     * "def" instead.
     *
     * @param name
     * @param module
     * @param def
     * @return
     */
    public static String getStringFromHierarchy(String name, Module module, String def)
        throws TorqueException
    {
      String value = getStringModule( name, module );

      if(value.equals(""))
      {
          Module parentModule = module.getParent();
         if(parentModule != null)
         {
             value = getStringFromHierarchy(name, parentModule, def);
         }
      }

        if(value.equals(""))
        {
            value = getStringGlobal(name);
        }

        if(value.equals(""))
        {
            value = def;
        }

        return value;
    }

    public static void setString(String name, String value)
        throws TorqueException
    {
      setString(name, null, value);
    }

    public static void setString(String name, Module module, String value)
        throws TorqueException
    {
        GlobalParameter p = getInstance(name, module);
        if (p == null)
        {
            p = getInstance();
            p.setName(name);
            p.setModuleId(module.getModuleId());
        }
        p.setValue(value);
        p.save();
    }

    /**
     * return the global value of parameter <name>
     *
     * @param name
     * @return "" if the parameter is not defined
     */
    public static boolean getBoolean(String name)
        throws TorqueException
    {
        return toBoolean(getString(name));
    }

    /**
     * return the value of parameter <name> for the module <module>
     * if it's not defined return the global value of this parameter
     *
     * @param name
     * @param module
     * @return "" if the parameter is not defined
     */
    public static boolean getBoolean(String name, Module module)
        throws TorqueException
    {
        return toBoolean(getString(name, module));
    }

    /**
     * Recursively look up for the existence of the name.
     * Further details, @see #getBooleanFromHierarchy(String name, Module module, boolean def)
     *
     * If no value was not found, return "def" instead.
     *
     * @param name
     * @param module
     * @param def
     * @return
     */
    public static boolean getBooleanFromHierarchy(String name, Module module, boolean def)
        throws TorqueException
    {
        String defAsString = (def)? "T":"F";
        return toBoolean(getStringFromHierarchy(name, module, defAsString ));
    }


    public static void setBoolean(String name, boolean value)
        throws TorqueException
    {
        setString(name, (value ? "T" : "F"));
    }

    public static void setBoolean(String name, Module module, boolean value)
        throws TorqueException
    {
        setString(name, module, (value ? "T" : "F"));
    }

    /**
     * return the global value of parameter <name>
     *
     * @param name
     * @return Empty List if the parameter is not defined
     */
    public static List getStringList(String name)
        throws TorqueException
    {
        return toStringList(getString(name));
    }

    private static List toStringList(String string)
    {
        return Arrays.asList(string.split(";"));
    }
}
