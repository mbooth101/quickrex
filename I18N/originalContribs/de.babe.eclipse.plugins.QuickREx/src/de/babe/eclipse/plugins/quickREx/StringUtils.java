/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx;

import java.text.StringCharacterIterator;

/**
 * @author bastian.bergerhoff
 */
public abstract class StringUtils {

  /**
   * Returns the passed String with any occurrence of " replaced by \"
   * and any occurrence of \ replaced by \\.
   *  
   * @param p_text the String to escape 
   * @return the escaped String or "" if null was passed.
   */
  public static String escapeForJava(String p_text) {
    StringBuffer retBuffer = new StringBuffer();
    StringCharacterIterator it = new StringCharacterIterator(p_text);
    for(char c = it.first(); c != StringCharacterIterator.DONE; c = it.next()) {
      if (c == '\\') {
        retBuffer.append("\\\\");
      } else if (c == '"') {
        retBuffer.append("\\\"");
      } else {
        retBuffer.append(c);
      }
    }
    return retBuffer.toString();
  }

  /**
   * Returns the first line of the passed String
   * 
   * @param p_text the String to return the first line from
   * @return the first line of the passed String
   */
  public static String firstLine(String p_text) {
    if (p_text.indexOf("\r") >= 0) {
      return p_text.substring(0, p_text.indexOf("\r"));
    } else {
      return p_text;
    }
  }
}
