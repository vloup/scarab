package org.tigris.scarab.util.word;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.commons.configuration.Configuration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.Turbine;
import org.tigris.scarab.attribute.StringAttribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentTypePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabException;

/**
 * Support for searching/indexing text
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id$
 */
public class LuceneSearchIndex 
    implements Contextualizable,Initializable,Disposable
{
    private static final String INDEX_PATH = "path";
    private static final String ANALYZER_CLASS = "analyzerClass";
    private static final String ISSUE_ID = "issueId";

    static final String COMMENT = "comment";
    static final String ATTRIBUTE_ID = "attributeId";

    private String applicationRoot;

    private FSDirectory indexDir;
    private IndexWriter writer;
    private Analyzer analyzer;
    private long updateCounter = 0;
    
    private Thread rebuildThread;
    private RebuildMonitor rebuildMonitor;

    public LuceneSearchIndex()
    {
    }

    /**
     *  returns a list of related issue IDs sorted by relevance descending.
     *  Should return an empty/length=0 array if search returns no results.
     */
    public List executeQuery(TextQuery textQuery) 
        throws Exception
    {
        String queryText = textQuery.toString();
        
        Query luceneQuery;
        try
        {
           luceneQuery = new QueryParser("", analyzer).parse(queryText);
        }
        catch (ParseException e)
        {
            throw new ScarabException( L10NKeySet.ExceptionParseError, queryText, e);
        }

        List issueIds = new ArrayList();
        IndexSearcher is = new IndexSearcher(indexDir); 
        try
        {
            Hits hits = is.search(luceneQuery);
            
            for(Iterator hi = hits.iterator(); hi.hasNext();)
            {
                Hit h = (Hit)hi.next();
                issueIds.add( Long.valueOf(h.get(ISSUE_ID)));
            }
        }
        finally
        {
            is.close();
        }        
        return issueIds;
    }

    public void index(Issue issue)
    throws Exception
    {
        Document doc = document(issue);
        synchronized(this)
        {
            update(doc);
            optimize(false);
            flush();
        }
    }

    private void update(Document doc)
    throws Exception
    {    
        writer.updateDocument(new Term( ISSUE_ID, doc.get(ISSUE_ID) ), doc); 
    }

    private void flush() throws CorruptIndexException, IOException
    {
        writer.flush();
    }

    private void optimize(boolean immediately) throws CorruptIndexException, IOException
    {
        if(immediately || updateCounter  % 100 == 0)
        {
            writer.optimize();
        }
        updateCounter++;
    }
    
    private void close() throws CorruptIndexException, IOException
    {
        writer.close();
    }

    private Document document(Issue issue)
            throws TorqueException
    {
        String issueId = issue.getIssueId().toString();
        Document doc = new Document();
        doc.add(new Field( ISSUE_ID, issueId, Field.Store.YES, Field.Index.UN_TOKENIZED ));

        for(Iterator as = issue.getAttachments().iterator(); as.hasNext();)
        {
            Attachment a = (Attachment)as.next();
            if (    a.getTypeId().equals(AttachmentTypePeer.COMMENT_PK)
                && !a.getDeleted()) 
            {
                doc.add(new Field( COMMENT, a.getData(), Field.Store.YES, Field.Index.TOKENIZED ));                
            }
        }
        
        for(Iterator vs = issue.getAttributeValuesMap().values().iterator(); vs.hasNext();)
        {
            AttributeValue v = (AttributeValue)vs.next();
            if(    v instanceof StringAttribute
               && !v.getDeleted())
            {
                doc.add(new Field( ATTRIBUTE_ID + v.getAttributeId().toString(), v.getValue(), Field.Store.YES, Field.Index.TOKENIZED ));
            }
        }
        return doc;
    }

    /**
     * update the index for all entities that currently exist
     * @param rebuildMonitor 
     */
    private void rebuild(RebuildMonitor rebuildMonitor)
        throws Exception
    {        
        Criteria crit = new Criteria()
            .add(IssuePeer.DELETED, false);
        
        List issues = IssuePeer.doSelect(crit);
        rebuildMonitor.setNoIssues(issues.size());
        
        for(Iterator issuei = issues.iterator(); issuei.hasNext();) 
        {            
            Document doc = document((Issue)issuei.next());
            synchronized(this){
                update(doc);                
                rebuildMonitor.issueProcessed();
            }
            
            if(rebuildMonitor.iscancelled()) return;
        }  

        synchronized(this)
        {
            optimize(true);        
            flush();
        }
    }
    
    /**
     * @see org.apache.avalon.framework.context.Contextualizable
     * @avalon.entry key="urn:avalon:home" type="java.io.File"
     */    
    public void contextualize(Context context) throws ContextException
    {
        this.applicationRoot = context.get( "urn:avalon:home" ).toString();
    }    
    
    /**
     * Avalon component lifecycle method
     * Initializes the service by loading default class loaders
     * and customized object factories.
     *
     * @throws InitializationException if initialization fails.
     */
    public void initialize()
        throws Exception
    {
        Configuration cfg = Turbine.getConfiguration().subset("searchindex");
        String indexPath = cfg.getString(INDEX_PATH, null);
        String analyzerClassName = cfg.getString(ANALYZER_CLASS, null);
        
        File absIndexPath = getIndexDir(indexPath, applicationRoot);
        
        indexDir = FSDirectory.getDirectory(absIndexPath, new NativeFSLockFactory(absIndexPath));
        
        boolean createIndex = !IndexReader.indexExists(indexDir);
        
        analyzer = createAnalyzer(analyzerClassName);

        writer = new IndexWriter(indexDir, analyzer, createIndex);
 
        if(createIndex) startRebuild();        
    }

    private Analyzer createAnalyzer(String analyzerClassName)
    {
        Analyzer analyzer;
        try
        {
            analyzer = (Analyzer)Class.forName(analyzerClassName).newInstance();
        }
        catch( Exception e)
        {
            analyzer = new PorterStemAnalyzer();
            Log.get().error("Could not create Lucene Analyzer (" + analyzerClassName + "), using default.", e);
        }
        return analyzer;
    }
    
    private File getIndexDir(String path, String applicationRoot)
    {
        File file = new File(path);
        
        if (applicationRoot != null && !file.isAbsolute())
        {
            return new File(applicationRoot, path).getAbsoluteFile();
        }
        else
        {
            return file.getAbsoluteFile();
        }
    }
    
    public void startRebuild()
    {        
        synchronized(this)
        {
            if(!isRebuildInProgress())
            {
                rebuildMonitor = new RebuildMonitor();
                rebuildThread = new Thread(rebuildMonitor, "RebuildIndex");
                rebuildThread.start();
            }
        }
    }

    public void cancelRebuild()
    {
        try
        {
            synchronized(this)
            {
                if(isRebuildInProgress())
                {
                    rebuildMonitor.cancel();
                    rebuildThread.join(10000);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isRebuildInProgress()
    {
        synchronized(this)
        {
            return rebuildThread!=null && rebuildThread.isAlive();
        }
    }

    public Integer pctRebuildFinished()
    {
        return new Integer(rebuildMonitor.pctFinished());
    }
    
    private class RebuildMonitor implements Runnable
    {
        private String REBUILDING = "Rebuilding of Lucene index ";
        private boolean cancelled = false;
        private int noIssuesProcessed = 0;
        private int noIssues = 1;

        public void run()
        {
            try
            {
                Log.get().info(REBUILDING + "started");
                rebuild(this);
                Log.get().info(REBUILDING + "finished");
            }
            catch (Exception e)
            {
                Log.get().error(REBUILDING + "failed", e);
                throw new RuntimeException(e);
            }            
        }

        public void issueProcessed()
        {
            noIssuesProcessed++;
            
        }

        public void setNoIssues(int noIssues)
        {
            this.noIssues = noIssues;
            
        }

        public void cancel()
        {
            cancelled = true;
        }

        public boolean iscancelled()
        {
            return cancelled;
        }
        
        public int pctFinished()
        {
            return noIssuesProcessed*100/noIssues;
        }
    }

    public void dispose()
    {
        try
        {
            synchronized(this)
            {
                cancelRebuild();
                close();
            }
        } 
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
    }
}
