package org.tigris.scarab.workflow;

import java.util.ArrayList;
import java.util.List;

import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.Transition;

/* ====================================================================
*
* Copyright (c) 2006 CollabNet.
*
* Licensed under the
*
*     CollabNet/Tigris.org Apache-style license (the "License");
*
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://scarab.tigris.org/LICENSE
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
* implied. See the License for the specific language governing
* permissions and limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of CollabNet.
*
*
*/

/**
 * The Transitions available for Attributes span a recursive TransitionTree.
 * A TransitionNode is one node in the TransitionTree. It contains a pointer
 * to a source AttributeOption of a Transition and a List of TransitionNodes 
 * which contain target AttributeOptions which can be reached from
 * this AttributeOption.
 * @author hussayn.dabbous@saxess.de
 */

public class TransitionNode
{
    private AttributeOption fromOption;
    List children;
    TransitionNode parent;
    
    static final Integer BLANK  = new Integer(0);
    static final Integer SINGLE = new Integer(1);
    static final Integer FIRST  = new Integer(2);
    static final Integer INTER  = new Integer(3);
    static final Integer LAST   = new Integer(4);
    static final Integer PASS   = new Integer(5);
    
    TransitionNode(AttributeOption option)
    {
        fromOption = option;
        children = new ArrayList();
        parent = null;
    }
    
    public void addNode(TransitionNode node)
    {
        children.add(node);
        node.setParent(this);
    }
    
    
    public TransitionNode addNode(Transition transition)
    {
        AttributeOption toOption = transition.getTo();
        TransitionNode child = new TransitionNode(toOption);
        addNode(child);
        return child;
    }

    private void setParent(TransitionNode node)
    {
        parent = node;
    }
    
    public TransitionNode getParent()
    {
        return parent;
    }
    
    public AttributeOption getOption()
    {
        return fromOption;
    }
    
    public List getChildren()
    {
        return children;
    }
    
    public List createRows()
    {
        List rows        = new ArrayList();
        List currentRow  = new ArrayList();
        return fillRows(rows, currentRow);
    }
    
    private List fillRows(List rows, List currentRow)
    {
        currentRow.add(getOption());
        if(children.size() > 0)
        {
            int size = children.size();
            for(int index=0; index < size; index++)
            {
                TransitionNode child = (TransitionNode)children.get(index);
                if (size == 1)
                {
                    currentRow.add(SINGLE);
                }
                else
                {
                    if (index==0)
                    {
                        currentRow.add(FIRST);
                    }
                    else if (index == size-1)
                    {
                        currentRow.add(LAST);
                    }
                    else
                    {
                        currentRow.add(INTER);
                    }
                }
                child.fillRows(rows, currentRow);
                if(index < (size-1))
                {
                    currentRow = createNewRow();
                }
            }
        }
        else
        {
            rows.add(currentRow);            
        }
        return rows;
    }

    private List createNewRow()
    {
        List result;
        TransitionNode parent = getParent();
        if(parent != null)
        {
            result = parent.createNewRow();
            Integer type = getConnectionType();
            result.add(type);
        }
        else
        {
            result = new ArrayList();                     
        }
        result.add("");
        return result;
    }
    
    private Integer getConnectionType()
    {
        TransitionNode parent = getParent();
        int parentSize = parent.getChildren().size();
        Integer result;

        if(parentSize == 1)
        {
            result = BLANK;
        }
        else
        {
            int index = 0;
            for (index = 0; index < parentSize; index++)
            {
                TransitionNode node = (TransitionNode) parent.getChildren()
                        .get(index);
                if (node == this)
                {
                    break;
                }
            }

            if (index == 0)
            {
                result = PASS;
            }
            else if (index == (parentSize - 1))
            {
                result = BLANK;
            }
            else
            {
                result = PASS;
            }
        }
        return result;
    }
    
    public int getTreeDepth()
    {
        int result = 0;
        for(int index=0; index < children.size(); index++)
        {
            TransitionNode child = (TransitionNode)children.get(index);
            int childDepth = child.getTreeDepth();
            if(childDepth >= result)
            {
                result = childDepth + 1;
            }
        }
        return result;
    }
    
}
