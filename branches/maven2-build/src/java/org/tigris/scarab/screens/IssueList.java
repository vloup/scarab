package org.tigris.scarab.screens;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;

import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * The sole purpose of this class is to generate a page
 * title for the Issue List screen. If the source query
 * for this screen has a name, it is displayed in the
 * page title.
 * @author <a href="mailto:p.ledbrook@cacoethes.co.uk">Peter Ledbrook</a>
 */
public class IssueList extends Default {

    protected void doBuildTemplate(RunData data, TemplateContext context)
    throws Exception
    {
        super.doBuildTemplate(data, context);
        populateContextWithQueryResults(data, context);
        
    }

    private void populateContextWithQueryResults(RunData data, TemplateContext context)
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        List searchResults = scarabR.getCurrentSearchResults();
        int searchResultsSize = searchResults.size();
        int resultsPerPage = data.getParameters().getInt("resultsPerPage", 25);
        int pageNum = data.getParameters().getInt("pageNum", 1 );
        boolean paginated = resultsPerPage > 0 && searchResultsSize > resultsPerPage;
        
        if(paginated)
        {
            searchResults = scarabR.getPaginatedList(searchResults, pageNum, resultsPerPage);
            
        }
        int isueListSize = searchResults.size();
        
        context.put("queryResults", searchResults );
        context.put("pageNum", new Integer(pageNum));
        context.put("totalCount", new Integer(searchResultsSize));
        context.put("isueListSize", new Integer(isueListSize));
        context.put("paginated", new Boolean(paginated));
        context.put("resultsPerPage", new Integer(resultsPerPage));        
    }
    
    /**
     * Returns a localised title for the issue list screen,
     * which includes the source query name if there is one.
     */
    protected String getTitle(ScarabRequestTool scarabR,
                              ScarabLocalizationTool l10n)
        throws Exception
    {
        if (scarabR.getQuery() != null
            && StringUtils.isNotBlank(scarabR.getQuery().getName()))
        {
            return l10n.format("IssueList.vm.TitleWithQueryName",
                               scarabR.getQuery().getName());
        }
        else
        {
            return super.getTitle(scarabR,l10n);
        }
    }
}
