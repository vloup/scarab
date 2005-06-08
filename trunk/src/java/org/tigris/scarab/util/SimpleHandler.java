package org.tigris.scarab.util;

import java.util.*;
import java.io.*;

import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.MITListManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserManager;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.util.word.IssueSearchFactory;
import org.tigris.scarab.util.word.QueryResult;

import org.apache.log4j.Category;
import org.apache.turbine.Turbine;

/**
 * Provides a basic API for XML-RPC requests to the Scarab server.
 * It makes the server side for scarab-post-commit.py script (in the
 * extensions/script directory).
 * 
 * @author Mick
 * @version $Id$
 * @see org.tigris.scarab.util.SimpleHandlerClient
 */
public class SimpleHandler
{
    private static final Category category = Category
            .getInstance(SimpleHandler.class);

    /**
     * Maximum number of characters to put into one comment, add multiple
     * comments if neccessary. Really only applies to oracle databases where 
     * varchar2 has a limit of 4000, and has to be used because oracle's clob/blob 
     * doesn't work w/ torque. <br/>
     * <b>
     * A value of zero means no limit of comment size.
     * </b>
     */
    private static final int MAX_COMMENT_SIZE;

    protected static void log(String str)
    {
        category.info("SimpleHandler: " + str);
    }

    static
    {
        log("loading");
	final String db = Turbine.getConfiguration().getString("scarab.database.type");
        MAX_COMMENT_SIZE = (db.equalsIgnoreCase("oracle")) ? 4000 : 0;
    }

