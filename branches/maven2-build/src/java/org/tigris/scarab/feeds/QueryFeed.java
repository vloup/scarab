
package org.tigris.scarab.feeds;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.fulcrum.security.util.TurbineSecurityException;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.util.ScarabUtil;
import org.tigris.scarab.util.word.QueryResult;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.workingdogs.village.DataSetException;

/**
 * Converts a query to an RSS feed.  The private methods are mostly ripped off
 * of ScarabRequestTool and there should be some refactoring done here.
 * 
 * @author Eric Pugh
 *  
 */
public class QueryFeed implements Feed {

    private Query query;
    private List results;
    private ScarabLink scarabLink;
    private String format;
    

    /**
     * Constructs the query feed with the search results. 
     * @param query The searched query
     * @param results The iterator containing the search-results
     * @param scarabToolManager
     * @param scarabLink
     * @param format 
     */
    public QueryFeed(Query query, List results, ScarabLink scarabLink, String format) {
        this.query = query;
        this.results = results;
        this.scarabLink = scarabLink;
        this.format = format;
    }

    public SyndFeed getFeed() throws Exception, TorqueException, DataSetException, TurbineSecurityException
    {
        SyndFeed feed = new SyndFeedImpl();
        feed.setTitle(query.getName());
        String link = scarabLink.setAction("Search").addPathInfo("go",query.getQueryId()).toString();
        feed.setLink(link);
        feed.setDescription(query.getDescription());
        List entries = new ArrayList();
        for(Iterator i = results.iterator(); i.hasNext();)
        {
            SyndEntry entry = new SyndEntryImpl();
            SyndContent description = new SyndContentImpl();
            QueryResult queryResult = (QueryResult)i.next();
            String title = queryResult.getUniqueId();
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
            Iterator avIteratorCSV = queryResult.getAttributeValuesAsCSV().iterator();
            Iterator avIterator = query.getMITList().getAllRModuleUserAttributes().iterator();
            while (avIterator.hasNext())
            {
                String value = (String)avIteratorCSV.next();
                RModuleUserAttribute av = (RModuleUserAttribute)avIterator.next();
                desc = desc + "<b>" + av.getAttribute().getName()+":</b>" + value +"<br/>";
            }
            description.setValue(ScarabUtil.filterNonXml(desc));

            entry.setDescription(description);
            entries.add(entry);
        }
        feed.setEntries(entries);
        feed.setFeedType(format);
        return feed;
    }    
    
}
