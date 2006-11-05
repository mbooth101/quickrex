/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp;

/**
 * @author bastian.bergerhoff
 */
public class Group {

	private final int index;
  private final int start;
  private final int end;
	private final String text;

	/**
   * The constructor. Instances of this class are immutable.
   * 
	 * @param p_index the index of this group
	 * @param p_text the textual contents of this group
	 * @param p_start the start-index of this group
	 * @param p_end the end-index of this group
	 */
	public Group(int p_index, String p_text, int p_start, int p_end) {
		this.index = p_index;
		this.text = p_text;
    this.start = p_start;
    this.end = p_end;
	}

	/**
   * Returns the index of this group
   * 
	 * @return the index
	 */
	public int getIndex() {
		return this.index;
	}

  /**
   * Returns the start-index of this group
   * 
   * @return the start-index
   */
  public int getStart() {
    return this.start;
  }

  /**
   * Returns the end-index of this group
   * 
   * @return the end-index
   */
  public int getEnd() {
    return this.end;
  }

	/**
   * Returns (a one-line represenation of) the textual contents of this group
   * where line-breaks and tabs are represented by their escape-sequences
   * 
	 * @return the textual contents
	 */
	public String getText() {
		return this.text.replaceAll("\r", "\\\u005C\\u0072").replaceAll("\n", "\\\u005C\\u006E").replaceAll("\t", "\\\u005C\\u0074");
	}
}
