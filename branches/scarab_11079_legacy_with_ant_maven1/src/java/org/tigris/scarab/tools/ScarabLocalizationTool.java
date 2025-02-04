package org.tigris.scarab.tools;

/* ================================================================
 * Copyright (c) 2000 CollabNet.  All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.fulcrum.localization.LocaleTokenizer;
import org.apache.fulcrum.localization.Localization;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.LocalizationTool;
import org.tigris.scarab.tools.localization.Localizable;
import org.tigris.scarab.tools.localization.LocalizationKey;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ReferenceInsertionFilter;
import org.tigris.scarab.util.SkipFiltering;

/**
 * Scarab-specific localiztion tool.  Uses a specific property
 * format to map a generic i10n key to a specific screen.
 * 
 * For example, the $i10n.title on the screen:
 * admin/AddPermission.vm would be in ScarabBundle_en.properties
 * <blockquote><code><pre>
 *  admin/AddPermission.vm.Title
 * </pre></code> </blockquote>
 * 
 * 
 * @author <a href="mailto:dlr@collab.net">Daniel Rall </a>
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh </a>
 */
public class ScarabLocalizationTool extends LocalizationTool
{
    /**
     * The Locale to be used, if the Resource could not be found in
     * one of the Locales specified in the Browser's language preferences.
     */
    public static Locale        DEFAULT_LOCALE = new Locale("en", "");

    /**
     * The portion of a key denoting the title property.
     */
    private static final String TITLE_PROP     = "Title";

    /**
     * We need to keep a reference to the request's <code>RunData</code> so
     * that we can extract the name of the target <i>after </i> the <code>Action</code>
     * has run (which may have changed the target from its original value as a
     * sort of internal redirect).
     */
    private RunData             data;

    /**
     * Initialized by <code>init()</code>, 
     * cleared by <code>refresh()</code>.
     */
    private String              bundlePrefix;
    private String              oldBundlePrefix;

    /**
     * Store the collection of locales to be used for ResourceBundle resolution.
     * If the Class is instantiated from RunData, the collection contains all
     * Locales in order of preference as specified in the Browser.
     * If the Class is instantiated from a Locale, the collection contains
     * just that Locale.
     * 
     */
    private List                locales;

    /**
     * true: enables cross-site scripting filtering.
     * @see resolveArgumentTemplates 
     * @see format(String, Object[])
     */
    private boolean             filterEnabled  = true;

    /**
     * Creates a new instance.  Client should 
     * {@link #init(Object) initialize} the instance.
     */
    public ScarabLocalizationTool()
    {
    }

    /**
     * Return the localized property value.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     */
    public String get(Localizable key)
    {
        return this.get(key, false);
    }

    
    /**
     * Return the localized property value.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     * If ignoreMissingResource is set to true, return the resource key
     * instead of throwing an exception. (Typically usefull from velocity)
     */
    public String get(Localizable key, boolean ignoreMissingResource)
    {
        String theKey = key.toString();
        String result;
        if ( ignoreMissingResource )
        {
            result = this.getIgnoreMissingResource(theKey);
        }
        else
        {
            result = this.get(theKey);
        }
        return result;
    }


    /**
     * Return the localized property value.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     * Throws an Exception when an error occurs.
     */
    private String getInternal(String key) 
        throws Exception
    {
        String value = null;
        
        // Try with all defined "Browser"-Locales ordered by relevance
        Iterator iter = locales.iterator();
        while (value == null && iter.hasNext())
        {
            Locale locale = (Locale) iter.next();
            value = resolveKey(key, locale);
        }
        return value;
    }

    
    /**
     * Return the localized property value.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     * NOTE: Please don't use this method from the Java-code.
     * It is intended for use with Velocity only!
     * @deprecated Please use {@link #get(LocalizationKey)} instead
     */
    public String get(String key)
    {
        String value;
        try
        {
            value = getInternal(key);
            if (value == null)
            {
                value = createMissingResourceValue(key);
            }
        }
        catch(Exception e)
        {
            value = createBadResourceValue(key, e);
        }
        return value;
    }

    /**
     * Return the localized property value.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     * NOTE: Please don't use this method from the Java-code.
     * It is intended for use with Velocity only!
     * @deprecated Please use {@link #get(LocalizationKey)} instead
     */
    public String getIgnoreMissingResource(String key)
    {
        // [HD]: I plan to make this method private.
        // Currently it is still used from the velocity templates.
        String value;
        try
        {
            value = getInternal(key);
            if(value == null)
            {
                value = key;
            }
        }
        catch(Exception e)
        {
            value = createBadResourceValue(key, e);
        }
        return value;
    }
    
