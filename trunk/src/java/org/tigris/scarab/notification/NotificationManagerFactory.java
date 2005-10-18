package org.tigris.scarab.notification;

import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.tigris.scarab.util.Log;

/**
 * This factory is used to get an instance of the  notification manager
 * configured in scarab.notificationmanager.classname.
 * 
 * @see org.tigris.scarab.notification.NotificationManager
 * @author jorgeuriarte
 */
public class NotificationManagerFactory
{
    public static Logger log = Log.get(NotificationManagerFactory.class
            .getName());

    private static NotificationManager instance = null;

    /**
     * Returns an instance of the currently defined NotificationManager.
     */
    public static NotificationManager getInstance()
    {
        if (instance == null)
        {
            String classname = Turbine.getConfiguration().getString(
                    "scarab.notificationmanager.classname",
                    "org.tigris.scarab.notification.ScarabNotificationManager");
            try
            {
                instance = (NotificationManager) Class.forName(classname)
                        .newInstance();
            }
            catch (Exception e)
            {
                log
                        .error("Could not instantiate notification manager '"
                                + classname + "'. Defaulting to '"
                                + ScarabNotificationManager.class.getName()
                                + "': " + e);
                instance = new ScarabNotificationManager();
            }
        }
        return instance;
    }
}
