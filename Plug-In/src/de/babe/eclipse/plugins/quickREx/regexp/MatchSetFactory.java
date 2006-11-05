/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *     Andreas Studer - Contributions to handling global flags
 *     Georg Sendt - added JRegexp-related implementations
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp;

import java.util.Collection;
import java.util.Vector;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.regexp.jakartaRegexp.JakartaRegexpMatchSet;
import de.babe.eclipse.plugins.quickREx.regexp.jdk.JavaMatchSet;
import de.babe.eclipse.plugins.quickREx.regexp.jregex.JRegexMatchSet;
import de.babe.eclipse.plugins.quickREx.regexp.oro.awk.OROAwkMatchSet;
import de.babe.eclipse.plugins.quickREx.regexp.oro.perl.OROPerlMatchSet;

/**
 * @author bastian.bergerhoff, andreas.studer, georg.sendt
 */
public class MatchSetFactory {

  public static final int JAVA_FLAVOUR = 1;

  public static final int ORO_PERL_FLAVOUR = 2;

  public static final int ORO_AWK_FLAVOUR = 4;
  
  public static final int JREGEX_FLAVOUR = 8;
  
  public static final int JAKARTA_REGEXP_FLAVOUR = 16;

  /**
   * Factory-Method to create a MatchSet for the passed details.
   * 
   * @param flavour
   *          one of the Flavour-Flags defined in this class
   * @param regExp
   *          the regular expression
   * @param text
   *          the text to match against the reg.exp.
   * @param flags
   *          a Collection of flags to pass to the Compiler. This may contain more flags than are applicable to the requested flavour. In this case,
   *          only those flags wich are applicable are taken into account when creating the MatchSet
   * @return a MatchSet as requested
   */
  public static MatchSet createMatchSet(int flavour, String regExp, String text, Collection flags) {
    Vector flavourFlags = new Vector(flags);
    flavourFlags.retainAll(MatchSetFactory.getAllFlags(flavour));
    switch (flavour) {
    case JAVA_FLAVOUR:
      return new JavaMatchSet(regExp, text, flavourFlags);
    case ORO_PERL_FLAVOUR:
      return new OROPerlMatchSet(regExp, text, flavourFlags);
    case ORO_AWK_FLAVOUR:
      return new OROAwkMatchSet(regExp, text, flavourFlags);
    case JREGEX_FLAVOUR:
      return new JRegexMatchSet(regExp, text, flavourFlags);
    case JAKARTA_REGEXP_FLAVOUR:
      return new JakartaRegexpMatchSet(regExp, text, flavourFlags);
    default:
      throw new IllegalArgumentException(Messages.getString("regexp.MatchSetFactory.error.message1") + flavour); //$NON-NLS-1$
    }
  }

  /**
   * Gets all possible flags for a specific regex-flavour.
   * 
   * @param flavour
   *          The flavour
   * @return A collection of the type de.babe.eclipse.plugins.quickREx.regexp.Flag
   * @see de.babe.eclipse.plugins.quickREx.regexp.Flag
   */
  public static Collection getAllFlags(int flavour) {
    switch (flavour) {
    case JAVA_FLAVOUR:
      return JavaMatchSet.getAllFlags();
    case ORO_PERL_FLAVOUR:
      return OROPerlMatchSet.getAllFlags();
    case ORO_AWK_FLAVOUR:
      return OROAwkMatchSet.getAllFlags();
    case JREGEX_FLAVOUR:
      return JRegexMatchSet.getAllFlags();
    case JAKARTA_REGEXP_FLAVOUR:
      return JakartaRegexpMatchSet.getAllFlags();
    default:
      throw new IllegalArgumentException(Messages.getString("regexp.MatchSetFactory.error.message2") + flavour); //$NON-NLS-1$
    }
  }

  /**
   * Returns a Collection of all flags supported by any of the regular-expression Compilers used by the plug-in.
   * 
   * @return a Collection of all flags supported by any of the regular-expression Compilers used by the plug-in
   */
  public static Collection getAllSupportedFlags() {
    Vector allFlags = new Vector();
    allFlags.addAll(MatchSetFactory.getAllFlags(JAVA_FLAVOUR));
    allFlags.addAll(MatchSetFactory.getAllFlags(ORO_PERL_FLAVOUR));
    allFlags.addAll(MatchSetFactory.getAllFlags(ORO_AWK_FLAVOUR));
    allFlags.addAll(MatchSetFactory.getAllFlags(JREGEX_FLAVOUR));
    allFlags.addAll(MatchSetFactory.getAllFlags(JAKARTA_REGEXP_FLAVOUR));
    return allFlags;
  }

  /**
   * Helper-Method to get the maximum number of flags supported by any of the regular-expression Compilers used by the plug-in.
   * 
   * @return the maximum number of flags
   */
  public static int getMaxFlagColumns() {
    return JavaMatchSet.getAllFlags().size();
  }
}