    /**
     * Formats a localized value using the provided object.
     * 
     * @param key  The identifier for the localized text to retrieve,
     * @param arg1 The object to use as {0} when formatting the localized text.
     * @return     Formatted localized text.
     * @see        #format(String, List)
     */
    public String format(String key, Object arg1)
    {
        return format(key, new Object[]{arg1});
    }

    /**
     * Formats a localized value using the provided objects.
     * 
     * @param key  The identifier for the localized text to retrieve,
     * @param arg1 The object to use as {0} when formatting the localized text.
     * @param arg2 The object to use as {1} when formatting the localized text.
     * @return     Formatted localized text.
     * @see        #format(String, List)
     */
    public String format(String key, Object arg1, Object arg2)
    {
        return format(key, new Object[]{arg1, arg2});
    }

    /**
     * Formats a localized value using the provided objects.
     * 
     * @param key  The identifier for the localized text to retrieve,
     * @param arg1 The object to use as {0} when formatting the localized text.
     * @param arg2 The object to use as {1} when formatting the localized text.
     * @param arg3 The object to use as {2} when formatting the localized text.
     * @return     Formatted localized text.
     * @see        #format(String, List)
     */
    public String format(String key, Object arg1, Object arg2, Object arg3)
    {
        return format(key, new Object[]{arg1, arg2, arg3});
    }

    /**
     * <p>Formats a localized value using the provided objects.</p>
     * 
     * <p>ResourceBundle:
     * <blockquote><code><pre>
     *  VelocityUsersNotWrong={0} out of {1} users can't be wrong!
     * </pre></code> </blockquote>
     * 
     * Template:
     * <blockquote><code><pre>
     * $l10n.format("VelocityUsersNotWrong", ["9", "10"])
     * </pre></code> </blockquote>
     * 
     * Result:
     * <blockquote><code><pre>
     *  9 out of 10 Velocity users can't be wrong!
     * </pre></code></blockquote></p>
     * 
     * @param key  The identifier for the localized text to retrieve,
     * @param args The objects to use as {0}, {1}, etc. when formatting the
     *             localized text.
     * @return     Formatted localized text.
     */
    public String format(String key, List args)
    {
        Object[] array = (args == null) ? null : args.toArray();
        return format(key, array);
    }

    /**
     * Allow us to be able to enable/disable our cross-site scripting filter
     * when rendering something from the format() method. The default is to
     * have it enabled.
     */
    public void setFilterEnabled(boolean v)
    {
        filterEnabled = v;
    }

    /**
     * Whether our cross-site scripting filter is enabled.
     */
    public boolean isFilterEnabled()
    {
        return filterEnabled;
    }

    /**
     * Formats a localized value using the provided objects.
     * Take into account the Browser settings (in order of preference), 
     * the Turbine default settings and the System Locale, 
     * if the Turbine Default Locale is not defined.
     * 
     * @param key  The identifier for the localized text to retrieve,
     * @param args The <code>MessageFormat</code> data used when formatting
     *             the localized text.
     * @return     Formatted localized text.
     * @see        #format(String, List)
     */
    public String format(String key, Object[] args)
    {
        String value = null;
        resolveArgumentTemplates(args);
        try
        {
            // try with the "Browser"-Locale
            Iterator iter = locales.iterator();
            while (value == null && iter.hasNext())
            {
                Locale locale = (Locale) iter.next();
                value = formatKey(key, args, locale);
            }
            /*if (value == null)
            {
                // try with the "Default"-Scope ??? This may be wrong (Hussayn)
                String prefix = getPrefix(null);
                setPrefix(DEFAULT_SCOPE + '.');
                try
                {
                    value = super.format(key, args);
                }
                catch (MissingResourceException itsNotThere)
                {
                    value = createMissingResourceValue(key);
                }
                setPrefix(prefix);
            }*/
        }
        catch (Exception e)
        {
            value = createBadResourceValue(key, e);
        }
        return value;
    }



    /**
     * Provides <code>$l10n.Title</code> to templates, grabbing it
     * from the <code>title</code> property for the current template.
     * 
     * @return The title for the template used in the current request, or
     *         <code>null</code> if title property was not found in
     *         the available resource bundles.
     */
    public String getTitle()
    {
        String title = findProperty(TITLE_PROP);
        
        return title;
    }

    /**
     * Retrieves the localized version of the value of <code>property</code>.
     * 
     * @param property 
     *        The name of the property whose value to retrieve.
     * @return The localized property value.
     */
    protected String findProperty(String property)
    {
        String value = null;

        String templateName = data.getTarget().replace(',', '/');
       

        String l10nKey = property;
        String prefix = getPrefix(templateName + '.');
        if (prefix != null)
        {
            l10nKey = prefix + l10nKey;
        }
        value = get(l10nKey);
        Log.get().debug( "ScarabLocalizationTool: Localized value is '"
              + value
              + '\'');            
        
        return value;
    }

