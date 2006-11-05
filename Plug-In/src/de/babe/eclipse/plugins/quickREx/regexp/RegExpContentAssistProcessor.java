/*******************************************************************************
 * Copyright (c) 2005 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *     Georg Sendt - added JRegexp-related implementations
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import de.babe.eclipse.plugins.quickREx.QuickRExPlugin;

/**
 * @author bastian.bergerhoff, georg.sendt
 */
public class RegExpContentAssistProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {

  /**
   * The available proposal strings.
   */
  private final static HashMap jdkProposalStrings = new HashMap();

  private final static HashMap oroPerlProposalStrings = new HashMap();

  private final static HashMap oroAwkProposalStrings = new HashMap();

  private final static HashMap jregexProposalStrings = new HashMap();
  
  private final static HashMap jakartaRegexpProposalStrings = new HashMap();

  /**
   * The available proposal keys.
   */
  private final static ArrayList jdkProposalKeys = new ArrayList();

  private final static ArrayList oroPerlProposalKeys = new ArrayList();

  private final static ArrayList oroAwkProposalKeys = new ArrayList();

  private final static ArrayList jregexProposalKeys = new ArrayList();

  private final static ArrayList jakartaRegexpProposalKeys = new ArrayList();

  static {
    initializeProposals();
  }

  private static void initializeProposals() {
    QuickRExPlugin.getDefault().initOROPerlCompletionsFromFile(oroPerlProposalStrings, oroPerlProposalKeys);
    QuickRExPlugin.getDefault().initOROAwkCompletionsFromFile(oroAwkProposalStrings, oroAwkProposalKeys);
    QuickRExPlugin.getDefault().initJDKCompletionsFromFile(jdkProposalStrings, jdkProposalKeys);
    QuickRExPlugin.getDefault().initJRegexCompletionsFromFile(jregexProposalStrings, jregexProposalKeys);
    QuickRExPlugin.getDefault().initJakartaRegexpCompletionsFromFile(jakartaRegexpProposalStrings, jakartaRegexpProposalKeys);
  }

  /**
   * The context information validator.
   */
  private IContextInformationValidator fValidator = new SubjectControlContextInformationValidator(this);

  /*
   * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
   */
  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    return computeCompletionProposals((IContentAssistSubjectControl)null, documentOffset);
  }

  /*
   * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
    return computeContextInformation((IContentAssistSubjectControl)null, documentOffset);
  }

  /*
   * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters() {
    return new char[] { '\\', '[', '(' };
  }

  /*
   * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters() {
    return new char[] {};
  }

  /*
   * @see IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator() {
    return fValidator;
  }

  /*
   * @see IContentAssistProcessor#getErrorMessage()
   */
  public String getErrorMessage() {
    return null;
  }

  /*
   * @see ISubjectControlContentAssistProcessor#computeCompletionProposals(IContentAssistSubjectControl, int)
   */
  public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    Set results = new TreeSet(new RECompletionProposalComparator());
    Iterator iter = null;
    if (QuickRExPlugin.getDefault().isUsingJavaRE()) {
      iter = jdkProposalKeys.iterator();
    } else if (QuickRExPlugin.getDefault().isUsingOROPerlRE()) {
      iter = oroPerlProposalKeys.iterator();
    } else if (QuickRExPlugin.getDefault().isUsingOROAwkRE()) {
      iter = oroAwkProposalKeys.iterator();
    } else if (QuickRExPlugin.getDefault().isUsingJRegex()) {
      iter = jregexProposalKeys.iterator();
    } else if (QuickRExPlugin.getDefault().isUsingJakartaRegexp()) {
      iter = jakartaRegexpProposalKeys.iterator();
    } else {
      return new ICompletionProposal[0];
    }
    while (iter.hasNext()) {
      addProposal((String)iter.next(), contentAssistSubjectControl, documentOffset, results);
    }

    List proposals = new ArrayList(results.size());

    Iterator propIt = results.iterator();

    while (propIt.hasNext()) {
      RECompletionProposal proposal = (RECompletionProposal)propIt.next();

      String proposalKey = proposal.getKey();
      String displayString = proposal.getDisplayString();
      String additionalInfo = proposal.getAdditionalInfo();
      IContextInformation info = createContextInformation(proposalKey);

      int relativeOffset = proposal.getInsertString().length();

      proposals.add(new CompletionProposal(proposal.getInsertString(), documentOffset, 0, Math.max(0, relativeOffset), null, displayString, info,
          additionalInfo));
    }

    return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[results.size()]);
  }

  /*
   * @see ISubjectControlContentAssistProcessor#computeContextInformation(IContentAssistSubjectControl, int)
   */
  public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
    return null;
  }

  private void addProposal(String proposalKey, IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset, Set results) {
    RECompletionProposal proposal = null;

    if (QuickRExPlugin.getDefault().isUsingJavaRE()) {
      proposal = (RECompletionProposal)jdkProposalStrings.get(proposalKey);
    } else if (QuickRExPlugin.getDefault().isUsingOROPerlRE()) {
      proposal = (RECompletionProposal)oroPerlProposalStrings.get(proposalKey);
    } else if (QuickRExPlugin.getDefault().isUsingOROAwkRE()) {
      proposal = (RECompletionProposal)oroAwkProposalStrings.get(proposalKey);
    } else if (QuickRExPlugin.getDefault().isUsingJRegex()) {
      proposal = (RECompletionProposal)jregexProposalStrings.get(proposalKey);
    } else if (QuickRExPlugin.getDefault().isUsingJakartaRegexp()) {
      proposal = (RECompletionProposal)jakartaRegexpProposalStrings.get(proposalKey);
    } 

    try {
      String text = null;
      text = contentAssistSubjectControl.getDocument().get(0, documentOffset);

      proposal.setText(text);

      if (proposal.isMatch()) {
        results.add(proposal);
      }
    } catch (BadLocationException ble) {
      // nop
    }
  }

  private IContextInformation createContextInformation(String proposalKey) {
    return new ContextInformation(null, "contextDisplayString", "informationDisplayString"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}