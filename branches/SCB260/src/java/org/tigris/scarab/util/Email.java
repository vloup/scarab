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

import java.io.StringWriter;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.mail.SendFailedException;

import org.apache.fulcrum.schedule.JobEntry;
import org.apache.fulcrum.schedule.ScheduleService;

import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.DefaultTemplateContext;
import org.apache.fulcrum.template.TemplateEmail;

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.velocity.VelocityService;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import org.apache.turbine.Turbine;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.PendingMessage;
import org.tigris.scarab.om.PendingMessageRecipient;

/**
 * Sends or queues a notification email.
 *
 * @author <a href="mailto:thierry.lach@bbdodetroit.com">Thierry Lach</a>
 * @author <a href="mailto:jon@collab.net">Jon Scott Stevens</a>
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class Email
{
    private static boolean enableEmail = true;

    private static boolean queueEmail = 
        Turbine.getConfiguration().getBoolean("scarab.queue.email", false);


    /**
     * Quick way to turn off sending of emails. By default
     * emails can be sent.
     */
    public static void setEnable(boolean value)
    {
        enableEmail = value;
    }

    public static boolean sendEmail( TemplateContext context, Module module, 
                                     Object fromUser, Object replyToUser,
                                     List toUsers, List ccUsers,
                                     String subject, String template )
        throws Exception
    {
            return sendEmail(context, module, 
                                   fromUser, replyToUser,
                                   toUsers, ccUsers, null,
                                   subject, template);
    }

    public static boolean sendEmail( TemplateContext context, Module module, 
                                     Object fromUser, Object replyToUser,
                                     List toUsers, List ccUsers, List bccUsers,
                                     String subject, String template )
        throws Exception
    {
        // System.out.println("scarab.queue.email=" + Turbine.getConfiguration().getBoolean("scarab.queue.email"));
        if (!enableEmail)
        {
            return true;
        }

        if (queueEmail)
        {
            return sendEmailQueued((VelocityContext)context, module, 
                                   fromUser, replyToUser,
                                   toUsers, ccUsers, bccUsers,
                                   subject, template);
        }
        else
        {
            return sendEmailUnqueued(context, module, 
                                     fromUser, replyToUser,
                                     toUsers, ccUsers, bccUsers,
                                     subject, template);
        }
    }

    private static boolean sendEmailQueued( VelocityContext context,
                                    Module module, 
                                    Object fromUser, Object replyToUser,
                                    List toUsers, List ccUsers, List bccUsers,
                                    String subject, String template )
        throws Exception
    {
        // VelocityService vs = null;

        boolean status = true;
        try
        {
        PendingMessage message = new PendingMessage();

        if (fromUser instanceof ScarabUser)
        {
            message.setFrom(((ScarabUser)fromUser).getEmail());
        }
        else
        {
            message.setFrom(fromUser.toString());
        }

        if (replyToUser instanceof ScarabUser)
        {
            message.setReplyTo(((ScarabUser)replyToUser).getEmail());
        }
        else
        {
            message.setReplyTo(replyToUser.toString());
        }

        message.setSubject(subject);
        // message.setBody(template.getBytes());

        // Process the template.
        StringWriter sw = new StringWriter();

        Velocity.init();
        Velocity.mergeTemplate(template, context, sw);
        // Template t = Velocity.getTemplate(template);

        // String body = vs.handleRequest(context, template);
        message.setBody(sw.toString().getBytes());


        message.save();
        // System.out.println ("Created message id " + message.getMessageId());

        PendingMessageRecipient recipient;

        Iterator iter = toUsers.iterator();
        while ( iter.hasNext() ) 
        {
            ScarabUser toUser = (ScarabUser)iter.next();
            recipient = new PendingMessageRecipient();
            recipient.setMessageId(message.getMessageId());
            recipient.setType("TO");
            recipient.setAddress(toUser.getEmail());
            recipient.save();
        }

        String archiveEmail = module.getArchiveEmail();
        if (archiveEmail != null && archiveEmail.trim().length() > 0)
        {
            ScarabUser ccUser = (ScarabUser)iter.next();
            recipient = new PendingMessageRecipient();
            recipient.setMessageId(message.getMessageId());
            recipient.setType("CC");
            recipient.setAddress(archiveEmail);
            recipient.save();
        }

        if (ccUsers != null)
        {
            iter = ccUsers.iterator();
            while ( iter.hasNext() ) 
            {
                ScarabUser ccUser = (ScarabUser)iter.next();
                recipient = new PendingMessageRecipient();
                recipient.setMessageId(message.getMessageId());
                recipient.setType("CC");
                recipient.setAddress(ccUser.getEmail());
                recipient.save();
            }
        }

        if (bccUsers != null)
        {
            iter = bccUsers.iterator();
            while ( iter.hasNext() ) 
            {
                ScarabUser bccUser = (ScarabUser)iter.next();
                recipient = new PendingMessageRecipient();
                recipient.setMessageId(message.getMessageId());
                recipient.setType("BCC");
                recipient.setAddress(bccUser.getEmail());
                recipient.save();
            }
        }
 
            // turn off the event cartridge handling so that when
            // we process the email, the html codes are escaped.
            // vs = (VelocityService) TurbineServices
                // .getInstance().getService(VelocityService.SERVICE_NAME);
            // vs.setEventCartridgeEnabled(false);

// 
            // TemplateEmail te = getTemplateEmail(context,  module, fromUser, 
                // replyToUser, subject, template);

            // iter = toUsers.iterator();
            // while ( iter.hasNext() ) 
            // {
                // ScarabUser toUser = (ScarabUser)iter.next();
                // te.addTo(toUser.getEmail(),
                         // toUser.getFirstName() + " " + toUser.getLastName());
            // }
           //  
            // if (ccUsers != null)
            // {
                // iter = ccUsers.iterator();
                // while ( iter.hasNext() ) 
                // {
                    // ScarabUser ccUser = (ScarabUser)iter.next();
                    // te.addCc(ccUser.getEmail(),
                             // ccUser.getFirstName() + " " + ccUser.getLastName());
                // }
            // }

            // String archiveEmail = module.getArchiveEmail();
            // if (archiveEmail != null && archiveEmail.trim().length() > 0)
            // {
                // te.addCc(archiveEmail, null);
            // }

            // try
            // {
                // te.sendMultiple();
            }
            catch (SendFailedException e)
            {
                status = false;
            }
        // }
        // finally
        // {
            // if (vs != null)
            // {
                // vs.setEventCartridgeEnabled(true);
            // }
        // }

        // submitEmailJob();
        return status;
    }

    private static boolean sendEmailUnqueued( TemplateContext context,
                                 Module module, 
                                 Object fromUser, Object replyToUser,
                                 List toUsers, List ccUsers, List bccUsers,
                                 String subject, String template )
        throws Exception
    {
        VelocityService vs = null;
        try
        {
            // turn off the event cartridge handling so that when
            // we process the email, the html codes are escaped.
            vs = (VelocityService) TurbineServices
                .getInstance().getService(VelocityService.SERVICE_NAME);
            vs.setEventCartridgeEnabled(false);

            boolean success = true;

            TemplateEmail te = getTemplateEmail(context,  module, fromUser, 
                replyToUser, subject, template);

            Iterator iter = toUsers.iterator();
            while ( iter.hasNext() ) 
            {
                ScarabUser toUser = (ScarabUser)iter.next();
                te.addTo(toUser.getEmail(),
                         toUser.getFirstName() + " " + toUser.getLastName());
            }
            
            if (ccUsers != null)
            {
                iter = ccUsers.iterator();
                while ( iter.hasNext() ) 
                {
                    ScarabUser ccUser = (ScarabUser)iter.next();
                    te.addCc(ccUser.getEmail(),
                             ccUser.getFirstName() + " " + ccUser.getLastName());
                }
            }

            String archiveEmail = module.getArchiveEmail();
            if (archiveEmail != null && archiveEmail.trim().length() > 0)
            {
                te.addCc(archiveEmail, null);
            }

            try
            {
                te.sendMultiple();
            }
            catch (SendFailedException e)
            {
                success = false;
            }
            return success;
        }
        finally
        {
            if (vs != null)
            {
                vs.setEventCartridgeEnabled(true);
            }
        }
    }

    /**
     * Single user recipient.
     */ 
    public static boolean sendEmail( TemplateContext context, Module module,
                                     Object fromUser, Object replyToUser, 
                                     ScarabUser toUser, 
                                     String subject, String template )
        throws Exception
    {
        List toUsers = new LinkedList();
        toUsers.add(toUser);
        return sendEmail( context, module, fromUser, replyToUser, toUsers, 
                          null, null, subject, template );
    }

    private static TemplateEmail getTemplateEmail( 
                                     TemplateContext context, Module module, 
                                     Object fromUser, Object replyToUser,
                                     String subject, String template )
        throws Exception
    {
        TemplateEmail te = new TemplateEmail();
            if ( context == null ) 
            {
                context = new DefaultTemplateContext();
            }        
            te.setContext(context);
            
            if (fromUser instanceof ScarabUser)
            {
                ScarabUser u = (ScarabUser)fromUser;
                te.setFrom(u.getName(), u.getEmail());
            }
            else if (fromUser instanceof String[])
            {
                String[] s = (String[])fromUser;
                te.addReplyTo(s[0], s[1]);
            }
            else
            {
                // assume string
                String key = (String)fromUser;
                if (fromUser == null)
                {
                    key = "scarab.email.default";
                } 
                
                te.setFrom(Turbine.getConfiguration().getString
                           (key + ".fromName", "Scarab System"), 
                           Turbine.getConfiguration().getString
                           (key + ".fromAddress",
                            "help@localhost"));
            }

            if (replyToUser instanceof ScarabUser)
            {
                ScarabUser u = (ScarabUser)replyToUser;
                te.addReplyTo(u.getName(), u.getEmail());
            }
            else if (replyToUser instanceof String[])
            {
                String[] s = (String[])replyToUser;
                te.addReplyTo(s[0], s[1]);
            }
            else
            {
                // assume string
                String key = (String)replyToUser;
                if (fromUser == null)
                {
                    key = "scarab.email.default";
                } 
                
                te.addReplyTo(Turbine.getConfiguration()
                              .getString(key + ".fromName", "Scarab System"), 
                              Turbine.getConfiguration()
                              .getString(key + ".fromAddress",
                                         "help@localhost"));
            }
            
            if (subject == null)
            {
                te.setSubject((Turbine.getConfiguration().
                               getString("scarab.email.default.subject")));
            }
            else
            {
                te.setSubject(subject);
            }
            
            if (template == null)
            {
                te.setTemplate(Turbine.getConfiguration().
                               getString("scarab.email.default.template"));
            }
            else
            {
                te.setTemplate(template);
            }
            

            String charset = Turbine.getConfiguration()
                .getString(ScarabConstants.DEFAULT_EMAIL_ENCODING_KEY); 
            if (charset != null && charset.trim().length() > 0) 
            {
                te.setCharset(charset);                
            }

            return te;
    }

    public static void submitEmailJob()
    throws Exception
    {
        ScheduleService ss = (ScheduleService)TurbineServices.getInstance()
                            .getService(ScheduleService.SERVICE_NAME);
        /* By default, every five minutes */
        JobEntry je = new JobEntry(300, -1, -1, -1, -1, "QueuedEmailJob");
        ss.addJob(je);
    }

}
