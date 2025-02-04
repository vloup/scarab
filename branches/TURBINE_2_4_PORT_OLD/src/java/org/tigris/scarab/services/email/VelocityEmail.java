package org.tigris.scarab.services.email;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.OutputStream;
import java.io.Writer;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.TurbineException;
import org.apache.velocity.context.Context;

/**
 * This is a simple static accessor to common Velocity tasks such as
 * getting an instance of a context as well as handling a request for
 * processing a template.
 * <pre>
 * Context context = TurbineVelocity.getContext(data);
 * context.put("message", "Hello from Turbine!");
 * String results = TurbineVelocity.handleRequest(context, "helloWorld.vm");
 * data.getPage().getBody().addElement(results);
 * </pre>
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:jvanzyl@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:stack@collab.net">Michael Stack</a>
 * @version $Id$
 */
public abstract class VelocityEmail
{
    /**
     * Utility method for accessing the service
     * implementation
     *
     * @return a VelocityEmailService implementation instance
     */
    protected static VelocityEmailService getService()
    {
        return (VelocityEmailService)TurbineServices
            .getInstance().getService(EmailService.SERVICE_NAME);
    }

    /**
     * This allows you to pass in a context and a path to a template
     * file and then grabs an instance of the velocity service and
     * processes the template and returns the results as a String
     * object.
     *
     * @param context A Context.
     * @param template The path to the template file.
     * @return The processed template.
     * @exception Exception Error processing template.
     */
    public static String handleRequest(Context context, String template)
        throws Exception
    {
        return getService().handleRequest(context, template);
    }

    /**
     * @see org.tigris.scarab.services.email.VelocityEmailService#handleRequest(Context,
     * String, String, String)
     */
    public String handleRequest(Context context, String template,
                                String charset, String encoding)
        throws Exception
    {
        return getService().handleRequest(context, template, charset,
                                          encoding);
    }

    /**
     * Process the request and fill in the template with the values
     * you set in the Context.
     *
     * @param context A Context.
     * @param filename A String with the filename of the template.
     * @param out A OutputStream where we will write the process template as
     * a String.
     *
     * @exception Exception Error processing template.
     *
     * @see org.tigris.scarab.services.email.VelocityEmailService#handleRequest(Context,
     * String, OutputStream)
     */
    public static void handleRequest(Context context, String template,
                                     OutputStream out)
        throws Exception
    {
        getService().handleRequest(context, template, out);
    }

    /**
     * Process the request and fill in the template with the values
     * you set in the Context.
     *
     * @param context A Context.
     * @param template The path to the template file.
     * @param out A OutputStream where we will write the process template as
     * a String.
     * @param charset The character set to use when writing the result.
     * @param encoding The encoding to use when merging context and template.
     *
     * @exception Exception Error processing template.
     *
     * @see org.tigris.scarab.services.email.VelocityEmailService#handleRequest(Context,
     * String, OutputStream)
     */
    public static void handleRequest(Context context, String template,
                                     OutputStream out, String charset,
                                     String encoding)
        throws Exception
    {
        getService().handleRequest(context, template, out, charset, encoding);
    }

    /**
     * @see org.tigris.scarab.services.email.VelocityEmailService#handleRequest(Context,
     * String, Writer)
     */
    public static void handleRequest(Context context, String filename, 
                                     Writer writer)
        throws TurbineException
    {
        getService().handleRequest(context, filename, writer, null);
    }

    /**
     * @see org.tigris.scarab.services.email.VelocityEmailService#handleRequest(Context,
     * String, Writer, String)
     */
    public static void handleRequest(Context context, String filename,
                                     Writer writer, String encoding)
        throws TurbineException
    {
        getService().handleRequest(context, filename, writer, encoding);
    }
}
