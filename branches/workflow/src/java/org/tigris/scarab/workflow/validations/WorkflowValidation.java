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

import java.util.List;
import java.util.Map;

/**
 * Interface for validations to be performed when transitioning
 * during workflow from one state to another.
 *
 * @author <a href="mailto:akuklewicz@yahoo.com">Andrew Kuklewicz</a>
 * @version $Id$
 */
public interface WorkflowValidation
{
    /**
     * takes a collection of arguments to specify behaviour, to some degree
     * and a map of objects to work on and use
     * returns empty string for success
     * returns an error message for failure
     * throws no exceptions (intentionally), returns error message instead
     */
    public String doValidation(Map parameters, Map objects, Map context);

    //return instructions on how to use the validation as a list of strings
    public List getUsage();

    //get list of parameters required/used by this validation
    public List getParameterList();

    //get list of objects expected by this validation
    public List getObjectList();

    //check to see if arguments will cause an error
    public String checkArguments(Map arguments);

    public class ParameterDescription
    {

        public static String TEXT_TYPE = "text";
        public static String TEXTAREA_TYPE = "textarea";
        public static String SELECT_TYPE = "select";

        public static int DEFAULT_HEIGHT = 1;
        public static int DEFAULT_WIDTH  = 20;

        public ParameterDescription(String aName, String aDescription)
        {
             this.type = TEXT_TYPE;
             this.name = aName;
             this.description = aDescription;
             this.width = DEFAULT_WIDTH;
        }

        public ParameterDescription(String aName, String aDescription, int aWidth)
        {
             this.type = TEXT_TYPE;
             this.name = aName;
             this.description = aDescription;
             this.width = aWidth;
        }

        public ParameterDescription(String aName, String aDescription, int aWidth, int aHeight)
        {
             this.type = TEXTAREA_TYPE;
             this.name = aName;
             this.description = aDescription;
             this.width = aWidth;
             this.height = aHeight;
        }

        public ParameterDescription(String aName, String aDescription, List aOptions)
        {
             this.type = SELECT_TYPE;
             this.name = aName;
             this.description = aDescription;
             this.options = aOptions;
        }

        public String getName()
        {
            return this.name;
        }

        public void setName(String aName)
        {
            this.name = aName;
        }

        public String getDescription()
        {
            return this.description;
        }

        public void setDescription(String aDescription)
        {
            this.description = aDescription;
        }

        public String getType()
        {
            return this.type;
        }

        public void setType(String aType)
        {
            this.type = aType;
        }

        public List getOptions()
        {
            return this.options;
        }

        public void setOptions(List aOptions)
        {
            this.options = aOptions;
        }

        public int getWidth()
        {
            return this.width;
        }

        public void setWidth(int aWidth)
        {
            this.width = aWidth;
        }

        public int getHeight()
        {
            return this.height;
        }

        public void setHeight(int aHeight)
        {
            this.height = aHeight;
        }

        private String name;
        private String description;
        private String type;
        private List options;
        private int width;
        private int height;

    }
}
