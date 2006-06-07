/*
 * ScarabRenderEngine.java
 *
 * Created on 27. april 2006, 16:18
 *
 */

package org.tigris.scarab.tools.radeox;

import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.engine.BaseRenderEngine;

/**
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public final class ScarabRenderEngine extends BaseRenderEngine implements WikiRenderEngine{
    
    
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    /** Creates a new instance of ScarabRenderEngine */
    public ScarabRenderEngine() {
    }
    
    // Public --------------------------------------------------------
    
    // WikiRenderEngne implementation ----------------------------------------------
    
    public boolean exists(String string) {
        return false;
    }

    public boolean showCreate() {
        return false;
    }

    public void appendLink(StringBuffer buffer, String name, String anchor, String string1) {
        appendLink(buffer, name, anchor);
        if (anchor != null && anchor.length()>0){
            buffer.setLength(buffer.length()-1);
            buffer.append(anchor);
         }
         buffer.append("]");
    }

    public void appendLink(StringBuffer buffer, String name, String anchor) {
         buffer.append("[");
         if (name != null && name.length()>0){
             buffer.append(name);
         }
         if (anchor != null && anchor.length()>0){
             buffer.append(anchor);
         }
         buffer.append("]");
    }

    public void appendCreateLink(StringBuffer buffer, String name, String anchor) {
        appendLink(buffer, name, anchor);
    }
    
    // Y overrides ---------------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------

    
}
