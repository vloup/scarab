package org.tigris.scarab.services;

/* ====================================================================
*
* Copyright (c) 2006 Hussayn dabbous.
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

import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.turbine.services.yaaficomponent.YaafiComponentService;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.tools.localization.L10NMessage;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.ScarabRuntimeException;

public class ServiceManager
{
    static ServiceManager instance = null;
    
    private ServiceManager()
    {
        // intentionally left blank
    }

    /**
     * Return the one and only Servicemanager instance
     * @return
     */
    public static ServiceManager getInstance()
    {
        if(instance == null)
        {
            instance = new ServiceManager();
        }
        return instance;
    }

    /**
     * Lookup a service. If the service exists, but is not initialized yet,
     * perform the initialization before returnning the service.
     * The method is synchronized in order to avoid duplicate initializations
     * due to possible parallel calls from multiple threads.
     * @param serviceClass
     * @return
     * @throws ScarabException
     */
    public synchronized Object lookup(Class serviceClass)
    {
        Object serviceInstance;
        YaafiComponentService yaafi = getService();
        try{
            serviceInstance =  yaafi.lookup(serviceClass.getName());
        } 
        catch (Exception e) 
        {
            L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric, e);
            throw new ScarabRuntimeException(msg);
        }
        return serviceInstance;
    }
    
    public static YaafiComponentService getService()
    {
        YaafiComponentService yaafi = null;
        try{
            yaafi = (YaafiComponentService) TurbineServices.getInstance().getService(
                    YaafiComponentService.SERVICE_NAME);
        } 
        catch (Exception e) 
        {
            L10NMessage msg = new L10NMessage(L10NKeySet.ExceptionGeneric, e);
            throw new ScarabRuntimeException(msg);
        }
        return yaafi;
        
    }

    
}