    /**
     * Append a comment to the specified issue. Wrapper method with string
     * parameters.
     *
     * @return Vector with first item a boolean indicating success.
     * @param issues
     *            string with the ids of the issue to add the comment to
     *            (can contain other words which will simply be ignored)
     * @param user
     *            the username of the user who is adding the comment
     * @param comment
     *            the comment to append to the issue
     * @param disableEmailsInt
     *            do we want this change to trigger an email to every person assigned a role.     
     */
    public Vector addComment( final String issues,
            final String user, final String comment, final int disableEmailsInt)
    {

        final Vector retValue = new Vector();
        boolean success = false;
        final String c = fixEOLs(comment);
        final boolean disableEmails = (disableEmailsInt != 0);
        log("addComment:  issues=" + issues
                + ", user=" + user + ", comment=\"" + c + ", disableEmails="
                + disableEmailsInt);

        try
        {

            // issues to add comment to
            final Set issueSet = new HashSet();

            // find user
            final ScarabUser u = ScarabUserManager
                    .getInstance(user, null /* XXX ??? */);

            // find modules in moduleDomain
            List modules = MITListManager.getAllModulesAllIssueTypesList(u)
                    .getModules();

            for (Iterator it = modules.iterator(); it.hasNext();)
            {
                final Module m = (Module) it.next();
                // Parse issues from issues string
                final List issueTokens = IssueIdParser.tokenizeText(m, issues);

                for (Iterator it2 = issueTokens.iterator(); it2.hasNext();)
                {
                    final Object o = it2.next();
                    // find issue
                    if (o instanceof List && ((List) o).get(1) != null
                            && ((List) o).get(1) instanceof String)
                    {
                        final Issue i = Issue.getIssueById((String) ((List) o)
                                .get(1));
                        issueSet.add(i);
                    }
                }
            }
            // now add the comments to each issue found
            for (Iterator it = issueSet.iterator(); it.hasNext();)
            {
                final Issue i = (Issue) it.next();
                success |= addComment(i, u, c, disableEmails);
            }
            retValue.add(new Boolean(success));
        }
        catch (RuntimeException e)
        {
            retValue.add(Boolean.FALSE);
            retValue.add(e);
            e.printStackTrace();
            throw e;
        }
        catch (Exception e)
        {
            retValue.add(Boolean.FALSE);
            retValue.add(e);
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * Append a comment to the specified issue.
     * 
     * @return success.
     * @param issueId
     *            the id of the issue to add the comment to
     * @param username
     *            the username of the user who is adding the comment
     * @param comment
     *            the comment to append to the issue
     */
    protected boolean addComment(final Issue issue, final ScarabUser user,
            final String comment, final boolean disableEmails) throws Exception
    {

        log("adding comment to " + issue.getIssueId());
        // turn off emailing? we need to remember original state anyway...
        final boolean originalEmailState = GlobalParameterManager
                .getBoolean(GlobalParameter.EMAIL_ENABLED);
        // buffer the comment so we can break it down if MAX_COMMENT_SIZE != 0
        final BufferedReader reader = new BufferedReader(new StringReader(
                comment));
        // The comment Attachments that will be added to the issue.
        // This will only contain one Attachment when MAX_COMMENT_SIZE == 0
        final List comments = new LinkedList();
        final StringBuffer nextComment = new StringBuffer();
        String nextLine = "";
        do
        {
            nextLine = reader.readLine();

            if (nextLine == null || 
                  ( MAX_COMMENT_SIZE > 0 
                      && nextComment.length() + nextLine.length() > MAX_COMMENT_SIZE ) )
            {
                final Attachment attachment = new Attachment();
                attachment.setData(nextComment.toString());
                comments.add(0, attachment); // reverse order the list, it looks better on issue page.
                nextComment.setLength(0);
            }
            if (nextLine != null)
            {
                nextComment.append(nextLine + '\n');
            }

        } while (nextLine != null);

        // actually add the comment Attachments to issue
        synchronized (issue)
        {
            if (disableEmails)
            {
                GlobalParameterManager.setBoolean(GlobalParameter.EMAIL_ENABLED, false);
            }
            for( Iterator it = comments.iterator(); it.hasNext(); )
            {
		Attachment attachment = (Attachment)it.next();
		if( attachment.getData() != null && attachment.getData().length() >0 )
		{
                	issue.addComment(attachment, user);
 	 	}
	    }
            if (disableEmails)
            {
                GlobalParameterManager.setBoolean(GlobalParameter.EMAIL_ENABLED,
                          originalEmailState);
            }                
        }

        // [TODO] It isn't neccessary to do all of these, which one is the correct one?!?
        ScarabCache.clear();
        IssueManager.getMethodResult().clear();
        ModuleManager.getMethodResult().clear();

        return true;
    }

    /**
     * Update Issue's attribute option with a given user and description.
     * Wrapper method with string parameters.
     * 
     * @return Vector with first item a boolean indicating success.
     */
    public Vector changeIssueAttributeOption(final String issue,
            final String user, final String attribute, final String option,
            final String description)
    {

        log("changeIssueAttributeOption:  issue=" + issue + ", user=" + user
                + ", attribute=\"" + attribute + "\"" + ", option=\"" + option
                + "\"" + ", description=\"" + description + "\"");

        final Vector retValue = new Vector();
        try
        {
            // find issue
            final Issue i = Issue.getIssueById(issue);
            // find user
            final ScarabUser u = ScarabUserManager
                    .getInstance(user, null /* XXX ??? */);
            // find attribute
            final Attribute a = Attribute.getInstance(attribute);
            // find attributeOption
            final AttributeOption o = AttributeOption.getInstance(a, option);
            // proper method call
            
            retValue.add(new Boolean(changeIssueAttributeOption(i, u, a, o,
                    description)));
        }
        catch (RuntimeException e)
        {
            retValue.add(Boolean.FALSE);
            retValue.add(e);
            e.printStackTrace();
            throw e;
        }
        catch (Exception e)
        {
            retValue.add(Boolean.FALSE);
            retValue.add(e);
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * Update Issue's attribute option with a given user and description
     * 
     * @return success.
     */
    protected boolean changeIssueAttributeOption(final Issue issue,
            final ScarabUser user, final Attribute attribute,
            final AttributeOption option, final String description)
            throws Exception
    {

        final AttributeValue status = issue.getAttributeValue(attribute);
        final AttributeValue nyStatus = AttributeValue.getNewInstance(
                attribute, issue);
        nyStatus.setOptionId(option.getOptionId());
        nyStatus.setActivityDescription(description);
        final HashMap newAttVals = new HashMap();
        newAttVals.put(status.getAttributeId(), nyStatus);
        final ActivitySet activitySet = issue.setAttributeValues(null,
                newAttVals, null, user);
        return true;
    }

    public Vector findIssuesWithAttributeValue(final String user,
            final String attribute, final String value)
    {

        log("findIssuesWithAttributeValue:  user=" + user + ", attribute=\""
                + attribute + "\"" + ", value=\"" + value + "\"");

        final Vector retValue = new Vector();
        try
        {

            // find user
            final ScarabUser u = ScarabUserManager
                    .getInstance(user, null /* XXX ??? */);
            // find attribute
            final Attribute a = Attribute.getInstance(attribute);
            // proper method call
            retValue.add(findIssuesWithAttributeValue(u, a, value));
        }
        catch (RuntimeException e)
        {
            retValue.add(null);
            retValue.add(e);
            e.printStackTrace();
            throw e;
        }
        catch (Exception e)
        {
            retValue.add(null);
            retValue.add(e);
            e.printStackTrace();
        }
        return retValue;
    }

    protected Vector findIssuesWithAttributeValue(final ScarabUser user,
            final Attribute attribute, final String value) throws Exception
    {

        final Vector retValue = new Vector();
        final IssueSearch search = IssueSearchFactory.INSTANCE.getInstance(
                MITListManager.getAllModulesAllIssueTypesList(user), user);
        final AttributeValue av = AttributeValue.getNewInstance(attribute,
                search);
        av.setValue(value);
        search.addAttributeValue(av);
        final Iterator queryresults = search.getQueryResults();

        while (queryresults.hasNext())
        {
            final QueryResult qr = (QueryResult) queryresults.next();
            retValue.add(qr.getIssueId());
            //log(" Adding to results "+qr.getIssueId());
        }

        // close search
        search.close();
        IssueSearchFactory.INSTANCE.notifyDone();
        // return matching issues
        return retValue;
    }

    /**
     * Hack to replace the string "\n" with EOL characters...
     * string.replaceAll("\\n","\n") does not work.
     */
    private static String fixEOLs(final String str)
    {
        final int idx = str.indexOf("\\n");
        if (idx != -1)
        {
            return str.substring(0, idx) + "\n"
                    + fixEOLs(str.substring(idx + "\\n".length()));
        }
        else
        {
            return str;
        }
    }

}
