package org.tigris.scarab.workflow.validations;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.tigris.scarab.workflow.validations.WorkflowValidation.ParameterDescription;

/**
 * This is an simple validation to be used with scarab,
 * it just checks to see that there are objects passed in.
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public class VelocityValidation extends AbstractValidation
{

    private static String USAGE                     = "Workflow_VelocityValidation_Usage";

    private static String VTL_TEXT_PARAMETER        = "VTLText";
    private static String VTL_TEXT_PARAMETER_DESC   = "Workflow_VelocityValidation_ScriptText";
    private static String VALIDATION_LOG_TAG        = "VelocityValidation";

    private static VelocityEngine VALIDATION_ENGINE = new VelocityEngine();

    /**
     * takes a collection of parameters,
     * returns empty string for success
     * returns an error message for failure
     * throws no exceptions, returns error message instead
     * I have doubts about the thread safety of the velocity engine so this is synchronized
     * 
     * @see org.tigris.scarab.workflow.validations.WorkflowValidation#doValidation(Map, Map, Map)
     */
    public synchronized String doValidation(Map parameters, Map objects, Map validationContext)
    {

        ValidationResult result = new ValidationResult("DEFAULT");

        try
        {
            VALIDATION_ENGINE.init();
        }
        catch (Exception e)
        {
            //not sure yet what to do here
            result.setResult("Unable to process validation using Velocity engine.");
            return result.toString();
        }

        //get the VTL text to process
        String vtlText = (String) parameters.get(VTL_TEXT_PARAMETER);
        StringReader vtlReader = new StringReader(vtlText);
        StringWriter outputWriter = new StringWriter();

        //set the context
        VelocityContext velocityContext = new VelocityContext();

        Iterator keys = objects.keySet().iterator();

        while (keys.hasNext())
        {
            String keyString = (String)keys.next();
            velocityContext.put(keyString, objects.get(keyString));
        }

        velocityContext.put(VALIDATION_CONTEXT, validationContext);

        //put in the result string, which should get defined in the validation script.
        velocityContext.put(VALIDATION_RESULT, result);

        try
        {
            VALIDATION_ENGINE.evaluate(velocityContext, outputWriter, VALIDATION_LOG_TAG, vtlReader );
//            System.out.println("velocity output:" + outputWriter.toString());
        }
        catch(Exception e)
        {
            result.setResult("Error in processing validation: " + e);
//            System.out.println("velocity output:" + outputWriter.toString());
        }

        //now evaluate the template
        return result.toString();
    }

    //get list of parameters required/used by this validation
    public List getParameterList()
    {
        List params = new ArrayList();
        params.add(new ParameterDescription(VTL_TEXT_PARAMETER,VTL_TEXT_PARAMETER_DESC,80,20));
        return params;
    }

    public String getUsage()
    {
        return USAGE;
    }
}
