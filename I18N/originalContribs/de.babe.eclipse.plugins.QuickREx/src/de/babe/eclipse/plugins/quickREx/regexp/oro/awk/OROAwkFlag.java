/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Andreas Studer - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.oro.awk;

import org.apache.oro.text.awk.AwkCompiler;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;

/**
 * Class OROAwkFlag.
 * This represents all flags for the ORO Awk implementation.
 *
 * @author Andreas Studer
 * @version 1.0
 * @since 2.1
 */
public class OROAwkFlag extends Flag
{
    public static final Flag AWK_CASE_INSENSITIVE = new OROAwkFlag("de.babe.eclipse.plugins.quickREx.regexp.oro.awk.CASE_INSENSITIVE", AwkCompiler.CASE_INSENSITIVE_MASK, Messages.getString("regexp.oro.awk.OROAwkFlag.case_insensitive"), //$NON-NLS-1$ //$NON-NLS-2$
    Messages.getString("regexp.oro.awk.OROAwkFlag.case_insensitive.description")); //$NON-NLS-1$

    static {
        flags.put(AWK_CASE_INSENSITIVE.getCode(), AWK_CASE_INSENSITIVE);
    }

    private OROAwkFlag(String code, int flag, String name, String description)
    {
        super(code, flag, name, description);
    }
}
