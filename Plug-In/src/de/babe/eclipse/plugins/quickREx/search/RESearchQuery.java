/*******************************************************************************
 * Copyright (c) 2006 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.search;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.QuickRExPlugin;
import de.babe.eclipse.plugins.quickREx.objects.REBook;
import de.babe.eclipse.plugins.quickREx.objects.RECategory;
import de.babe.eclipse.plugins.quickREx.objects.RELibraryEntry;

/**
 * @author bastian.bergerhoff
 */
public class RESearchQuery implements ISearchQuery {

  private final String text;

  private final int searchScope;

  private final RESearchResult searchResult;

  public static final int SCOPE_TITLE = 1;

  public static final int SCOPE_RE = 2;

  public static final int SCOPE_TESTTEXT = 4;

  public static final int SCOPE_DESC = 8;

  public static final int SCOPE_SOURCE = 16;

  /**
   * The constructor
   * 
   * @param text the text to search
   * @param searchScope a combination of the scope-constants indicating where to look
   */
  public RESearchQuery(String text, int searchScope) {
    this.text = text;
    this.searchScope = searchScope;
    this.searchResult = new RESearchResult(this);
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
    REBook[] allBooks = QuickRExPlugin.getDefault().getREBooks();
    for (int i = 0; i < allBooks.length; i++) {
      List catList = allBooks[i].getContents();
      for (Iterator iter = catList.iterator(); iter.hasNext();) {
        RECategory currentCat = (RECategory)iter.next();
        RELibraryEntry[] entries = currentCat.getCategoryContents();
        for (int j = 0; j < entries.length; j++) {
          boolean added = false;
          if (include(SCOPE_TITLE)) {
            if (entries[j].getTitle().indexOf(this.text) >= 0) {
              searchResult.addMatchingEntry(entries[j]);
              searchResult.addMatch(new Match(entries[j], 0, 0));
              added = true;
            }
          }
          if (!added && include(SCOPE_RE)) {
            if (entries[j].getRe().indexOf(this.text) >= 0) {
              searchResult.addMatchingEntry(entries[j]);
              searchResult.addMatch(new Match(entries[j], 0, 0));
              added = true;
            }
          }
          if (!added && include(SCOPE_TESTTEXT)) {
            if (entries[j].getTesttext().indexOf(this.text) >= 0) {
              searchResult.addMatchingEntry(entries[j]);
              searchResult.addMatch(new Match(entries[j], 0, 0));
              added = true;
            }
          }
          if (!added && include(SCOPE_DESC)) {
            if (entries[j].getDescription().indexOf(this.text) >= 0) {
              searchResult.addMatchingEntry(entries[j]);
              searchResult.addMatch(new Match(entries[j], 0, 0));
              added = true;
            }
          }
          if (!added && include(SCOPE_SOURCE)) {
            if (entries[j].getSource().indexOf(this.text) >= 0) {
              searchResult.addMatchingEntry(entries[j]);
              searchResult.addMatch(new Match(entries[j], 0, 0));
              added = true;
            }
          }
        }
      }
    }
    monitor.done();
    return Status.OK_STATUS;
  }

  private boolean include(int scopeId) {
    return (searchScope & scopeId) == scopeId;
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchQuery#getLabel()
   */
  public String getLabel() {
    return Messages.getString("search.RESearchQuery.label", new Object[] { text}); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchQuery#canRerun()
   */
  public boolean canRerun() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
   */
  public boolean canRunInBackground() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
   */
  public ISearchResult getSearchResult() {
    return searchResult;
  }

  /**
   * Returns the text that was searched for
   * 
   * @return the text
   */
  public String getText() {
    return this.text;
  }
}