package org.tigris.scarab.test;

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

import java.io.File;

import org.apache.torque.om.NumberKey;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetManager;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;

/**
 * Test case that provides scaffolding for other Scarab test cases.
 * 
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh </a>
 * @version $Id$
 */
public class BaseScarabTestCase extends BaseTurbineTestCase
{
    private static Module module = null;
    private static IssueType defaultIssueType = null;
    protected static int nbrDfltModules = 7;
    protected static int nbrDfltIssueTypes = 5;
    private ScarabUser user0 = null;
    private ScarabUser user1 = null;
    private ScarabUser user2 = null;

    private Issue issue0 = null;
    private Attribute platformAttribute = null;
    private Attribute assignAttribute = null;
    private Attribute ccAttribute = null;

    private static boolean initialized = false;

    public BaseScarabTestCase()
    {
        System.setProperty("applicationRoot", "./src/webapp");
        System.setProperty("urn:avalon:home", "./src/webapp");
    }

    public BaseScarabTestCase(String name) throws Exception
    {
        super(name);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        if (!initialized)
        {
            initScarab();
            initialized = true;
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Grab Module #5 for testing. This is the same as what the web application
     * does and this is setup in ScarabPage.tempWorkAround()
     */
    private void initScarab() throws Exception
    {
        module = ModuleManager.getInstance(new NumberKey(5), false);
        defaultIssueType = IssueTypeManager
                .getInstance(new NumberKey(1), false);
    }

    protected ScarabUser getUser1() throws Exception
    {
        if (user1 == null)
        {
            user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
        }
        return user1;
    }

    protected ScarabUser getUser2() throws Exception
    {
        if (user2 == null)
        {
            user2 = ScarabUserManager.getInstance(new NumberKey(2), false);

        }
        return user2;
    }

    protected ScarabUser getUser5() throws Exception
    {
        if (user0 == null)
        {
            user0 = ScarabUserManager.getInstance(new NumberKey(5), false);
        }
        return user0;
    }

    protected Issue getIssue0() throws Exception
    {
        if (issue0 == null)
        {
            issue0 = IssueManager.getInstance(new NumberKey(1), false);
        }
        return issue0;
    }

    protected Attribute getPlatformAttribute() throws Exception
    {
        if (platformAttribute == null)
        {
            platformAttribute = AttributeManager.getInstance(new NumberKey(5));
        }
        return platformAttribute;
    }

    protected ActivitySet getEditActivitySet() throws Exception
    {
        Attachment attach = new Attachment();
        attach.setTextFields(getUser1(), getIssue0(),
                Attachment.MODIFICATION__PK);
        attach.setName("commenttest");
        attach.save();

        ActivitySet trans = ActivitySetManager.getInstance(
                ActivitySetTypePeer.EDIT_ISSUE__PK, getUser1(), attach);
        trans.save();
        return trans;
    }

    /**
     * If something like an Issue needs a mapping to a Module, then this is
     * Module #5 that you can use. For example, you should call:
     * issue.setModule(getModule()) in your Test before you use any of the rest
     * of the methods on the Issue object.
     */
    protected Module getModule()
    {
        return BaseScarabTestCase.module;
    }

    protected IssueType getDefaultIssueType()
    {
        return BaseScarabTestCase.defaultIssueType;
    }
    protected Attribute getAssignAttribute() throws Exception
    {
        if (assignAttribute == null)
        {
            assignAttribute = AttributeManager.getInstance(new NumberKey(2));
        }
        return assignAttribute;
    }

    protected Attribute getCcAttribute() throws Exception
    {
        if (ccAttribute == null)
        {
            ccAttribute = AttributeManager.getInstance(new NumberKey(13));
        }
        return ccAttribute;
    }

}