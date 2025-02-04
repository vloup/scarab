package org.tigris.scarab.om;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.test.BaseTurbineTestCase;

/**
 * A Testing Suite for the om.Issue class.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @version $Id$
 */
public class IssueTest extends BaseTurbineTestCase
{
    private List issueList = new ArrayList();
    protected static int nbrDfltModules = 7;
    protected static int nbrDfltIssueTypes = 5;
    private ScarabUserTestObjectFactory testUsers = new ScarabUserTestObjectFactory();
    private IssueTestObjectFactory testIssues = new IssueTestObjectFactory();

    public void setUp() throws Exception
    {
        super.setUp();
        createTestIssues();
        loopThruTestIssues();

    }


    private void createTestIssues() throws Exception
    {
        // loops thru module and issue type combinations
        // creates an issue in each combination
        for (int i = 1; i < nbrDfltModules + 1; i++)
        {
            for (int j = 1; j < nbrDfltIssueTypes + 1; j++)
            {
                Module module =
                    ScarabModulePeer.retrieveByPK(
                        new NumberKey(Integer.toString(i)));
                IssueType issueType =
                    IssueTypePeer.retrieveByPK(
                        new NumberKey(Integer.toString(j)));
                Issue issue = Issue.getNewInstance(module, issueType);
                issueList.add(issue);
            }
        }
    }

    private void loopThruTestIssues() throws Exception
    {
        for (int i = 1; i < issueList.size(); i++)
        {
            Issue issue = (Issue) issueList.get(i);
            System.out.println("MODULE=" + issue.getModule().getName());
            System.out.println(
                "ISSUE TYPE = " + issue.getIssueType().getName());
            String strUniqueID = issue.getUniqueId();
            System.out.println("Unique id: " + strUniqueID);
            runTestGetAllAttributeValuesMap(issue);
        }
    }

    private void runTestGetAllAttributeValuesMap(Issue issue) throws Exception
    {
        System.out.println("testGetAllAttributeValuesMap()");
        Map map = issue.getAllAttributeValuesMap();
        System.out.println("getAllAttributeValuesMap().size(): " + map.size());
        int expectedSize = 12;
        switch (Integer.parseInt(issue.getTypeId().toString()))
        {
            case 1 :
                expectedSize = 12;
                break;
            case 2 :
                expectedSize = 12;
                break;
            case 3 :
                expectedSize = 11;
                break;
            case 4 :
                expectedSize = 11;
                break;
            case 5 :
                expectedSize = 9;
                break;
            case 6 :
                expectedSize = 9;
                break;
            case 7 :
                expectedSize = 9;
                break;
            case 8 :
                expectedSize = 9;
                break;
            case 9 :
                expectedSize = 9;
                break;
            case 10 :
                expectedSize = 9;
        }
        //assertEquals("issue.getTypeId():" + issue.getTypeId(),expectedSize, map.size());
        assertTrue("issue.getTypeId():" + issue.getTypeId(),map.size()>4);
    }

    private void assignUser() throws Exception
    {
        System.out.println("assignUser()");
        Attribute assignAttr = getAssignAttribute();
        ScarabUser assigner = testUsers.getUser1();
        ScarabUser assignee = testUsers.getUser2();
        testIssues.getIssue0().assignUser(
            null,
            assigner,
            assignee,
            assignAttr,
            getAttachment(assigner));
    }

    public void testGetAssociatedUsers() throws Exception
    {
        System.out.println("testAssociatedUsers()");
        assignUser();
        assertEquals(testIssues.getIssue0().getAssociatedUsers().size(), 1);
        List pair = (List) testIssues.getIssue0().getAssociatedUsers().iterator().next();
        assertEquals(pair.get(1), testUsers.getUser1());
    }

    public void OFFtestChangeUserAttributeValue() throws Exception
    {
        System.out.println("testChangeUserAttributeValue()");
        assignUser();
        Attribute assignAttr = getAssignAttribute();        
        Attribute ccAttr = getCcAttribute();
        ScarabUser assigner = testUsers.getUser1();
        ScarabUser assignee = testUsers.getUser2();
        AttributeValue attVal = testIssues.getIssue0().getAttributeValue(assignAttr);
        testIssues.getIssue0().changeUserAttributeValue(
            null,
            assigner,
            assignee,
            attVal,
            ccAttr,
            getAttachment(assigner));
        List pair = (List) testIssues.getIssue0().getAssociatedUsers().iterator().next();
        assertEquals(pair.get(0), ccAttr);
    }

    public void OFFtestDeleteUser() throws Exception
    {
        System.out.println("testDeleteUser()");
        Attribute assignAttr = getAssignAttribute();
        ScarabUser assigner = testUsers.getUser1();
        AttributeValue attVal = testIssues.getIssue0().getAttributeValue(assignAttr);
        testIssues.getIssue0().deleteUser(
            null,
            testUsers.getUser1(),
            testUsers.getUser2(),
            attVal,
            getAttachment(assigner));
        assertEquals(testIssues.getIssue0().getAssociatedUsers().size(), 0);
    }

    public void testGetUserAttributeValues() throws Exception
    {
        System.out.println("testAssociatedUsers()");
        assignUser();
        List attVals = testIssues.getIssue0().getUserAttributeValues();
        AttributeValue attVal = (AttributeValue) attVals.get(0);
        assertEquals(attVal.getAttributeId().toString(), "2");
    }

    public void testGetEligibleUsers() throws Exception
    {
        System.out.println("testGetEligibleUsers()");
        assignUser();
        List users = testIssues.getIssue0().getEligibleUsers(getAssignAttribute());
        assertTrue(users.size()>0);
    }

    public void testGetUsersToEmail() throws Exception
    {
        System.out.println("testGetUsersToEmail()");
        assignUser();
        Set users =
            testIssues.getIssue0().getUsersToEmail(
                AttributePeer.EMAIL_TO,
                testIssues.getIssue0(),
                null);
        assertEquals(users.size(), 2);
    }

    private Attachment getAttachment(ScarabUser assigner) throws Exception
    {
        Attachment attachment = new Attachment();
        attachment.setData("test reason");
        attachment.setName("comment");
        attachment.setTextFields(
            assigner,
            testIssues.getIssue0(),
            Attachment.MODIFICATION__PK);
        attachment.save();
        return attachment;
    }

    public void testCounts() throws Exception
    {
        System.out.println("Testing IssuePeer count methods");
        assignUser();
        int count = IssuePeer.count(new Criteria());
        assertTrue(
            "IssuePeer.count(new Criteria()) returned " + count,
            count>0);
        count = IssuePeer.countDistinct(new Criteria());
        assertTrue(
            "IssuePeer.countDistinct(new Criteria()) returned " + count,
            count>0);
    }
    
    protected ScarabUser getUser2()
    throws Exception
    {
            return ScarabUserManager.getInstance(new NumberKey(2), false);
     
    }    
    
    protected Attribute getAssignAttribute()
    throws Exception
    {
            return AttributeManager.getInstance(new NumberKey(2));
    }
        
    protected Attribute getCcAttribute()
    throws Exception
    {
       
            return AttributeManager.getInstance(new NumberKey(13));
        
    }
}
