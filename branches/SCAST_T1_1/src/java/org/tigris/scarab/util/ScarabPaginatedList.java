package org.tigris.scarab.util;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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

import java.util.List;

/**
 * A container of pagination information.
 *
 * FIXME: Convert zero-based semantics of "page number" to one-based.
 * Properties with zero-based semantics should be named "index", not
 * "number".
 *
 * @author <a href="mailto:tenersen@collab.net">Todd Enersen</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @since Scarab 1.0 b14
 */
public class ScarabPaginatedList
{
    /**
     *  The total size of the list, not just the window which is 
     *  the displayable region. 
     */
    private int totalListSize;
    
    /**
     *  The number of items per page, including the current 
     *  displayable region. 
     */
    private int resultsPerPage; 

    /**
     * The number of the currently displayed page (one-based).
     */
    private int currentPageNumber; 

    /**
     *  The current viewable list window, as a List
     */
    private List window;


    /**
     * Constructor
     */
    public ScarabPaginatedList()
    {
        window = null;
        currentPageNumber = 1;
        resultsPerPage = 0; 
        totalListSize = 0;
    }

    /**
     * Constructor which sets things up for a 'ready' list.
     *
     * @param pageIndex Zero-based index for the current page.
     * @param perPage Number of items shown per page of pagination.
     */
    public ScarabPaginatedList(List l, int size, int pageIndex, int perPage)
    {
        window = l;
        totalListSize = size; 
        setPageNumber(pageIndex);
        resultsPerPage = perPage;
    }

    /**
     *  Method to return the size of the entire list, not 
     *  just the part that is currently 'to be' displayed. 
     */
    public int getTotalListSize()
    {
        return totalListSize;
    }

    /**
     *  Method to return the total number of pages in this list.
     */
    public int getNumberOfPages()
    {
        int r = 0; 

        if (resultsPerPage != 0)
        {
            r = (int)Math.ceil((float)totalListSize / resultsPerPage);
        }

        return r;
    }

    /**
     *  Method to get the current page number. 
     */
    public int getPageNumber()
    {
        return currentPageNumber;
    }

    /**
     *  Method to return the previous page number.
     */
    public int getPrevPageNumber()
    {
        int r = getPageNumber() - 1;
        if (r < 0)
        {
            r = 0;
        }
        return r;
    }

    /**
     *  Method to return the next page number
     */
    public int getNextPageNumber()
    {
        int r = getPageNumber() + 1; 
        if (r > getNumberOfPages())
        {
            r = 0;
        }
        return r;
    }
    
    /**
     *  Method to get the number of results per page. 
     */
    public int getResultsPerPage()
    {
        return resultsPerPage;
    }

    /**
     *  Method to get the portion of the list that is currently
     *  viewable (inside the window)
     */
    public List getList()
    {
        return window;
    }


    /**
     *  Method to set the TotalListSize.
     */
    public void setTotalListSize(int size)
    {
        totalListSize = size; 
    }

    /**
     * Sets the current page number.
     *
     * @param pageIndex The index of the current page (zero-based).
     */
    public void setPageNumber(int pageIndex)
    {
        currentPageNumber = pageIndex + 1;
    }

    /**
     *  Method to set the current list window. 
     */
    public void setList(List list)
    {
        window = list;
    }

    /**
     * @deprecated Use setPageNumber(int) instead.
     */
    public final void setCurrentPageNumber(int pageIndex)
    {
        setPageNumber(pageIndex);
    }
}
