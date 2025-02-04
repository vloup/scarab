package org.tigris.scarab.tools.localization;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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
 * software developed by CollabNet (http://www.collab.net/)."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" name
 * nor may "Tigris" appear in their names without prior written
 * permission of CollabNet.
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

import org.tigris.scarab.tools.ScarabLocalizationTool;


/**
 * Interface identifying instances capable of being localized
 * using a <code>ScarabLocalizationTool</code> instance.
 * <p>
 * In order to localize throwables, one could reuse the following pattern:
 * <code>
 * public class MyLocalizedThrowable extends MyThrowableClass implements Localizable
 * {
 *   // may be null
 *   private ScarabLocalizationTool localizer;
 *   
 *   // Set the localizer to be used in later calls to {@link #getLocalizedMessage()}
 *   // @param theLocalizer the localizer (may be <code>null</code>)
 *   public void setLocalizer(final ScarabLocalizationTool theLocalizer)
 *    {
 *       localizer = theLocalizer;
 *    }
 *   
 *   // Return the localized message for that throwable, if a localizer
 *   // was defined using {@link #setLocalizer(ScarabLocalizationTool)}
 *   // @return the localized message.
 *   public String getLocalizedMessage()
 *   {   
 *       // we effectively implement an IoC pattern, made necessary by
 *       // the design of the Throwable base class
 *       if (localizer != null)
 *       {
 *          return toString(localizer);
 *       } 
 *       else
 *       {
 *           return super.getLocalizedMessage();
 *      }
 *   }
 * }
 * </code>
 * This should be particularly thought when one subclasses instances from a
 * different framework, because that framework may be or may become localized
 * one day.
 *
 * @version $Id$
 * @author <a href="mailto:dabbous@saxess.com">Hussayn Dabbous</a>
 */

public interface Localizable
{
    /**
     * resolve the instance to the ScarabLocalizationTool.DEFAULT_LOCALE
     * Note: This method should return english messages independent of
     * any l10n settings. If a ScarabLocalizationTool instance is 
     * available, it is preferreable to use 
     * {@link resolve(ScarabLocalizationTool) } instead.
     * @return the resolved String
     */
    public String getMessage();

    /**
     * resolve the message according to the parameters of the 
     * given ScarabLocalizationTool instance. It may contain L10NMessage 
     * instances and Exceptions. The parameters should be resolved 
     * recursively if necessary.
     * @return
     */
    public String getMessage(ScarabLocalizationTool l10n);

}
