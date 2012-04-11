package org.tigris.scarab.search;

import java.util.AbstractList;
import org.tigris.scarab.search.CachedQuery;

public class CachedResultList extends AbstractList
{
	private CachedQuery query;
	
	CachedResultList(CachedQuery query)
	{
	    this.query = query;
	    query.cacheInitial();
	}
	
	public int size()
	{
	    return query.totalRowCount();
	}
	
	public Object get(int index)
	{
	    if(index>query.totalRowCount())
	    {
	        throw new IndexOutOfBoundsException();
	    }
	    else if(index>query.cachedRowCount())
	    {
	        query.cacheAll();
		    if(index>query.cachedRowCount())
		    	index = query.cachedRowCount();
	    }
	    return query.getCachedRows().get(index);
	}
}
