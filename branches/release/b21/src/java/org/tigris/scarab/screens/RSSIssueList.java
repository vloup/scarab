package org.tigris.scarab.screens;

/* ================================================================
 * Copyright (c) 2006 CollabNet.  All rights reserved.
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
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.torque.TorqueException;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.IteratorWithSize;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.ScarabUtil;
import org.tigris.scarab.util.word.QueryResult;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import com.workingdogs.village.DataSetException;

/**
 * Screen that will act just like IssueList, but will dump the results of the in-session
 * query into a rss feed in the outputstream (no .vm template associated)
 * 
 * @author jorgeuriarte
 *
 */
public class RSSIssueList extends Default
{
    private static final String DEFAULT_FEED_FORMAT = "atom_0.3";
    
    protected void doBuildTemplate(RunData data, TemplateContext context) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLink scarabLink = getScarabLinkTool(context);
        ParameterParser parser = data.getParameters();
        String feedType = parser.getString(RSSDataExport.FEED_TYPE_KEY);
        SyndFeed feed = getQueryFeed(scarabR, scarabLink);
        feed.setFeedType((feedType==null)?RSSDataExport.DEFAULT_FEED_FORMAT:feedType);
        Writer writer = data.getResponse().getWriter();
        outputFilteredXml(feed, writer);
        data.setTarget(null);
    }

    private SyndFeed getQueryFeed(ScarabRequestTool scarabR, ScarabLink scarabLink) throws Exception, TorqueException, DataSetException, TurbineSecurityException
    {
        Query query = scarabR.getQuery();

        boolean showModuleName = !query.getMITList().isSingleModule();
        boolean showIssueType = !query.getMITList().isSingleIssueType();

        IteratorWithSize it = scarabR.getCurrentSearchResults();
        SyndFeed feed = new SyndFeedImpl();
        feed.setTitle(query.getName());
        String link = scarabLink.setAction("Search").addPathInfo("go",query.getQueryId()).toString();
        feed.setLink(link);
        feed.setDescription(query.getDescription());
        List entries = new ArrayList();
        while (it.hasNext())
        {
            SyndEntry entry = new SyndEntryImpl();
            SyndContent description = new SyndContentImpl();
            QueryResult queryResult = (QueryResult)it.next();
            String title = queryResult.getUniqueId();
            if(showModuleName){
                title = title + " ("+ queryResult.getModule().getRealName() + ")";
            }
            if(showIssueType){
                title = title + " ("+ queryResult.getRModuleIssueType().getDisplayName() + ")";
            }
            entry.setTitle(title);
            
            Issue issue = IssueManager.getInstance(Long.valueOf(queryResult.getIssueId()));

            link = scarabLink.getIssueIdAbsoluteLink(issue).toString();
            entry.setLink(link);

            Date publishedDate = null;
            if(issue.getModifiedDate()!= null){
                publishedDate = issue.getModifiedDate();
            }
            else {
                publishedDate = issue.getCreatedDate();
            }
            entry.setPublishedDate(publishedDate);

            description = new SyndContentImpl();
            description.setType("text/html");
            String desc = "";
            List attributeValues = queryResult.getAttributeValuesAsCSV();
            if (null != attributeValues)
            {
                Iterator avIteratorCSV = attributeValues.iterator(); 
                Iterator avIterator = query.getMITList().getAllRModuleUserAttributes().iterator();
                while (avIterator.hasNext())
                {
                    String value = (String)avIteratorCSV.next();
                    RModuleUserAttribute av = (RModuleUserAttribute)avIterator.next();
                    desc = desc + "<b>" + av.getAttribute().getName()+":</b>" + value +"<br/>";
                }
            }
            else
            {
                AttributeValue av = issue.getDefaultTextAttributeValue();
                if (av != null)
                {
                    RModuleAttribute rma = av.getRModuleAttribute();
                    desc = "<b>" + rma.getDisplayValue() + ":</b>" + av.getValue() + "<br/>";                    
                }
            }
                
            description.setValue(ScarabUtil.filterNonXml(desc));

            entry.setDescription(description);
            entries.add(entry);
        }
        feed.setEntries(entries);
        feed.setFeedType(DEFAULT_FEED_FORMAT);
        return feed;
    }
    
    private ScarabLink getScarabLinkTool(TemplateContext context)
    {
        return (ScarabLink)context
            .get(ScarabConstants.SCARAB_LINK_TOOL);
    }
    
    private void outputFilteredXml(SyndFeed feed, Writer writer) throws IOException, FeedException
    {
        SyndFeedOutput output = new SyndFeedOutput();
        try
        {
            output.output(feed, writer);
        }
        catch (FeedException fe)
        {
            // Will retry after filtering
            feed.setDescription(ScarabUtil.filterNonXml(feed.getDescription()));
            output.output(feed, writer);
        }
    }    
}
