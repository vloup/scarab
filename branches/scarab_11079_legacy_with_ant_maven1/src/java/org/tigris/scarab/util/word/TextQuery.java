package org.tigris.scarab.util.word;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tigris.scarab.om.AttributeValue;

public class TextQuery
{

    private List attrClauses = new ArrayList();
    private List commentClauses = new ArrayList();
    private boolean and;

    public String toString()
    {
        String query = "";
        
        String connection;
        if(and) 
            connection = "+";
        else 
            connection = "";

        for(Iterator i = commentClauses.iterator();i.hasNext();)
        {
            String clause = (String)i.next();
            query = query + connection + LuceneSearchIndex.COMMENT + ":(" + clause + ")";
        }
        for(Iterator i = attrClauses.iterator();i.hasNext();)
        {
            AttributeValue clause = (AttributeValue)i.next();
            query = query + connection + LuceneSearchIndex.ATTRIBUTE_ID + clause.getAttributeId().toString() + ":(" + clause.getValue() + ")";                
        }
        return query;
    }

    public void addAttrClause(AttributeValue clause)
    {
        attrClauses.add(clause);
    }

    public void addAttrClauses(List clauses)
    {
            attrClauses.addAll(clauses);        
    }

    public void addCommentClause(String clause)
    {
        if(clause!=null && !clause.equals(""))
            commentClauses .add(clause); 
    }

    public boolean isEmpty()
    {
        return attrClauses.size()==0 && commentClauses.size()==0;
    }

    public void setAnd(boolean and)
    {
        this.and = and;
        
    }

    public boolean isAnd()
    {
        return and;
    }
}