    /**
     * Change the BundlePrefix. Keep the original value for later
     * restore
     * @param prefix
     */
    public void setBundlePrefix(String prefix)
    {
        oldBundlePrefix = bundlePrefix;
        bundlePrefix = prefix;
    }

    /**
     * Restore the old Bundle Prefix to it's previous value.
     */
    public void restoreBundlePrefix()
    {
        bundlePrefix = oldBundlePrefix;
    }


    /**
     * Get the default ResourceBundle name
     */
    protected String getBundleName()
    {
        String name = Localization.getDefaultBundleName();
        return (bundlePrefix == null) ? name : bundlePrefix + name;
    }

    /**
     * Gets the primary locale.
     * The primary locale is the locale which will be choosen
     * at first from the set of Locales which are accepted by the user
     * as defined on the Browser language preferrences.
     * @return The primary locale currently in use.
     */
    public Locale getPrimaryLocale()
    {
        return (locales == null || locales.size() == 0) ? super.getLocale()
                : (Locale) locales.iterator().next();
    }

    // ---- ApplicationTool implementation ----------------------------------

    /**
     * Initialize the tool. Within the turbine pull service this tool is
     * initialized with a RunData. However, the tool can also be initialized
     * with a Locale.
     */
    public void init(Object obj)
    {
        super.init(obj);
        if (obj instanceof RunData)
        {
            data = (RunData) obj;
            locales = getPreferredLocales();
        }
        else if (obj instanceof Locale)
        {
            locales = new ArrayList();
            locales.add(obj);
            locales.add(DEFAULT_LOCALE);
            locales.add(null);
        }
    }

    /**
     * Reset this instance to initial values.
     * Probably needed for reuse of ScarabLocalizationTool Instances.
     */
    public void refresh()
    {
        super.refresh();
        data = null;
        bundlePrefix = null;
        oldBundlePrefix = null;
        locales = null;
        setFilterEnabled(true);
    }


    // ===========================
    // Private utility methods ...
    // ===========================

    /**
     * Utility method: Get a Collection of possible locales 
     * to be used as specified in the Browser settings. Adds
     * the DEFAULT_LOCALE as last resort to the list.
     * Additionally adds a final null to the list. So be prepared
     * to see a null pointer when you iterate through the list.
     * @return
     */
    private List getPreferredLocales()
    {
        List result = new ArrayList(3);
        String localeAsString = getBrowserLocalesAsString();
        LocaleTokenizer localeTokenizer = new LocaleTokenizer(localeAsString);
        while (localeTokenizer.hasNext())
        {
            Locale browserLocale = (Locale) localeTokenizer.next();
            Locale finalLocale = getFinalLocaleFor(browserLocale);
            if (finalLocale != null)
            {
                result.add(finalLocale);
            }
        }
        result.add(DEFAULT_LOCALE);
        result.add(null);
        return result;
    }


    /**
     * Contains a map of Locales which support given
     * browserLocales.
     */
    static private Map supportedLocaleMap   = new HashMap();
    /**
     * Contains a map of Locales which do NOT support given
     * browserLocales.
     */
    static private Map unsupportedLocaleMap = new HashMap();

    /**
     * Return the locale, which will be used to resolve
     * keys of the given browserLocale. This method returns
     * null, when Scarab does not directly support the 
     * browserLocale. 
     * @param browserLocale
     * @return 
     */
    private Locale getFinalLocaleFor(Locale browserLocale)
    {
        Locale result = (Locale) supportedLocaleMap.get(browserLocale);
        if (result == null)
        {
            if (unsupportedLocaleMap.get(browserLocale) == null)
            {
                ResourceBundle bundle;
                try {
                    bundle = ResourceBundle.getBundle(
                             getBundleName(), browserLocale);
                }
                catch (Exception e)
                {
                  // [HD] This should not happen, but it does happen;
                  // The problem raises when the system locale is not
                  // supported by Scarab. This was reported on Windows 
                  // systems. This needs to be further investigated.
                  // setting bundle to null here enforces usage of the
                  // default ResourceBundle (en-US)
                  bundle = null;
                }

                if (bundle != null)
                {
                    Locale finalLocale = bundle.getLocale();
                    String initialLanguage = browserLocale.getLanguage();
                    String finalLanguage = finalLocale.getLanguage();
                    if (initialLanguage.equals(finalLanguage))
                    {
                        result = finalLocale;
                        supportedLocaleMap.put(browserLocale, finalLocale);
                    }
                    else
                    {
                        unsupportedLocaleMap.put(browserLocale, finalLocale);
                    }
                }
                else
                {
                    Log.get().error("ScarabLocalizationTool: ResourceBundle '" + getBundleName()
                            + "' -> not resolved for Locale '"
                            + browserLocale
                            + "'.");
                }
            }
        }
        return result;
    }

