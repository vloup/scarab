package org.tigris.scarab.om;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.io.Serializable;
import org.apache.torque.util.Criteria;
import org.tigris.scarab.services.cache.ScarabCache;

// Local classes
import org.tigris.scarab.om.Module;

/** 
 *  You should add additional methods to this class to meet the
 *  application requirements.  This class will only be generated as
 *  long as it does not already exist in the output directory.
 */
public class IssueTemplateInfoPeer 
    extends org.tigris.scarab.om.BaseIssueTemplateInfoPeer
{

    private static final String GET_TEMPLATES = 
        "getTemplates";
    static final String TEMPLATE_PEER = 
        "IssueTemplateInfoPeer";

    // query types
    public static final String TYPE_PRIVATE = "private";
    public static final String TYPE_GLOBAL = "global";
    public static final String TYPE_ALL = "all";

    /**
     * List of Issue Template objects associated with this module.
     * And issue type.
     */
    public static List getTemplates(Module module, IssueType issueType,
                                    ScarabUser user, String sortColumn, 
                                    String sortPolarity, String type)
        throws Exception
    {
        List templates = null;
        Serializable[] key = {TEMPLATE_PEER, GET_TEMPLATES, module, null, 
            issueType, user, sortColumn, sortPolarity, type};
        Object obj = ScarabCache.get(key);
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(IssuePeer.MODULE_ID, module.getModuleId())
                .add(IssuePeer.DELETED, 0)
                .addJoin(ActivitySetPeer.TRANSACTION_ID, 
                         ActivityPeer.TRANSACTION_ID) 
                .addJoin(IssuePeer.ISSUE_ID, 
                         ActivityPeer.ISSUE_ID) 
                .add(IssuePeer.TYPE_ID, issueType.getTemplateId())
                .addJoin(IssueTemplateInfoPeer.ISSUE_ID,
                         IssuePeer.ISSUE_ID);
            crit.setDistinct();

            Criteria.Criterion cGlob = crit.getNewCriterion(
                IssueTemplateInfoPeer.SCOPE_ID, Scope.MODULE__PK,
                Criteria.EQUAL);
            Criteria.Criterion cPriv = crit.getNewCriterion(
                ActivitySetPeer.CREATED_BY, user.getUserId(),  
                Criteria.EQUAL);
            cPriv.and(crit.getNewCriterion(
                IssueTemplateInfoPeer.SCOPE_ID, Scope.PERSONAL__PK, 
                Criteria.EQUAL));

            if (TYPE_PRIVATE.equals(type))
            {
                crit.add(cPriv);
            }
            else if (TYPE_GLOBAL.equals(type))
            {
                crit.add(cGlob);
            }
            else
            {
                // All templates
                cGlob.or(cPriv);
                crit.add(cGlob);
            }
            crit.setDistinct();

            // Add sort criteria
            if (sortColumn.equals("desc"))
            {
                addSortOrder(crit, IssueTemplateInfoPeer.DESCRIPTION, 
                             sortPolarity);
            }
            else if (sortColumn.equals("avail"))
            {
                crit.addJoin(IssueTemplateInfoPeer.SCOPE_ID,
                             ScopePeer.SCOPE_ID);
                addSortOrder(crit, ScopePeer.SCOPE_NAME, sortPolarity);
            }
            else if (!sortColumn.equals("user"))
            {
                // sort by name
                addSortOrder(crit, IssueTemplateInfoPeer.NAME, sortPolarity);
            }
            templates = IssuePeer.doSelect(crit);
            ScarabCache.put(templates, key);
        }
        else 
        {
            templates = (List)obj;
        }
        if (sortColumn.equals("user"))
        {
            templates = sortByCreatingUser(templates, sortPolarity);
        }
        return templates;
    }

    private static Criteria addSortOrder(Criteria crit, 
                       String sortColumn, String sortPolarity)
    {
        if (sortPolarity.equals("desc"))
        {
            crit.addDescendingOrderByColumn(sortColumn);
        }
        else
        {
            crit.addAscendingOrderByColumn(sortColumn);
        }
        return crit;
    }

    private static List sortByCreatingUser(List result,
                                           String sortPolarity)
        throws Exception
    {
        final int polarity = ("asc".equals(sortPolarity)) ? 1 : -1;   
        Comparator c = new Comparator() 
        {
            public int compare(Object o1, Object o2) 
            {
                int i = 0;
                try
                {
                    i = polarity * 
                        ((Issue)o1).getCreatedBy().getFirstName()
                         .compareTo(((Issue)o2).getCreatedBy().getFirstName());
                }
                catch (Exception e)
                {
                    //
                }
                return i;
             }
        };
        Collections.sort(result, c);
        return result;
    }

}
