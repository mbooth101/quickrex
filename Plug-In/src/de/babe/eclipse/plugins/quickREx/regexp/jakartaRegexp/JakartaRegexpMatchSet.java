/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.jakartaRegexp;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSet;

/**
 * MatchSet using Jakarta-Regexp-regular expressions.
 * 
 * @author bastian.bergerhoff
 */
public class JakartaRegexpMatchSet implements MatchSet {

  private final RE re;
  private final String text;

  private final static Collection flags = new Vector();
  private int lastEndIndex = 0;

  static {
    flags.add(JakartaRegexpFlag.MATCH_IGNORE_CASE);
    flags.add(JakartaRegexpFlag.MATCH_MULTILINE);
  }

  /**
   * Returns a Collection of all Compiler-Flags the Jakarta-Regexp-implementation knows about.
   * 
   * @return a Collection of all Compiler-Flags the Jakarta-Regexp-implementation knows about
   */
  public static Collection getAllFlags() {
    return flags;
  }

  /**
   * The constructor - uses the Jakarta-Regexp regular expressions to evaluate the passed regular expression against the passed
   * text.
   * 
   * @param regExp
   *          the regular expression
   * @param text
   *          the text to evaluate regExp against
   * @param flags
   *          a Collection of Flags to pass to the Compiler
   */
  public JakartaRegexpMatchSet(String regExp, String text, Collection flags) {

    try {
      int iFlags = 0;
      for (Iterator iter = flags.iterator(); iter.hasNext();) {
        Flag element = (Flag) iter.next();
        iFlags = iFlags | element.getFlag();
      }
      re = new RE(regExp, iFlags);
      this.text = text;
    } catch (Exception ex) {
      if (ex instanceof RESyntaxException)
        throw (RESyntaxException) ex;
      else
        throw new RESyntaxException(ex.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#nextMatch()
   */
  public boolean nextMatch() {
    if (re.match(text, lastEndIndex)) {
      lastEndIndex = re.getParenEnd(0);
      // to avoid endless loops when the expression matches an empty input:
      if (re.getParenLength(0) == 0) {
        lastEndIndex++;
      }
      return true;
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#start()
   */
  public int start() {
    return re.getParenStart(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#end()
   */
  public int end() {
    return re.getParenEnd(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupCount()
   */
  public int groupCount() {
    return re.getParenCount()-1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupContents(int)
   */
  public String groupContents(int groupIndex) {
    return re.getParen(groupIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupStart(int)
   */
  public int groupStart(int groupIndex) {
    return re.getParenStart(groupIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupEnd(int)
   */
  public int groupEnd(int groupIndex) {
    return re.getParenEnd(groupIndex);
  }

  public boolean matches() {
    if (re.match(text)) {
      return re.getParenStart(0) == 0 && re.getParenLength(0) == text.length();
    } else {
      return false;
    }
  }
}
