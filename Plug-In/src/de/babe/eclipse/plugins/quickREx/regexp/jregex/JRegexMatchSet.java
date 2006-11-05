/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Georg Sendt - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.jregex;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import jregex.Matcher;
import jregex.Pattern;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSet;

/**
 * MatchSet using JRegex-regular expressions.
 * 
 * @author georg.sendt
 */
public class JRegexMatchSet implements MatchSet {

  private final Pattern pattern;

  private final Matcher matcher;

  private final static Collection flags = new Vector();

  static {
    flags.add(JRegexFlag.RE_IGNORE_CASE);
    flags.add(JRegexFlag.RE_MULTILINE);
    flags.add(JRegexFlag.RE_DOTALL);
    flags.add(JRegexFlag.RE_IGNORE_SPACES);
    flags.add(JRegexFlag.RE_UNICODE);
    flags.add(JRegexFlag.RE_XML_SCHEMA);
  }

  /**
   * Returns a Collection of all Compiler-Flags the JRegex-implementation knows about.
   * 
   * @return a Collection of all Compiler-Flags the JRegex-implementation knows about
   */
  public static Collection getAllFlags() {
    return flags;
  }

  /**
   * The constructor - uses the JRegex regular expressions to evaluate the passed regular expression against the passed
   * text.
   * 
   * @param regExp
   *          the regular expression
   * @param text
   *          the text to evaluate regExp against
   * @param flags
   *          a Collection of Flags to pass to the Compiler
   */
  public JRegexMatchSet(String regExp, String text, Collection flags) {

    try {
      int iFlags = 0;
      for (Iterator iter = flags.iterator(); iter.hasNext();) {
        Flag element = (Flag) iter.next();
        iFlags = iFlags | element.getFlag();
      }
      pattern = new Pattern(regExp, iFlags);
      matcher = pattern.matcher(text);
    } catch (Exception ex) {
      if (ex instanceof PatternSyntaxException)
        throw (PatternSyntaxException) ex;
      else
        throw new PatternSyntaxException(ex.getMessage(), regExp, 0);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#nextMatch()
   */
  public boolean nextMatch() {
    return matcher.find();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#start()
   */
  public int start() {
    return matcher.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#end()
   */
  public int end() {
    return matcher.end();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupCount()
   */
  public int groupCount() {
    return matcher.groupCount()-1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupContents(int)
   */
  public String groupContents(int groupIndex) {
    return matcher.group(groupIndex);
  }

  public String getGroup(String groupID) {
    return matcher.group(groupID);
  }

  public String getGroupID(int groupIndex) {
    return matcher.pattern().groupName(new Integer(groupIndex));
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupStart(int)
   */
  public int groupStart(int groupIndex) {
    return matcher.start(groupIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSet#groupEnd(int)
   */
  public int groupEnd(int groupIndex) {
    return matcher.end(groupIndex);
  }

  public boolean matches() {
    return matcher.matches();
  }
}
