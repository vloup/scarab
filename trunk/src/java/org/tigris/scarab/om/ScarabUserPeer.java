package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

import java.util.Vector;

// Village classes
import com.workingdogs.village.*;

import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.om.ObjectKey;
import org.apache.turbine.om.security.peer.TurbineUserPeer;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.services.db.TurbineDB;
import org.apache.turbine.util.db.pool.DBConnection;

/**
    This class is an abstraction that is currently based around
    Turbine's code. We can change this later. It is here so
    that it is easier to change later to work under different
    implementation needs.

    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id$
*/
public class ScarabUserPeer extends BaseScarabUserPeer
{

    /** 
     * Retrieve a single object by pk
     *
     * @param ObjectKey pk
     */
    public static ScarabUser retrieveByPK( ObjectKey pk )
        throws Exception
    {
        DBConnection db = null;
        ScarabUser retVal = null;
       try
        {
           db = TurbineDB
               .getConnection( getTableMap().getDatabaseMap().getName() );
           retVal = retrieveByPK( pk, db );
        }
        finally
        {
           if (db != null)
              TurbineDB.releaseConnection(db);
        }
        return(retVal);
    }

    /** 
     * Retrieve a single object by pk
     *
     * @param ObjectKey pk
     * @param DBConnection dbcon
     */
    public static ScarabUser retrieveByPK( ObjectKey pk, DBConnection dbcon )
        throws Exception
    {

        Criteria criteria = new Criteria();
            criteria.add( TurbineUserPeer.USER_ID, pk );
        Vector v = doSelect(criteria, dbcon);
        if ( v.size() != 1)
        {
            throw new Exception("Failed to select one and only one row.");
        }
        else
        {
            return (ScarabUser)v.firstElement();
        }
    }

}    


