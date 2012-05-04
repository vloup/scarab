package org.tigris.scarab.services.security.ldap;
/* ====================================================================
*
* Copyright (c) 2006 CollabNet.
*
* Licensed under the
*
*     CollabNet/Tigris.org Apache-style license (the "License");
*
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://scarab.tigris.org/LICENSE
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
* implied. See the License for the specific language governing
* permissions and limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of CollabNet.
*
*/

import javax.security.auth.callback.Callback;

public class LDAPUserInfoCallback implements Callback {
    
    private String sn;
    private String givenname;
    private String email;
    
    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }
    /**
     * @return Returns the givenname.
     */
    public String getGivenname() {
        return givenname;
    }
    /**
     * @return Returns the sn.
     */
    public String getSn() {
        return sn;
    }
    /**
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }
    /**
     * @param givenname The givenname to set.
     */
    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }
    /**
     * @param sn The sn to set.
     */
    public void setSn(String sn) {
        this.sn = sn;
    }
    
    

}
