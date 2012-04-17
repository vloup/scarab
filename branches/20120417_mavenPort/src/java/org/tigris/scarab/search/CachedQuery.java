package org.tigris.scarab.search;

import java.util.ArrayList;
import java.util.List;
import org.tigris.scarab.util.Log;

import com.workingdogs.village.Record;

import org.apache.torque.util.Criteria;
import org.apache.torque.util.BasePeer;
import org.apache.torque.TorqueException;

public class CachedQuery {
    private static final char DOT_REPLACEMENT_IN_JOIN_CONDITION = '#';
	
	private static final String LOGGER = "org.apache.torque";

    private Criteria criteria;
    private boolean countOnly = false;
    private List cachedRows;
    private int totalRowCount;
    private CachedResultList results;
    
	public CachedQuery( Criteria criteria, boolean countOnly )
	{
		this.criteria  = criteria;
		this.countOnly = countOnly;
	}

    private static List executeSelect( String sql )
        throws TorqueException
    {
		List result;
		try 
    	{
	        long queryStartTime = System.currentTimeMillis();

		    result = BasePeer.executeQuery(sql);
	        
	        logLongRunningQuery(sql, System.currentTimeMillis() - queryStartTime);
        }
	    catch (TorqueException e)
        {
            Log.get(LOGGER).warn("Search sql:\n" + sql + 
                "\nresulted in an exception: " + e.getMessage());
            throw e;
        }
        return result;
    }       		

    public List getResults()
    {
        if(countOnly)
        	throw new RuntimeException("is countOnly!");

        if(results==null)
    		results = new CachedResultList(this);
    	
    	return results;
    }
    
    public int getRowCount()
    {
    	cacheInitial();
    	return totalRowCount();
    }
	
    public void cacheInitial()
    {
    	if(cachedRows==null)
    	{
    		try
    		{
				if(!countOnly)
	    		{
	    	        criteria.setLimit(-1).setOffset(0);
	    	    	String sql = adjustSelectSql(BasePeer.createQueryString(criteria));
	    		    cachedRows = executeSelect(sql);
	    		}	    		

				if(cachedRows==null) cachedRows = new ArrayList();
	
	            totalRowCount = cachedRows.size(); 
    		
			    if(countOnly)
			    {
	    	        criteria.setLimit(-1).setOffset(0);
	    	    	String sql = adjustSelectSql(BasePeer.createQueryString(criteria));
			        sql = makeCountSql(sql);
	                List countRows = executeSelect(sql);
	                Record countRow = (Record)countRows.get(0);   
	                totalRowCount = countRow.getValue(1).asInt();
			    }
    		}
		    catch(Exception e)
    		{
    			throw new RuntimeException(e);
    		}
    	}
    }

    public void cacheAll()
	{
		if(cachedRows==null)
			cacheInitial();
		
    	if(cachedRowCount() < totalRowCount())
		{
    		try
    		{
		        criteria.setLimit(-1).setOffset(cachedRowCount());
		    	String sql = adjustSelectSql(BasePeer.createQueryString(criteria));
			    cachedRows.addAll(executeSelect(sql));
		    }
			catch(Exception e)
    		{
    			throw new RuntimeException(e);
    		}
	        
		}
	}

	public List getCachedRows()
	{
		return cachedRows;
    }
    
	public int cachedRowCount()
	{
		return cachedRows.size();
	}

	public int totalRowCount()
	{
		return totalRowCount;
	}

	private static String adjustSelectSql(String sql)
    {
        return sql.replace(DOT_REPLACEMENT_IN_JOIN_CONDITION, '.');
    }

    private static String makeCountSql(String sql)
    {
        return "SELECT COUNT(*) FROM ( " + sql + " ) SEARCH_RESULT";
    }

    private static void logLongRunningQuery(String sql, long time)
    {
        if (time > 500) 
            Log.get(LOGGER).warn("Long running query:\n" + sql + "\nTime = " + time + " ms");
    }
}
