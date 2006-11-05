/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.oro;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;

import de.babe.eclipse.plugins.quickREx.regexp.MatchSet;

/**
 * @author bastian.bergerhoff
 */
public abstract class OROMatchSet implements MatchSet {

  protected Pattern pattern;
  protected PatternMatcher matcher;
  protected PatternMatcherInput input;
  protected String text;
  protected int lastMatchEnd = 0;

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#nextMatch()
   */
  public boolean nextMatch() {
    try {
      if (matcher.contains(input, pattern)) {
        lastMatchEnd = end();
        if (matcher.getMatch().length()==0) {
          lastMatchEnd++;
        }
        return true;
      } else {
        return false;
      }
    } catch (StringIndexOutOfBoundsException e) {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#start()
   */
  public int start() {
    return matcher.getMatch().beginOffset(0);
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#end()
   */
  public int end() {
    return matcher.getMatch().endOffset(0);
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupCount()
   */
  public int groupCount() {
    return matcher.getMatch().groups()-1;
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupContents(int)
   */
  public String groupContents(int groupIndex) {
    return text.substring(groupStart(groupIndex), groupEnd(groupIndex));
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupStart(int)
   */
  public int groupStart(int groupIndex) {
    return matcher.getMatch().beginOffset(groupIndex);
  }

  /* (non-Javadoc)
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupEnd(int)
   */
  public int groupEnd(int groupIndex) {
    return matcher.getMatch().endOffset(groupIndex);
  }
  
  public boolean matches() {
    return matcher.matches(text, pattern);
  }
}
