package org.tigris.scarab.util;

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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.mail.SendFailedException;

import org.apache.commons.lang.StringUtils;

import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.TemplateEmail;
import org.apache.fulcrum.template.TemplateService;
import org.apache.fulcrum.template.TurbineTemplateService;
import org.apache.fulcrum.velocity.ContextAdapter;
import org.apache.fulcrum.mimetype.TurbineMimeTypes;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.TurbineServices;

import org.apache.turbine.Turbine;

import org.tigris.scarab.om.PendingMessage;
import org.tigris.scarab.om.PendingMessageRecipient;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.services.email.VelocityEmail;

/**
 * Encapsulates email content and sends or queues a notification email.
 * Class EmailHandler initializes and configures this class.
 * Class Email contains only non-static methods.
 *
 * @author <a href="mailto:thierry.lach@bbdodetroit.com">Thierry Lach</a>
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class Email extends TemplateEmail
{

    /**
     * Override the super.handleRequest() and process the template
     * our own way.
     * This could have been handled in a more simple way, which was
     * to create a new service and associate the emails with a different
     * file extension which would have prevented the need to override
     * this method, however, that was discovered after the fact and it
     * also seemed to be a bit more work to change the file extension. 
     */
    protected String handleRequest()
        throws ServiceException
    {
        String result = null;
        try
        {
            result = VelocityEmail
                     .handleRequest(new ContextAdapter(getContext()),
                                    getTemplate());
        }
        catch (Exception e)
        {
            throw new ServiceException(e);
        }
        return result;
    }

	/**
	 * Send an email request via the queue.
	 */
	public void sendQueued()
	    throws Exception
	{
		Iterator i;

		PendingMessage message = new PendingMessage();
		message.setFrom(this.getFromEmail());
        message.setSubject(this.getSubject());

		// Process the template.
		TurbineTemplateService tts = (TurbineTemplateService) TurbineServices
				.getInstance().getService(TemplateService.SERVICE_NAME);

		String body = tts.handleRequest(this.getContext(), this.getTemplate());
		message.setBody(body.getBytes());

		message.save();

		PendingMessageRecipient recipient;

        // Handle TO list
		i = this.getToList().iterator();
		while (i.hasNext())
		{
			ScarabUser u = (ScarabUser)i.next();
			recipient = new PendingMessageRecipient();
			recipient.setMessageId(message.getMessageId());
			recipient.setType("TO");
			recipient.setAddress(u.getEmail());
			recipient.save();
		}

		// Handle CC list
		i = this.getCCList().iterator();
		while (i.hasNext())
		{
			ScarabUser u = (ScarabUser)i.next();
			recipient = new PendingMessageRecipient();
			recipient.setMessageId(message.getMessageId());
			recipient.setType("CC");
			recipient.setAddress(u.getEmail());
			recipient.save();
		}

		// Handle Reply To list
		i = this.getReplyToList().iterator();
		while (i.hasNext())
		{
			// TODO Change reply-to into a list
			ScarabUser u = (ScarabUser)i.next();
			recipient = new PendingMessageRecipient();
			recipient.setMessageId(message.getMessageId());
			recipient.setType("REPLYTO");
			recipient.setAddress(u.getEmail());
			recipient.save();
		}

	}

}
