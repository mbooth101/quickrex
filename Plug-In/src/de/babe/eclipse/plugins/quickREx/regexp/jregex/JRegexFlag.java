/*******************************************************************************
 * Copyright (c) 2005, 2007 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Georg Sendt - initial API and implementation
 *     Bastian Bergerhoff - some minor cleanup
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.jregex;

import jregex.REFlags;
import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;

/**
 * Class JavaFlag. This represents all flags for the JRegex implementation.
 * 
 * @author Georg Sendt, Bastian Bergerhoff
 * @version 1.1
 * @since 3.5
 */
public class JRegexFlag extends Flag {
  
  /**
   * Pattern "a" matches both "a" and "A".
   * Corresponds to "i" in Perl notation.
   */
  public static final Flag RE_IGNORE_CASE = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.IGNORE_CASE", REFlags.IGNORE_CASE, Messages.getString("regexp.jregex.REFlag.ignore_case"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.ignore_case.description")); //$NON-NLS-1$

  /**
   * Affects the behaviour of "^" and "$" tags. When switched off:
   * <li> the "^" matches the beginning of the whole text;
   * <li> the "$" matches the end of the whole text, or just before the '\n' or "\r\n" at the end of text.
   * When switched on:
   * <li> the "^" additionally matches the line beginnings (that is just after the '\n');
   * <li> the "$" additionally matches the line ends (that is just before "\r\n" or '\n');
   * Corresponds to "m" in Perl notation.
   */
  public static final Flag RE_MULTILINE = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.MULTILINE", REFlags.MULTILINE, Messages.getString("regexp.jregex.REFlag.multiline"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.multiline.description")); //$NON-NLS-1$


  /**
   * Affects the behaviour of dot(".") tag. When switched off:
   * <li> the dot matches any character but EOLs('\r','\n');
   * When switched on:
   * <li> the dot matches any character, including EOLs.
   * This flag is sometimes referenced in regex tutorials as SINGLELINE, which confusingly seems opposite to MULTILINE, but in fact is orthogonal.
   * Corresponds to "s" in Perl notation.
   */
  public static final Flag RE_DOTALL = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.DOTALL", REFlags.DOTALL, Messages.getString("regexp.jregex.REFlag.dotall"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.dotall.description")); //$NON-NLS-1$

  /**
   * Affects how the space characters are interpeted in the expression. When switched off:
   * <li> the spaces are interpreted literally;
   * When switched on:
   * <li> the spaces are ingnored, allowing an expression to be slightly more readable.
   * Corresponds to "x" in Perl notation.
   */
  public static final Flag RE_IGNORE_SPACES = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.IGNORE_SPACES", REFlags.IGNORE_SPACES, Messages.getString("regexp.jregex.REFlag.ignore_spaces"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.ignore_spaces.description")); //$NON-NLS-1$
  
  /**
   * Affects whether the predefined classes("\d","\s","\w",etc) in the expression are interpreted as belonging to Unicode. When switched off:
   * <li> the predefined classes are interpreted as ASCII;
   * When switched on:
   * <li> the predefined classes are interpreted as Unicode categories;
   */
  public static final Flag RE_UNICODE = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.UNICODE", REFlags.UNICODE, Messages.getString("regexp.jregex.REFlag.unicode"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.unicode.description")); //$NON-NLS-1$

  /**
   * Turns on the compatibility with XML Schema regular expressions.
   */
  public static final Flag RE_XML_SCHEMA = new JRegexFlag(
      "de.babe.eclipse.plugins.quickREx.regexp.jregex.XML_SCHEMA", REFlags.XML_SCHEMA, Messages.getString("regexp.jregex.REFlag.xml_schema"), //$NON-NLS-1$ //$NON-NLS-2$
      Messages.getString("regexp.jregex.REFlag.xml_schema.description")); //$NON-NLS-1$

  static {
    flags.put(RE_IGNORE_CASE.getCode(), RE_IGNORE_CASE);
    flags.put(RE_MULTILINE.getCode(), RE_MULTILINE);
    flags.put(RE_DOTALL.getCode(), RE_DOTALL);
    flags.put(RE_IGNORE_SPACES.getCode(), RE_IGNORE_SPACES);
    flags.put(RE_UNICODE.getCode(), RE_UNICODE);
    flags.put(RE_XML_SCHEMA.getCode(), RE_XML_SCHEMA);
  }

  private JRegexFlag(String code, int flag, String name, String description) {
    super(code, flag, name, description);
  }
}