/*******************************************************************************
 * Copyright (c) 2005 2007 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.jakartaRegexp;

import org.apache.regexp.RE;
import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;

/**
 * Class JakartaRegexpFlag. This represents all flags for the Jakarta-Regexp-regular expressions.
 * 
 * @author bastian.bergerhoff
 */
public class JakartaRegexpFlag extends Flag {
  
  /**
   * Pattern "a" matches both "a" and "A".
   * Corresponds to "i" in Perl notation.
   */
  public static final Flag MATCH_IGNORE_CASE = new JakartaRegexpFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jakartaRegexp.IGNORE_CASE", RE.MATCH_CASEINDEPENDENT, Messages.getString("regexp.jakartaRegexp.REFlag.ignore_case"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jakartaRegexp.REFlag.ignore_case.description")); //$NON-NLS-1$

  /**
   * Affects the behaviour of "^" and "$" tags. When switched off:
   * <li> the "^" matches the beginning of the whole text;
   * <li> the "$" matches the end of the whole text, or just before the '\n' or "\r\n" at the end of text.
   * When switched on:
   * <li> the "^" additionally matches the line beginnings (that is just after the '\n');
   * <li> the "$" additionally matches the line ends (that is just before "\r\n" or '\n');
   * Corresponds to "m" in Perl notation.
   */
  public static final Flag MATCH_MULTILINE = new JakartaRegexpFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jakartaRegexp.MULTILINE", RE.MATCH_MULTILINE, Messages.getString("regexp.jakartaRegexp.REFlag.multiline"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jakartaRegexp.REFlag.multiline.description")); //$NON-NLS-1$

  static {
    flags.put(MATCH_IGNORE_CASE.getCode(), MATCH_IGNORE_CASE);
    flags.put(MATCH_MULTILINE.getCode(), MATCH_MULTILINE);
  }

  private JakartaRegexpFlag(String code, int flag, String name, String description) {
    super(code, flag, name, description);
  }
}