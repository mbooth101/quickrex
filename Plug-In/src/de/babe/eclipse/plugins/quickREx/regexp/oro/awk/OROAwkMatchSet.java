/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *     Andreas Studer - Contributions to handling global flags
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.oro.awk;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.apache.oro.text.awk.AwkCompiler;
import org.apache.oro.text.awk.AwkMatcher;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternMatcherInput;

import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.oro.OROMatchSet;

/**
 * @author bastian.bergerhoff, andreas.studer
 */
public class OROAwkMatchSet extends OROMatchSet {

  private final static Collection flags = new Vector();
  
  static {
    flags.add(OROAwkFlag.AWK_CASE_INSENSITIVE);
  }
  
  /**
   * Returns a Collection of all Compiler-Flags the ORO-Awk-implementation
   * knows about.
   * 
   * @return a Collection of all Compiler-Flags the ORO-Awk-implementation
   * knows about
   */
  public static Collection getAllFlags() {
    return flags;
  }
  
  /**
   * The constructor.
   * 
   * @param regExp the regular expression
   * @param text the Text the RegExp should be applied to
   * @param flags a Collection of Flags to pass to the Compiler
   */
  public OROAwkMatchSet(String regExp, String text, Collection flags) {
    super();
    try {
      int iFlags = 0;
      for (Iterator iter = flags.iterator(); iter.hasNext();) {
        Flag element = (Flag)iter.next();
        iFlags = iFlags | element.getFlag();
      }
      AwkCompiler comp = new AwkCompiler();
      this.pattern = comp.compile(regExp, iFlags);
      this.matcher = new AwkMatcher();
      this.text = text;
      this.input = new PatternMatcherInput(text);
    } catch (MalformedPatternException e) {
      throw new PatternSyntaxException(e.getMessage(), regExp, 0);
    }    
  }

}
