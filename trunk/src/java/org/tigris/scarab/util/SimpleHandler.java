package org.tigris.scarab.util;

import java.util.*;
import java.io.*;

import org.tigris.scarab.om.*;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.util.word.*;
import org.tigris.scarab.services.cache.ScarabCache;

import org.apache.log4j.Category;
import org.apache.torque.util.Criteria;

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
     * comments if neccessary. TODO adapt to the various clob sizes for
     * different databases.
     */
    private static final int MAX_COMMENT_SIZE = 2000;

    protected static void log(String str)
    {
        category.info("SimpleHandler: " + str);
    }

    static
    {
        log("loading");
    }

    /**
     * Append a comment to the specified issue. Wrapper method with string
     * parameters.
     * 
     * @param comment
     * @param disableEmailsInt
     * @param issues
     * @param moduleDomain
     * @param user
     * 
     * @return Vector with first item a boolean indicating success.
     * @param issueId
     *            the id of the issue to add the comment to
     * @param username
     *            the username of the user who is adding the comment
     * @param comment
     *            the comment to append to the issue
     */
    public Vector addComment(final String moduleDomain, final String issues,
            final String user, final String comment, final int disableEmailsInt)
    {

        final Vector retValue = new Vector();
        boolean success = false;
        final String c = fixEOLs(comment);
        final boolean disableEmails = (disableEmailsInt != 0);
        log("addComment:  moduleDomain=" + moduleDomain + ", issues=" + issues
                + ", user=" + user + ", comment=\"" + c + ", disableEmails="
                + disableEmailsInt);

        try
        {

            // issues to add comment to
            Set issueSet = new HashSet();

            // find user
            final ScarabUser u = ScarabUserManager
                    .getInstance(user, null /* XXX ??? */);

            // find modules in moduleDomain
            List modules = MITListManager.getAllModulesAllIssueTypesList(u)
                    .getModules();

            for (Iterator it = modules.iterator(); it.hasNext();)
            {
                Module m = (Module) it.next();
                // Parse issues from issues string
                final List issueTokens = IssueIdParser.tokenizeText(m, issues);

                for (Iterator it2 = issueTokens.iterator(); it2.hasNext();)
                {
                    Object o = it2.next();
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
                Issue i = (Issue) it.next();
                success |= addComment(i, u, c, disableEmails);
            }
            retValue.add(Boolean.valueOf(success));
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
        // turn off emailing?
        final boolean originalEmailState = GlobalParameterManager
                .getBoolean(GlobalParameter.EMAIL_ENABLED);

        final BufferedReader reader = new BufferedReader(new StringReader(
                comment));
        final StringBuffer nextComment = new StringBuffer();
        String nextLine = "";
        do
        {
            nextLine = reader.readLine();

            if (nextLine != null
                    && nextComment.length() + nextLine.length() < MAX_COMMENT_SIZE)
            {

                nextComment.append(nextLine + '\n');
            }
            else
            {
                final Attachment attachment = new Attachment();
                attachment.setData(nextComment.toString());
                synchronized (issue)
                {
                    if (disableEmails)
                    {
                        GlobalParameterManager.setBoolean(GlobalParameter.EMAIL_ENABLED,
                                false);
                    }
                    issue.addComment(attachment, user);
                    if (disableEmails)
                    {
                        GlobalParameterManager.setBoolean(GlobalParameter.EMAIL_ENABLED,
                                originalEmailState);
                    }                
                }
                nextComment.setLength(0);
            }

        } while (nextLine != null);

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
            retValue.add(Boolean.valueOf(changeIssueAttributeOption(i, u, a, o,
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