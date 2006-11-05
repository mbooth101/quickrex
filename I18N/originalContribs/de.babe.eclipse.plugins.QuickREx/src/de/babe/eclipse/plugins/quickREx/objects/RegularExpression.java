/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.objects;

/**
 * @author bastian.bergerhoff
 */
public class RegularExpression {

	private String string;
	public static final String INSTANCE_QNAME = "regularExpression";

	/**
   * THe constructor
   * 
	 * @param p_stringValue
	 */
	public RegularExpression(String p_stringValue) {
		this.string = p_stringValue;
	}
	
	/**
   * The string representing this Reg. Exp.
	 * 
   * @return the string-representation
	 */
	public String getString() {
		return this.string;
	}

  /**
   * Returns an XML-representation of this object, using the passed String
   * as a prefix for each line
   * 
   * @param p_prefix the prefix for the line
   * @return an XML-String-representation of this object
   */
	public String toXMLString(String p_prefix) {
	    StringBuffer retBuffer = new StringBuffer(p_prefix);
		retBuffer.append("<").append(INSTANCE_QNAME).append(">");
		retBuffer.append(replaceIllegalChars(this.string));
		retBuffer.append("</").append(INSTANCE_QNAME).append(
				">\r\n");
		return retBuffer.toString();
	}

	private String replaceIllegalChars(String p_input) {
		return p_input.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
