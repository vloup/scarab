package org.tigris.scarab.util.xml;

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

import org.xml.sax.Attributes;

import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.ParentChildAttributeOption;

/**
 * Handler for the xpath "scarab/module/issue/transaction/activity"
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class ActivityRule extends BaseRule
{
    public ActivityRule(ImportBean ib)
    {
        super(ib);
    }
    
    /**
     * This method is called when the beginning of a matching XML element
     * is encountered.
     *
     * @param attributes The attribute list of this element
     */
    public void begin(Attributes attributes) throws Exception
    {
        log().debug("(" + getImportBean().getState() + 
            ") activity begin()");
        super.doInsertionOrValidationAtBegin(attributes);
    }
    
    protected void doInsertionAtBegin(Attributes attributes)
    {
        ActivityInfo activityInfo = new ActivityInfo();
        getDigester().push(activityInfo);
    }
    
    protected void doValidationAtBegin(Attributes attributes)
    {
        ActivityInfo activityInfo = new ActivityInfo();
        getDigester().push(activityInfo);
    }
    
    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end() throws Exception
    {
        super.doInsertionOrValidationAtEnd();
        log().debug("(" + getImportBean().getState() + 
            ") activity end()");
    }
    
    /**
     * This function will add the activity to the transaction.
     * FIXME: there is a problem with this function (besides the bad code)
     * the create issue stuff is working, but the edit issue stuff
     * does not appear to be working.
     */
    protected void doInsertionAtEnd()
        throws Exception
    {
        ActivityInfo activityInfo = (ActivityInfo)getDigester().pop();
        Transaction transaction = (Transaction)getDigester().pop();
        Issue issue = (Issue)getDigester().pop();
        Attribute attribute = Attribute.getInstance(activityInfo.getName());
        if(attribute == null)
        {
            // create attribute
            attribute = AttributeManager.getInstance();
            attribute.setName(activityInfo.getName());
            attribute.setAttributeType(activityInfo.getType());
            attribute.setDescription("Generated by Data import util");
            attribute.save();
            
            if (attribute.isOptionAttribute())
            {
                // create option
                ParentChildAttributeOption newPCAO = 
                    ParentChildAttributeOption.getInstance();
                newPCAO.setName(activityInfo.getValue());
                newPCAO.setAttributeId(attribute.getAttributeId());
                newPCAO.save();
            }
        }
        
        AttributeValue attributeValue = 
            AttributeValue.getNewInstance(attribute, issue);
        
        if (attribute.isOptionAttribute())
        {
            AttributeOption attributeOption = AttributeOption
                .getInstance(attribute, activityInfo.getValue());
            
            if (attributeOption == null)
            {
                // create option
                ParentChildAttributeOption newPCAO = 
                    ParentChildAttributeOption.getInstance();
                newPCAO.setName(activityInfo.getValue());
                newPCAO.setAttributeId(attribute.getAttributeId());
                newPCAO.save();
                attributeOption = AttributeOption
                    .getInstance(attribute, activityInfo.getValue());
            }
            attributeValue.setOptionId(attributeOption.getOptionId());
        }
        else
        {
            attributeValue.setValue(activityInfo.getValue());
        }
        
        if (transaction.getTransactionType().getTypeId()
            .equals(TransactionTypePeer.CREATE_ISSUE__PK))
        {
            attributeValue.startTransaction(transaction);
            attributeValue.save();
        }
        else
        {
            Activity activity = new Activity();
            activity.create(issue, attribute, activityInfo.getDescription(), 
                            transaction, activityInfo.getOldValue(), 
                            activityInfo.getValue());
        }
        
        getDigester().push(issue);
        getDigester().push(transaction);
    }
    
    protected void doValidationAtEnd()
        throws Exception
    {
        ActivityInfo activityInfo = (ActivityInfo)getDigester().pop();
        if (activityInfo.getName().equals("Assigned To"))
        {
            validateUser(activityInfo.getValue());
            if (activityInfo.getOldValue() != null)
            {
                validateUser(activityInfo.getOldValue());
            }
        }
    }
}
