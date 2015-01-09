package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
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

// JDK classes
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * This class deals with AttributeOptions. For more details
 * about the implementation of this class, read the documentation
 * about how Scarab manages Attributes.
 * <p>
 * The implementation of this class is "smart" in that it will only
 * touch the database when it absolutely needs to. For example, if
 * you create a new AttributeOption, it will not query the database
 * for the parent/child relationships until you ask it to. It will then
 * cache the information locally.
 * <p>
 * All instances of AttributeOptions are cached using the 
 * TurbineGlobalCache service.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id$
 */
public class AttributeOption 
    extends BaseAttributeOption
    implements Persistent
{

    /**
     * Storage for ID's of the children of this AttributeOption
     */
    private List sortedChildren = null;

    /**
     * Must call getInstance()
     */
    protected AttributeOption()
    {
    }

    /**
     * A comparator for this class. Compares on OPTION_NAME.
     */
    private static final Comparator COMPARATOR = new Comparator()
        {
            public int compare(Object obj1, Object obj2)
            {
                String name1 = ((AttributeOption)obj1).getName();
                String name2 = ((AttributeOption)obj2).getName();
                return name1.compareTo(name2);
            }
        };

    /**
     * Compares numeric value and in cases where the numeric value
     * is the same it compares the display values.
     */
    public static Comparator getComparator()
    {
        return COMPARATOR;
    }

    /**
     * Returns a list of AttributeOptions which are ancestors
     * of this AttributeOption. An Ancestor is the parent tree
     * going up from this AO. The order is bottom up.
     */
    public List getAncestors()
        throws TorqueException
    {
        List options = new ArrayList();
        addAncestors(options);
        return options;
    }

    /**
     * Recursive method that loops over the ancestors
     */
    private void addAncestors(List ancestors)
        throws TorqueException
    {
        List parents = getParents();
        for (int i=parents.size()-1; i>=0; i--) 
        {
            AttributeOption parent = (AttributeOption) 
                parents.get(i);
            if (!ancestors.contains(parent)) 
            {
                ancestors.add(parent);    
                parent.addAncestors(ancestors);
            }
        }
    }

    /**
     * Returns a list of AttributeOptions which are descendants
     * of this AttributeOption. The descendants is the child tree
     * going down from this AO. The order is bottom up.
     */
    public List getDescendants()
        throws TorqueException
    {
        List options = new ArrayList();
        addDescendants(options);
        return options;
    }

    /**
     * Recursive method that loops over the descendants
     */
    private void addDescendants(List descendants)
        throws TorqueException
    {
        List children = getChildren();
        for (int i=children.size()-1; i>=0; i--)
        {
            AttributeOption child = (AttributeOption)
                children.get(i);
            descendants.add(child);
            child.addDescendants(descendants);
        }
    }

    /**
     * Returns a list of AttributeOption's which are children
     * of this AttributeOption.
     */
    public List getChildren()
        throws TorqueException
    {
        if (sortedChildren == null)
        {
            buildChildren();
        }
        return sortedChildren;
    }

    /**
     * Returns a list of AttributeOption's which are parents
     * of this AttributeOption.
     */
    public List getParents()
        throws TorqueException
    {
        List sortedParents = (List)AttributeOptionManager.getMethodResult().get(
            this, AttributeOptionManager.GET_PARENTS
        );
        if ( sortedParents == null )
        {
            sortedParents = buildParents();
            AttributeOptionManager.getMethodResult().put(
                sortedParents, this, AttributeOptionManager.GET_PARENTS
            );
        }
        return sortedParents;
    }

    /**
     * Builds a list of AttributeOption's which are children
     * of this AttributeOption.
     */
    private synchronized void buildChildren()
        throws TorqueException
    {
        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, OptionRelationship.PARENT_CHILD)
            .add(ROptionOptionPeer.OPTION1_ID, super.getOptionId())
            .addAscendingOrderByColumn(ROptionOptionPeer.PREFERRED_ORDER);

        List relations = ROptionOptionPeer.doSelect(crit);
        sortedChildren = new ArrayList(relations.size());
        for (int i=0; i < relations.size(); i++)
        {
            ROptionOption relation = (ROptionOption)relations.get(i);
            Integer key = relation.getOption2Id();
            if (key != null)
            {
                sortedChildren.add(relation.getOption2Option());
            }
        }
        sortChildren();
    }

    /**
     * Builds a list of AttributeOption's which are parents
     * of this AttributeOption.
     */
    private synchronized List buildParents()
        throws TorqueException
    {
        Criteria crit = new Criteria()
            .add(ROptionOptionPeer.RELATIONSHIP_ID, 
                 OptionRelationship.PARENT_CHILD)
            .add(ROptionOptionPeer.OPTION2_ID,
                 super.getOptionId());
        List relations = ROptionOptionPeer.doSelect(crit);

        List sortedParents = new ArrayList(relations.size());
        for (int i=0; i < relations.size(); i++)
        {
            ROptionOption relation = (ROptionOption)relations.get(i);
            Integer key = relation.getOption1Id();
            if (key != null)
            {
                sortedParents.add(relation.getOption1Option());
            }
        }
        Collections.sort(sortedParents, getComparator());

        return sortedParents;
    }

    /**
     * re-sorts the children
     */
    public void sortChildren()
    {
        synchronized (this)
        {
            Collections.sort(sortedChildren, getComparator());
        }
    }

    /**
     * Checks to see if this Attribute option is a child of
     * the passed in AttributeOption parent.
     */
    public boolean isChildOf(AttributeOption parent)
        throws TorqueException
    {
        return getParents().contains(parent);
    }

    /**
     * Checks to see if this Attribute option is a parent of
     * the passed in AttributeOption child.
     */
    public boolean isParentOf(AttributeOption child)
        throws TorqueException
    {
        return getChildren().contains(child);
    }
    
    /**
     * Does this AttributeOption have children?
     */
    public boolean hasChildren()
        throws TorqueException
    {
        return getChildren().size() > 0 ? true : false;
    }

    /**
     * Does this AttributeOption have parents?
     */
    public boolean hasParents()
        throws TorqueException
    {
        return getParents().size() > 0 ? true : false;
    }

    /**
     * Returns direct parent of this child if only one parent exists.
     */
    public AttributeOption getParent()
        throws TorqueException
    {
        AttributeOption parent = null;
        List parents = getParents();
        if (parents.size() == 1)
        {
           parent = (AttributeOption) parents.get(0);
        }
        return parent;
    }

    /**
     * Delete mappings with all modules and issue types.
     */
    public void deleteModuleMappings()
        throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(RModuleOptionPeer.OPTION_ID, getOptionId());
        RModuleOptionPeer.doDelete(crit);
        ScarabCache.clear();
    }

    /**
     * Delete mappings with global issue types.
     */
    public void deleteIssueTypeMappings()
        throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(RIssueTypeOptionPeer.OPTION_ID, getOptionId());
        RIssueTypeOptionPeer.doDelete(crit);
        ScarabCache.clear();
    }

    /**
     * A String representation of this object.
     */
    public String toString()
    {
        try
        {
            return "Id: " + getOptionId() + " Name: " + getName();// + " ParentIds: " + getParentIds(); 
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all the global issue type mappings for this attribute option.
     */
    private List getIssueTypesWithMappings()
        throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(RIssueTypeOptionPeer.OPTION_ID, getOptionId());
        crit.addJoin(RIssueTypeOptionPeer.ISSUE_TYPE_ID, 
                     IssueTypePeer.ISSUE_TYPE_ID);
        return IssueTypePeer.doSelect(crit);
    }


    /**
     * Checks if this attribute option is associated with atleast one of the
     * global issue types that is system defined.
     *
     * @return True if it is associated with a System defined
     *  global Issue Type. False otherwise.
     */

    public boolean isSystemDefined()
        throws TorqueException
    {
        boolean systemDefined = false;
        List issueTypeList = getIssueTypesWithMappings();
        for (Iterator i = issueTypeList.iterator(); 
             i.hasNext() && !systemDefined;)
        {
            systemDefined = ((IssueType)i.next()).isSystemDefined();
        }
        return systemDefined;
    }
}
