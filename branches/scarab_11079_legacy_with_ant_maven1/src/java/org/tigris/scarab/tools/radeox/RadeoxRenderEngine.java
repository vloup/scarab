/*
 * ScarabRenderEngine.java
 *
 * Created on 27. april 2006, 16:18
 *
 */

package org.tigris.scarab.tools.radeox;

import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;
import org.tigris.scarab.tools.render.ScarabRenderAPI;

/** renderEngine implementation for scarab.
 *
 * renders links
 * renders newlines as newlines (replaces "\n[^\n]" with "\n\n")
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @author <a href="mailto:hussayn.dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id$
 */
public final class RadeoxRenderEngine extends BaseRenderEngine implements WikiRenderEngine, ScarabRenderAPI{

    private static RenderContext context = new BaseRenderContext();

    /** Creates a new instance of ScarabRenderEngine */
    public RadeoxRenderEngine() {
        context.setRenderEngine(this);
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

    @Override
    public String render(String text) {
        return render(text, context);
    }

    /* discarded (breaks creole-1.0 syntax)
       The effect will be added as configurable Radeox-Filter instead
    public String render(final String content, final RenderContext context) {

        // Issue SCB2552:  (Preserve entered newlines when rendering wiki style)
        // [HD]: Corrected the regular expression. The previous version 
        //       sucked up the first character of the next line.
        return super.render(content.replaceAll("\n([^\n])", "\n\n$1"), context);
    }
    */
    
    
}
