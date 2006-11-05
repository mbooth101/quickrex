/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Andreas Studer - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp.oro.perl;

import org.apache.oro.text.regex.Perl5Compiler;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;

/**
 * Class OROPerlFlag.
 * This represents all Flags from the ORO Perl implementation.
 *
 * @author Andreas Studer
 * @version 1.0
 * @since 2.1
 */
public class OROPerlFlag extends Flag
{
    public static final Flag PERL_EXTENDED = new OROPerlFlag("de.babe.eclipse.plugins.quickREx.regexp.oro.perl.EXTENDED", Perl5Compiler.EXTENDED_MASK, Messages.getString("regexp.oro.perl.OROPerlFlag.extended"), //$NON-NLS-1$ //$NON-NLS-2$
    Messages.getString("regexp.oro.perl.OROPerlFlag.extended.description")); //$NON-NLS-1$
    public static final Flag PERL_SINGLELINE = new OROPerlFlag("de.babe.eclipse.plugins.quickREx.regexp.oro.perl.SINGLELINE", Perl5Compiler.SINGLELINE_MASK, Messages.getString("regexp.oro.perl.OROPerlFlag.singleline"), //$NON-NLS-1$ //$NON-NLS-2$
    Messages.getString("regexp.oro.perl.OROPerlFlag.singleline.description")); //$NON-NLS-1$
    public static final Flag PERL_MULTILINE = new OROPerlFlag("de.babe.eclipse.plugins.quickREx.regexp.oro.perl.MULTILINE", Perl5Compiler.MULTILINE_MASK, Messages.getString("regexp.oro.perl.OROPerlFlag.multiline"), //$NON-NLS-1$ //$NON-NLS-2$
    Messages.getString("regexp.oro.perl.OROPerlFlag.multiline.description")); //$NON-NLS-1$
    public static final Flag PERL_CASE_INSENSITIVE = new OROPerlFlag("de.babe.eclipse.plugins.quickREx.regexp.oro.perl.CASE_INSENSITIVE", Perl5Compiler.CASE_INSENSITIVE_MASK, Messages.getString("regexp.oro.perl.OROPerlFlag.case_insensitive"), //$NON-NLS-1$ //$NON-NLS-2$
    Messages.getString("regexp.oro.perl.OROPerlFlag.case_insensitive.description")); //$NON-NLS-1$

    static {
        flags.put(PERL_EXTENDED.getCode(), PERL_EXTENDED);
        flags.put(PERL_SINGLELINE.getCode(), PERL_SINGLELINE);
        flags.put(PERL_MULTILINE.getCode(), PERL_MULTILINE);
        flags.put(PERL_CASE_INSENSITIVE.getCode(), PERL_CASE_INSENSITIVE);
    }

    private OROPerlFlag(String code, int flag, String name, String description)
    {
        super(code, flag, name, description);
        // TODO Auto-generated constructor stub
    }
}