    /**
     * Utility method: Get the content of the Browser localizationj settings.
     * Return an empty String when no Browser settings are defined.
     * @param acceptLanguage
     * @param request
     */
    private String getBrowserLocalesAsString()
    {
        String acceptLanguage = LocalizationService.ACCEPT_LANGUAGE;
        HttpServletRequest request = data.getRequest();
        String browserLocaleAsString = request.getHeader(acceptLanguage);
        if (browserLocaleAsString == null)
        {
            browserLocaleAsString = "";
        }
        return browserLocaleAsString;
    }

    /**
     * Utility method: Resolve a given key using the given Locale.
     * If the key can not be resolved, return null
     * @param key
     * @param locale
     * @return
     */
    private String resolveKey(String key, Locale locale)
    {
        String value;
        try
        {
            value = Localization.getString(getBundleName(), locale, key);
        }
        catch (MissingResourceException noKey)
        {
            // No need for logging (already done in base class).
            value = null;
        }
        return value;
    }


    /**
     * Utility method: Resolve a given key using the given Locale and apply 
     * the resource formatter. If the key can not be resolved, 
     * return null
     * @param key
     * @param args
     * @param locale
     * @return
     */
    private String formatKey(String key, Object[] args, Locale locale)
    {
        String value;
        try
        {
            value = Localization.format(getBundleName(), locale, key, args);
        }
        catch (MissingResourceException noKey)
        {
            value = null;
        }
        return value;
    }

    /**
     * Utility method: Resolve $variables placed within the args.
     * Used before actually calling the resourceBundle formatter.
     * @param args
     * @return a cloned args list, or args when
     *         filtering is disabled. If args is null, also
     *         return null.
     */
    private Object[] resolveArgumentTemplates(Object[] args)
    {
        // we are going to allow html text within resource bundles. This
        // avoids problems in translations when links or other html tags
        // would result in an unnatural breakup of the text. We need
        // to apply the filtering here on the arguments which might contain
        // user entered data, if we are going to skip the filtering later.

        Object[] result;
        if (isFilterEnabled() && args != null && args.length > 0)
        {
            result = new Object[args.length];
            for (int i = 0; i < args.length; i++)
            {
                Object obj = args[i];
                // we don't filter Number, because these are sometimes passed
                // to message formatter in order to make a choice. Converting
                // the number to a String will cause error
                if (obj != null)
                {
                    if (!(    (obj instanceof SkipFiltering) 
                           || (obj instanceof Number))
                    )
                    {
                        obj = ReferenceInsertionFilter.filter(obj.toString());
                    }
                }
                result[i] = obj;
            }
        }
        else
        {
            result = args;
        }
        return result;
    }

    /**
     * Utility method: create a Pseudovalue when the key
     * has no resolution at all.
     * @param key
     * @return
     */
    private String createMissingResourceValue(String key)
    {
        String value;
        value = "ERROR! Missing resource ("
                + key
                + ")("
                + Locale.getDefault()
                + ")";
        Log.get().error(
                "ScarabLocalizationTool: ERROR! Missing resource: " + key);
        return value;
    }

    /**
     * Utility method: create a Pseudovalue when the key
     * can not be used as resource key.
     * @param key
     * @param e
     * @return
     */
    private String createBadResourceValue(String key, Exception e)
    {
        String value;
        value = "ERROR! Bad resource (" + key + ")";
        Log.get().error( "ScarabLocalizationTool: ERROR! Bad resource: " + key
                + ".  See log for details.", e);
        return value;
    }


    /**
     * Extract a message from an exception. This method checks, if
     * the exception is Localizable. If so, we now can retrieve the localized exception message.
     * Otherwise we retrieve the standard message via e.getLocalizedMessage().
     * @param e
     * @return 
     * throws NullPointerException if t is <code>null</code>
     */
    public String getMessage(Throwable t)
    {
        String result;            
        if(t instanceof Localizable)
        {    
             result = ((Localizable) t).getMessage(this);
        } 
        else
        {   
            // [HD] note we reuse getLocalizedMessage() in case the exception 
            // coming from a third party library is also localized.
            // [JEROME] After rethinking this, I am not sure that this else {}
            // would work. The intent is nice but the implementation perhaps 
            // naive. As I said in the Localizable javadoc, implementation of 
            // getLocalizedMessage() probably requires the implementation of 
            // an IoC pattern. That means somebody would have to say to the 
            // third party library which locale to use for the localization. 
            // Perhaps register it to the instance, or to a global Localizer, 
            // or anything that the getLocalizedMessage() implementation
            // would use to properly localize. Just calling getLocalizedMessage()
            // wouldn't work without this prior registration, which might be 
            // library dependent.
            result = t.getLocalizedMessage();
        }
        return result;    
    }


}
