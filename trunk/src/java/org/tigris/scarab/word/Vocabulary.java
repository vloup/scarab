package org.tigris.scarab.word;

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
import java.util.*;

// Turbine classes
import org.apache.turbine.util.db.Criteria;

// Scarab classes
import org.tigris.scarab.om.*;

/**
  * This class handles vocabulary information for a single issue
  *
  */
public class Vocabulary
{
    private static HashSet ignoredWords;
    private Hashtable words;
    private int pos = 1;

    static
    {
        Criteria crit = new Criteria().add(WordPeer.IGNORE, 1);
        ignoredWords = new HashSet();
        try
        {
            Vector v = WordPeer.doSelect(crit);
            for(int i=0; i<v.size(); i++)
                ignoredWords.add(((Word)v.get(i)).getWord());
        }
        catch (Exception e)
        {
            //anything better than just swallowing the exception?
        }
    }

    public Vocabulary() throws Exception
    {
        this("");
    }

    public Vocabulary(String text) throws Exception
    {
        words = new Hashtable(1+text.length()/5); // should be a good guess?
        add(text);
    }

    /**
     *  used to determine whether a word should not be indexed
     *  should return true for very frequently used word or non-words
     *  or in other cases when apropriate
     *
     */
    protected boolean ignore(String word)
    {
        /* for now only ignore words in the ignore list.
            should _probably_ also ignore non-alpha words i.e. 
            "3.1415" "&*%$#^" etc... */
        return ignoredWords.contains(word);
    }

    /**
     *  indexes more text. this is incremental.
     *
     */
    public void add(String text) throws Exception
    {
        StringTokenizer st = new StringTokenizer(text);
        String tok;
        HashSet newEntries = new HashSet();
        while (st.hasMoreTokens())
        {
            tok = st.nextToken();
            if (!ignore(tok))
            {
                Entry entry = (Entry)words.get(tok);
                if (entry==null)
                {
                    entry = new Entry(tok, pos);
                    words.put(tok, entry);
                    newEntries.add(tok);
                }
                else
                    entry.inc();
            }
            pos++;
        }
        if (!newEntries.isEmpty())
        {
            Criteria crit = new Criteria()
                .addIn(WordPeer.WORD, newEntries.toArray());
            Vector v = WordPeer.doSelect(crit);
            for (int i=0; i<v.size(); i++)
            {
                Word word = (Word)v.get(i);
                Entry entry = (Entry)words.get(word.getWord());
                entry.setRating(word.getRating());
                entry.setNew(false);
            }
        }
    }

    /**
     *  this class represents a vocabulary entry
     *
     */
    class Entry
    {
        private String word;
        private int count;
        private int firstPos;
        private int rating=0;
        private boolean isNew=true;

        public Entry(String word, int firstPos)
        {
            this.word = word;
            this.firstPos = firstPos;
            this.count = 1;
        }

        public void inc()
        {
            this.count++;
        }

        public void setRating(int rating)
        {
            this.rating = rating;
        }

        public void setNew(boolean isNew)
        {
            this.isNew = isNew;
        }
    }
}



