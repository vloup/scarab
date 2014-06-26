package org.tigris.scarab.tools.bliki;

import org.tigris.scarab.tools.render.ScarabRenderAPI;

import info.bliki.wiki.model.WikiModel;

/*
 * ScarabRenderEngine.java
 *
 * Created on 27. april 2006, 16:18
 *
 */


/** renderEngine implementation for scarab.
 *
 * renders links
 * renders newlines as newlines (replaces "\n[^\n]" with "\n\n")
 *
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @author <a href="mailto:hussayn.dabbous@saxess.com">Hussayn Dabbous</a>
 * @version $Id$
 */
public final class BlikiRenderEngine implements ScarabRenderAPI {
     
    /** Creates a new instance of ScarabRenderEngine */
    public BlikiRenderEngine() 
    {
    }
    
    @Override
    public String render(String text) 
    {
        String match   = "([^\"'=]|^)((http|ftp)s?://(%[\\p{Digit}A-Fa-f][\\p{Digit}A-Fa-f]|[-_.!~*';/?:@#&=+$,\\p{Alnum}])+)";
        String replace = "$1<span class=\"nobr\"><a target=\"_blank\" href=\"$2\">$2</a></span>";

        String result = WikiModel.toHtml(text);
        result = result.replaceAll("\\\\\\\\", "<br/>");
        result = result.replaceAll(match, replace);
        result = result.replaceAll("(?s)\\{\\{\\{(.*)\\}\\}\\}", "<pre>$1</pre>");
        return result;
    }
    
}
