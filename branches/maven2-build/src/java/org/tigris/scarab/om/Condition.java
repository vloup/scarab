//  ================================================================
//  Copyright (c) 2000-2005 CollabNet.  All rights reserved.
//  
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//  
//  1. Redistributions of source code must retain the above copyright
//  notice, this list of conditions and the following disclaimer.
// 
//  2. Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  
//  3. The end-user documentation included with the redistribution, if
//  any, must include the following acknowlegement: "This product includes
//  software developed by Collab.Net <http://www.Collab.Net/>."
//  Alternately, this acknowlegement may appear in the software itself, if
//  and wherever such third-party acknowlegements normally appear.
//  
//  4. The hosted project names must not be used to endorse or promote
//  products derived from this software without prior written
//  permission. For written permission, please contact info@collab.net.
//  
//  5. Products derived from this software may not use the "Tigris" or 
//  "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
//  prior written permission of Collab.Net.
//  
//  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
//  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
//  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
//  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
//  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
//  IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// 
//  ====================================================================
//  
//  This software consists of voluntary contributions made by many
//  individuals on behalf of Collab.Net.

package org.tigris.scarab.om;


import java.util.Map;
import org.apache.torque.om.Persistent;

/**
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class Condition
    extends BaseCondition
    implements Persistent
{
    public boolean equals(Condition cond)
    {
        boolean bRdo = false;
        Integer attr1 = this.getAttributeId();
        Integer attr2 = cond.getAttributeId();
        if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                attr1 == attr2)
        {
            attr1 = this.getOptionId();
            attr2 = cond.getOptionId();
            if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                    attr1 == attr2)
            {
                attr1 = this.getModuleId();
                attr2 = cond.getModuleId();
                if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                        attr1 == attr2)
                {
                    attr1 = this.getIssueTypeId();
                    attr2 = cond.getIssueTypeId();
                    if ((attr1 != null && attr2 != null && attr1.intValue() == attr2.intValue()) ||
                            attr1 == attr2)
                    {
                        bRdo = true;
                    }
                }
            }
        }
        return bRdo;
    }
    
    public boolean equals(Object obj)
    {
        return this.equals((Condition)obj);
    }
    
    /**
     * Evaluates the current condition against the user and issue passed.
     */
    public boolean evaluate(ScarabUser user, Issue issue) {
        boolean bEval = false;
        try {
            if (this.getAttributeOption() != null) {
                // Old-style condition (tied to any of the attribute-options
                // being selected)
                Attribute requiredAttribute = this.getAttributeOption().getAttribute();
                Integer optionId = this.getOptionId();
                AttributeValue av = issue.getAttributeValue(requiredAttribute);
                if (av != null) {
                    Integer issueOptionId = av.getOptionId();
                    if (issueOptionId != null && issueOptionId.equals(optionId)) {
                        bEval = true;
                    }
                }
            } else {
                // New-style condition (driven by an evaluated script)
            }
        } catch (Exception e) {
            this.getLog().debug("evaluate: Failed to evaluate, will return false. ", e);
        }
        return bEval;
    }
}
