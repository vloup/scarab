package org.tigris.scarab.services.cache;

import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.pool.PoolService;
import org.apache.jcs.JCS;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabRuntimeException;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;


/**
 * This class is a replacement for the DefaultScarabCacheService
 * which uses JCS to manage the cached objects.
 *
 * @author <a href="mailto:ronny.voelker@elaxy.com">Ronny Völker</a>
 * @version $Id$
 */
public class JCSScarabCacheService
extends AbstractLogEnabled implements ScarabCacheService, Serviceable,  Initializable
{

    private PoolService poolService;
    private ServiceManager manager;
    private Class keyClass;
    private GroupCacheAccess JCSCache;

    private static final String SCARAB_CACHE_REGION =
        "org_tigris_scarab_services_cache_ScarabCache";

   public JCSScarabCacheService()
    {
    }

   private String getGroup()
    {
      return Long.toString(Thread.currentThread().getId());
    }

   private Object getFromJCS(ScarabCacheKey key) {
      return JCSCache.getFromGroup(key, getGroup());
   }

   private void putInJCS(ScarabCacheKey key, Object value) throws CacheException {
      JCSCache.putInGroup(key, getGroup(), value );
   }

    public Map getMapImpl()
    {
        throw new UnsupportedOperationException(
        "'getMap' is not implemented");
    }

    public void clearImpl()
    {
      JCSCache.invalidateGroup(getGroup());
    }

    public Object getImpl(Serializable instanceOrClass, String method)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method);
            result = getFromJCS(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            result = getFromJCS(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            result = getFromJCS(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable instanceOrClass, String method,
                             Serializable arg1, Serializable arg2,
                             Serializable arg3)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            result = getFromJCS(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public Object getImpl(Serializable[] keys)
    {
        Object result = null;
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(keys);
            result = getFromJCS(key);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
        return result;
    }

    public void putImpl(Object value, Serializable instanceOrClass,
                           String method)
    {
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method);
            putInJCS(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass,
                           String method, Serializable arg1)
    {
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1);
            putInJCS(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass,
                           String method, Serializable arg1, Serializable arg2)
    {
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2);
            putInJCS(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable instanceOrClass,
                           String method, Serializable arg1, Serializable arg2,
                           Serializable arg3)
    {
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(instanceOrClass, method, arg1, arg2, arg3);
            putInJCS(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }

    public void putImpl(Object value, Serializable[] keys)
    {
        try
        {
            ScarabCacheKey key =
                (ScarabCacheKey)poolService.getInstance(keyClass);
            key.init(keys);
            putInJCS(key, value);
        }
        catch (Exception e)
        {
            Log.get().error(e);
        }
    }


    /**
     * Avalon component lifecycle method
     * @avalon.dependency type="org.apache.fulcrum.factory.FactoryService"
     */
    public void service(ServiceManager manager)
    {
        this.manager = manager;
    }

    /**
     * Avalon component lifecycle method
     * Initializes the service by loading default class loaders
     * and customized object factories.
     *
     * @throws InitializationException if initialization fails.
     */
    public void initialize() throws Exception
    {
        try
        {
            poolService = (PoolService) manager.lookup(PoolService.ROLE);
        }
        catch (Exception e)
        {
            throw new ScarabRuntimeException(
               L10NKeySet.ExceptionScarabCacheService, e);
        }

        JCSCache = JCS.getInstance(SCARAB_CACHE_REGION);

        try
        {
            keyClass = Class
                .forName("org.tigris.scarab.services.cache.ScarabCacheKey");
        }
        catch (Exception x)
        {
            throw new InitializationException(
                "Failed to initialize ScarabCache",x);
        }

    }
}
