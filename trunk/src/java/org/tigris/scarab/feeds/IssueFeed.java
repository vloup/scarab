
package org.tigris.scarab.feeds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabLink;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;

/**
 * Converts a Issue to an RSS feed.
 * 
 * @todo improve what is shown to a user
 * @author Eric Pugh
 *  
 */
public class IssueFeed implements Feed{

    private Issue issue;

    private ScarabLink scarabLink;
    private ScarabLocalizationTool l10nTool;

    public IssueFeed(Issue issue,ScarabLink scarabLink, ScarabLocalizationTool l10nTool) {
        this.issue = issue;
        this.scarabLink = scarabLink;
        this.l10nTool = l10nTool;
    }

    public SyndFeed getFeed() throws IOException, FeedException, TorqueException, Exception {

        SyndFeed feed = new SyndFeedImpl();
       
        String title = issue.getUniqueId() + ": " + issue.getDefaultText();
        feed.setTitle(title);
        String link = scarabLink.getIssueIdAbsoluteLink(issue).toString();
        feed.setLink(link);
        feed.setDescription(title);

        List entries = new ArrayList();
        List allActivities = issue.getActivity(true);
        

        for (Iterator i = allActivities.iterator(); i.hasNext();) {
            SyndEntry entry;
            SyndContent description;
            
            Activity activity = (Activity)i.next();
            
            ActivitySet activitySet = activity.getActivitySet();
            Date date =activitySet.getCreatedDate();
            entry = new SyndEntryImpl();

            entry.setPublishedDate(date);
            
            description = new SyndContentImpl();
            description.setType("text/html");
            
            String desc =
                  "<b>Description:</b>" + activity.getDisplayName(this.l10nTool) +"<br/>"
                + "<b>New:</b>" + activity.getNewValue(this.l10nTool) +"<br/>"
                + "<b>Old:</b>" + activity.getOldValue(this.l10nTool) +"<br/>"
                + "<b>Reason:</b>" + activitySet.getCommentForHistory(issue) +"<br/>";
            
            entry.setAuthor(activitySet.getCreator().getName());

            description.setValue(desc.toString());            

			String entryTitle = createEntryTitle(activity);
            entry.setTitle(entryTitle);
            
            entry.setDescription(description);
            
            //The hashCode is a cheap trick to have a unique link tag in the RSS feed
            //to help those reader as ThunderBird that use the link to check for new items
            entry.setLink(link+"?hash="+entry.hashCode());
            
            entries.add(entry);
        }
     

        feed.setEntries(entries);
        
        return feed;
    }

	/**
	 * Just a method to isolate the logic for the entry naming.
	 * It is just a substring of the activity description
	 * @param activity the activity we want to create the title for
	 * @return the entry title
	 */
	private String createEntryTitle(Activity activity) {
        String displayName = null;
        try
        {
            displayName=activity.getDisplayName();
        }
        catch( Exception e)
        {
            throw new RuntimeException(e);
        }
		return displayName.substring(0,64);
	}

}
