package org.tigris.scarab.soap;

/* ================================================================
 * Copyright (c) 2003 CollabNet.  All rights reserved.
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


// import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import junit.framework.TestCase;

public class TestQueryClient extends TestCase
{
    /**
     * 
     */
    public TestQueryClient(String s)
    {
        super(s);
    }

    public void testModuleList()
    {
        try
        {
            String endpointURL = "http://localhost:8080/scarab/services/QueryService";
//              String textToSend = "this is a query";
            
            Service  service = new Service();
            Call     call    = (Call) service.createCall();

            call.setTargetEndpointAddress( new java.net.URL(endpointURL) );
//            call.setProperty(Call., "urn:issue");
            call.setOperationName( new QName("QueryService", "getModuleList") );
            call.setReturnType(XMLType.XSD_ANY);

            QName    qn      = new QName( "urn:QueryService", "Issue" );

            call.registerTypeMapping(SoapIssue.class, qn,
                          new org.apache.axis.encoding.ser.BeanSerializerFactory(SoapIssue.class, qn),        
                          new org.apache.axis.encoding.ser.BeanDeserializerFactory(SoapIssue.class, qn));        
 

            Object[] ret = (Object[]) call.invoke( new Object[] { } );
            
            System.out.println("You typed : " + ret);
            System.out.println("size is " + ret.length);
            for (int i = 0; i < ret.length; i++)
            {
                System.out.println (ret[i]);
            }
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
    }

    public void testIssue()
    {
        try
        {
            String endpointURL = "http://localhost:8080/scarab/services/QueryService";
            String module = "PACD1";
            
            Service  service = new Service();
            Call     call    = (Call) service.createCall();

            call.setTargetEndpointAddress( new java.net.URL(endpointURL) );
            call.setOperationName( new QName("QueryService", "getIssue") );
            call.addParameter( "arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_ANY);

            Object ret = call.invoke( new Object[] {module} );
            
            System.out.println("You typed : " + ret);
            //System.out.println(ret.getId());
            //System.out.println(ret.getName());
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
    }

    public void testIssueList()
    {
        try
        {
            String endpointURL = "http://localhost:8080/scarab/services/QueryService";
            String module = "PACD";
            
            Service  service = new Service();
            Call     call    = (Call) service.createCall();

            call.setTargetEndpointAddress( new java.net.URL(endpointURL) );
            call.setOperationName( new QName("QueryService", "getIssueList") );
            call.addParameter( "arg1", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_ANYTYPE);

            Object[] ret = (Object[]) call.invoke( new Object[] {module} );
            
            System.out.println("You typed : " + ret);
            System.out.println("size is " + ret.length);
            for (int i = 0; i < ret.length; i++)
            {
                System.out.println (ret[i]);
            }
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
    }


}
