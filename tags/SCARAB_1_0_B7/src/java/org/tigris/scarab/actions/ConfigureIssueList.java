package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
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
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.ComboKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.util.Criteria;
import org.apache.turbine.tool.IntakeTool;
import org.apache.turbine.ParameterParser;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.RModuleUserAttributeManager;
import org.tigris.scarab.om.RModuleUserAttributePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
    This class is responsible for the user configuration of the issue list.
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id$
*/
public class ConfigureIssueList extends RequireLoginFirstAction
{

    public void doSave( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);

        ScarabRequestTool scarab = getScarabRequestTool(context);
        Module module = scarab.getCurrentModule();
        IssueType issueType = scarab.getCurrentIssueType();
        NumberKey moduleId = module.getModuleId();
        ScarabUser user = (ScarabUser)data.getUser();

        RModuleUserAttribute mua = null;

        // Delete current attribute selections for user
        Criteria crit = new Criteria();
        crit.add(RModuleUserAttributePeer.USER_ID, user.getUserId());
        List currentAttributes = RModuleUserAttributePeer.doSelect(crit);
        for (int i =0; i<currentAttributes.size(); i++)
        {
            try
            {
                SimpleKey key1 = moduleId;
                SimpleKey key2 = user.getUserId();
                SimpleKey key3 = issueType.getIssueTypeId();
                SimpleKey key4 = 
                    ((RModuleUserAttribute)currentAttributes.get(i))
                    .getAttributeId();
                SimpleKey[] key = {key1, key2, key3, key4};
                mua = RModuleUserAttributeManager
                       .getInstance(new ComboKey(key), false);
                mua.delete(user);
            }
            catch (Exception e)
            {
                // ignored
            }
        }

        // Add user's new selection of attributes
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        for (int i =0; i<keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("selected_"))
            {
                NumberKey attributeId =  new NumberKey(key.substring(9));
                Attribute attribute = AttributeManager
                    .getInstance(attributeId);

                mua = user.getRModuleUserAttribute(module, 
                                                   attribute, issueType);
                Group group = intake.get("RModuleUserAttribute", 
                                         mua.getQueryKey(), false);

                Field order = group.get("Order");
                order.setProperty(mua);
                mua.save();
            }
        }
        data.setMessage(DEFAULT_MSG);
    }

    /**
        Resets back to default values for module.
    */
    public void doUsedefaults( RunData data, TemplateContext context ) 
        throws Exception
    {
        data.getParameters().add("usedefaults", "true"); 
    }
        

}
