/*******************************************************************************
 * Copyright (c) 2005, 2006 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *     Andreas Studer - Contributions to handling global flags
 *     Georg Sendt - Contributions to threaded evaluation, implementation of
 *                   JRegex-Flavour
 *******************************************************************************/
package de.babe.eclipse.plugins.quickREx.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.contentassist.ComboContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.PluginImageRegistry;
import de.babe.eclipse.plugins.quickREx.QuickRExPlugin;
import de.babe.eclipse.plugins.quickREx.StringUtils;
import de.babe.eclipse.plugins.quickREx.actions.GrepAction;
import de.babe.eclipse.plugins.quickREx.actions.JCopyAction;
import de.babe.eclipse.plugins.quickREx.actions.KeepREAction;
import de.babe.eclipse.plugins.quickREx.actions.LoadTestTextAction;
import de.babe.eclipse.plugins.quickREx.actions.OrganizeREsAction;
import de.babe.eclipse.plugins.quickREx.actions.OrganizeTestTextsAction;
import de.babe.eclipse.plugins.quickREx.actions.SaveTestTextAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJDKREAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJRegexAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJakartaRegexpAction;
import de.babe.eclipse.plugins.quickREx.actions.UseOROAwkREAction;
import de.babe.eclipse.plugins.quickREx.actions.UseOROPerlREAction;
import de.babe.eclipse.plugins.quickREx.dialogs.OrganizeREsDialog;
import de.babe.eclipse.plugins.quickREx.dialogs.OrganizeTestTextDialog;
import de.babe.eclipse.plugins.quickREx.dialogs.SimpleTextDialog;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpression;
import de.babe.eclipse.plugins.quickREx.preferences.QuickRExPreferencesPage;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.Match;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory;
import de.babe.eclipse.plugins.quickREx.regexp.RegExpContentAssistProcessor;
import de.babe.eclipse.plugins.quickREx.regexp.RegularExpressionHits;

/**
 * @author bastian.bergerhoff, andreas.studer, georg.sendt
 */
public class QuickRExView extends ViewPart {

  public final static String ID = "de.babe.eclipse.plugins.quickREx.views.QuickRExView"; //$NON-NLS-1$

  private Combo regExpCombo;

  private StyledText testText;

  private Label globalMatch;

  private Label matches;

  private Label groups;

  private Button liveEvalButton;

  private Button evaluateButton;

  private Button previousButton;

  private Button nextButton;

  private Button previousGroupButton;

  private Button nextGroupButton;

  private RegularExpressionHits hits = new RegularExpressionHits();

  private Action organizeREsAction;

  private Action organizeTestTextsAction;

  private static final String MATCH_BG_COLOR_KEY = "de.babe.eclipse.plugins.QuickREx.matchBgColor"; //$NON-NLS-1$

  private static final String MATCH_FG_COLOR_KEY = "de.babe.eclipse.plugins.QuickREx.matchFgColor"; //$NON-NLS-1$

  private static final String CURRENT_MATCH_BG_COLOR_KEY = "de.babe.eclipse.plugins.QuickREx.currentMatchBgColor"; //$NON-NLS-1$

  private static final String CURRENT_MATCH_FG_COLOR_KEY = "de.babe.eclipse.plugins.QuickREx.currentMatchFgColor"; //$NON-NLS-1$

  private static final String NOT_EVALUATED_BG_COLOR_KEY = "de.babe.eclipse.plugins.QuickREx.notEvaluatedBgColor"; //$NON-NLS-1$

  public static final String EDITOR_FONT_KEY = "de.babe.eclipse.plugins.QuickREx.textfontDefinition"; //$NON-NLS-1$

  private SubjectControlContentAssistant regExpContentAssistant;

  private Action useJDKREAction;

  private Action useOROPerlREAction;

  private Action useOROAWKAction;
  
  private Action useJRegexAction;
  
  private Action useJakartaRegexpAction;
  
  private Collection currentFlags = new Vector();

  private String msg = ""; //$NON-NLS-1$

  private Action keepREAction;

  private Action saveTextAction;

  private Action loadTextAction;

  private Action jcopyAction;

  private Action grepAction;

  private boolean liveEval;

  /**
   * The constructor.
   */
  public QuickRExView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent) {
    createViewContents(parent);
    makeActions();
    contributeToActionBars();
    initializeCurrentFlags();
    liveEval = QuickRExPlugin.getDefault().isLiveEvaluation();
    liveEvalButton.setSelection(liveEval);
    evaluateButton.setEnabled(!liveEval);
  }

  private void initializeCurrentFlags() {
    for (Iterator iter = MatchSetFactory.getAllSupportedFlags().iterator(); iter.hasNext();) {
      Flag element = (Flag)iter.next();
      if (QuickRExPlugin.getDefault().isFlagSaved(element)) {
        currentFlags.add(element);
      }
    }
  }

  private void createViewContents(Composite parent) {
    FormToolkit tk = new FormToolkit(parent.getDisplay());
    Form form = tk.createForm(parent);
    GridLayout layout = new GridLayout();
    
    layout.numColumns = 4;

    form.getBody().setLayout(layout);

    createFirstRow(tk, form);

    createSecondRow(tk, form);

    createThirdRow(tk, form);

    createFourthRow(tk, form);

    createFifthRow(tk, form);

    createFlagsSection(tk, form);
  }

  private void createFlagsSection(FormToolkit tk, final Form form) {
    GridData gd;
    Section section = tk.createSection(form.getBody(), Section.DESCRIPTION | Section.TWISTIE);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 5;
    section.setLayoutData(gd);
    section.setText(Messages.getString("views.QuickRExView.global.flags")); //$NON-NLS-1$
    tk.createCompositeSeparator(section);
    section.setDescription(Messages.getString("views.QuickRExView.global.flags.description") + msg); //$NON-NLS-1$
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        form.redraw();
      }
    });
    Composite client = tk.createComposite(section);
    GridLayout layout = new GridLayout();
    layout.numColumns = MatchSetFactory.getMaxFlagColumns() + 1;
    client.setLayout(layout);
    gd = new GridData();
    gd.horizontalSpan = 2;
    gd.grabExcessHorizontalSpace = true;
    client.setLayoutData(gd);

    createFlagFlavourSection(tk, client, layout, gd, Messages.getString("views.QuickRExView.jdk.flags"), MatchSetFactory.JAVA_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk, client, layout, gd, Messages.getString("views.QuickRExView.perl.flags"), MatchSetFactory.ORO_PERL_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk, client, layout, gd, Messages.getString("views.QuickRExView.awk.flags"), MatchSetFactory.ORO_AWK_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk, client, layout, gd, Messages.getString("views.QuickRExView.jregex.flags"), MatchSetFactory.JREGEX_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk, client, layout, gd, Messages.getString("views.QuickRExView.jakartaRegexp.flags"), MatchSetFactory.JAKARTA_REGEXP_FLAVOUR); //$NON-NLS-1$

    section.setClient(client);
  }

  /*
   * Creates a line of flags. This is a helper for the Method createFlagSection. @param tk The FormToolkit to use @param client The Composite Client
   * @param layout The GridLayout to use. @param gd The GridData to fill. @param text The text for the labe at the beginning. @param flavour The
   * Flavour to use from MatchSetFactory
   * 
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory
   */
  private void createFlagFlavourSection(FormToolkit tk, Composite client, GridLayout layout, GridData gd, String text, int flavour) {
    Label l = tk.createLabel(client, text);
    int nButtons = 1;
    Collection jdkFlags = MatchSetFactory.getAllFlags(flavour);
    for (Iterator iter = jdkFlags.iterator(); iter.hasNext();) {
      nButtons++;
      final Flag element = (Flag)iter.next();
      final Button checkButton = tk.createButton(client, element.getName(), SWT.CHECK);
      gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
      gd.grabExcessHorizontalSpace = false;
      checkButton.setLayoutData(gd);
      checkButton.setToolTipText(element.getDescription());
      checkButton.setSelection(QuickRExPlugin.getDefault().isFlagSaved(element));
      checkButton.addSelectionListener(new SelectionListener() {
        public void widgetSelected(SelectionEvent p_e) {
          if (checkButton.getSelection()) {
            currentFlags.add(element);
          } else {
            currentFlags.remove(element);
          }
          updateEvaluation();
        }

        public void widgetDefaultSelected(SelectionEvent p_e) {
        }
      });
    }
    while (nButtons < layout.numColumns) {
      nButtons++;
      Label fillLabel = tk.createLabel(client, ""); //$NON-NLS-1$
    }
  }

  private void createFifthRow(FormToolkit tk, Form form) {
    GridData gd;
    // Fourth row...
    Label groupsLabel = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.fifthrow.label")); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    groupsLabel.setLayoutData(gd);
    previousGroupButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.fifthrow.prev"), SWT.PUSH); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    previousGroupButton.setLayoutData(gd);
    previousGroupButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        handlePreviousGroupButtonPressed();
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    previousGroupButton.setEnabled(false);
    nextGroupButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.fifthrow.next"), SWT.PUSH); //$NON-NLS-1$
    gd = new GridData(GridData.VERTICAL_ALIGN_END);
    gd.grabExcessHorizontalSpace = false;
    nextGroupButton.setLayoutData(gd);
    nextGroupButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        handleNextGroupButtonPressed();
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    nextGroupButton.setEnabled(false);
    groups = tk.createLabel(form.getBody(), ""); //$NON-NLS-1$
    gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    groups.setLayoutData(gd);
  }

  private void createFourthRow(FormToolkit tk, Form form) {
    GridData gd;
    // Third row...
    Label regExpResult = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.fourthrow.label")); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    regExpResult.setLayoutData(gd);
    previousButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.fourthrow.prev"), SWT.PUSH); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    previousButton.setLayoutData(gd);
    previousButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        handlePreviousButtonPressed();
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    previousButton.setEnabled(false);
    nextButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.fourthrow.next"), SWT.PUSH); //$NON-NLS-1$
    gd = new GridData(GridData.VERTICAL_ALIGN_END);
    gd.grabExcessHorizontalSpace = false;
    nextButton.setLayoutData(gd);
    nextButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        handleNextButtonPressed();
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    nextButton.setEnabled(false);
    matches = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.fourthrow.message")); //$NON-NLS-1$
    gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    matches.setLayoutData(gd);
  }

  private void createThirdRow(FormToolkit tk, Form form) {
    liveEvalButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.button.toggleLiveEvaluation.label"), SWT.CHECK); //$NON-NLS-1$
    GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    gd.grabExcessHorizontalSpace = false;
    liveEvalButton.setLayoutData(gd);
    liveEvalButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        if (liveEvalButton.getSelection()) {
          QuickRExPlugin.getDefault().getPreferenceStore().setValue(QuickRExPreferencesPage.P_LIVE_EVAL, true);
          liveEval = true;
          evaluateButton.setEnabled(false);
          evaluate();
        } else {
          QuickRExPlugin.getDefault().getPreferenceStore().setValue(QuickRExPreferencesPage.P_LIVE_EVAL, false);
          liveEval = false;
          evaluateButton.setEnabled(true);
        }
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    liveEvalButton.setEnabled(true);
    liveEvalButton.setSelection(true);
    liveEvalButton.setToolTipText(Messages.getString("views.QuickRExView.button.toggleLiveEvaluation.tooltip")); //$NON-NLS-1$
    evaluateButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.button.evaluate.label"), SWT.PUSH); //$NON-NLS-1$
    gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.grabExcessHorizontalSpace = false;
    evaluateButton.setLayoutData(gd);
    evaluateButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent p_e) {
        evaluate();
      }

      public void widgetDefaultSelected(SelectionEvent p_e) {
      }
    });
    evaluateButton.setEnabled(!liveEval);
    globalMatch = tk.createLabel(form.getBody(), ""); //$NON-NLS-1$
    gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalSpan = 2;
    globalMatch.setLayoutData(gd);
  }

  private void createSecondRow(FormToolkit tk, Form form) {
    GridData gd;
    // Second row
    Label testTextEnter = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.secondrow.label")); //$NON-NLS-1$
    gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
    gd.grabExcessHorizontalSpace = false;
    testTextEnter.setLayoutData(gd);
    testText = new StyledText(form.getBody(), SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    testText.setFont(JFaceResources.getFont(EDITOR_FONT_KEY));
    gd = new GridData(GridData.FILL_BOTH);
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    
    gd.horizontalSpan = 3;
    testText.setLayoutData(gd);
    testText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent p_e) {
        handleTestTextModified();
      }
    });
    tk.adapt(testText, true, true);
  }

  private void createFirstRow(FormToolkit tk, Form form) {
    GridData gd;
    // First row...
    Label regExpEnter = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.firstrow.label")); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.grabExcessHorizontalSpace = false;
    regExpEnter.setLayoutData(gd);
    regExpCombo = new Combo(form.getBody(), SWT.DROP_DOWN);
    regExpCombo.setItems(QuickRExPlugin.getDefault().getRegularExpressions());
    regExpCombo.setFont(JFaceResources.getFont(EDITOR_FONT_KEY));
    gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    gd.horizontalSpan = 3;
    gd.grabExcessHorizontalSpace = true;
    regExpCombo.setLayoutData(gd);
    regExpCombo.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent p_e) {
        handleRegExpModified();
      }
    });
    regExpCombo.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent p_e) {
        // This is a hack to keep the Previous- and Next-Buttons from generating
        // selections in the component...
        regExpCombo.clearSelection();
      }

      public void focusLost(FocusEvent p_e) {
        // This is a hack to keep the Previous- and Next-Buttons from generating
        // selections in the component...
        regExpCombo.clearSelection();
      }
    });
    tk.adapt(regExpCombo, true, true);
    createRegExpContentAssist();
  }
  
  private void createRegExpContentAssist() {
    regExpContentAssistant = new SubjectControlContentAssistant();
    regExpContentAssistant.enableAutoActivation(false);
    regExpContentAssistant.enableAutoInsert(true);
    regExpContentAssistant.setContentAssistProcessor(new RegExpContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
    regExpContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    regExpContentAssistant.setRestoreCompletionProposalSize(QuickRExPlugin.getDefault().getDialogSettings()); //$NON-NLS-1$
    regExpContentAssistant.setInformationControlCreator(new IInformationControlCreator() {
      /*
       * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
       */
      public IInformationControl createInformationControl(Shell parent) {
        return new DefaultInformationControl(parent);
      }
    });
    regExpContentAssistant.install(new ComboContentAssistSubjectAdapter(regExpCombo));
    IHandler handler = new AbstractHandler() {
      public Object execute(Map parameterValuesByName) throws ExecutionException {
        regExpContentAssistant.showPossibleCompletions();
        return null;
      }
    };
    ContentAssistHandler.createHandlerForCombo(regExpCombo, regExpContentAssistant);
  }

  private void makeActions() {
    organizeREsAction = new OrganizeREsAction();

    organizeTestTextsAction = new OrganizeTestTextsAction();

    useJDKREAction = new UseJDKREAction();

    useOROPerlREAction = new UseOROPerlREAction();

    useOROAWKAction = new UseOROAwkREAction();

    useJRegexAction = new UseJRegexAction();
    
    useJakartaRegexpAction = new UseJakartaRegexpAction();
    
    keepREAction = new KeepREAction();

    saveTextAction = new SaveTestTextAction();

    loadTextAction = new LoadTestTextAction();

    jcopyAction = new JCopyAction();

    grepAction = new GrepAction();
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
    fillToolBar(toolbar);
  }

  private void fillToolBar(IToolBarManager manager) {
    manager.add(useJDKREAction);
    manager.add(useOROPerlREAction);
    manager.add(useOROAWKAction);
    manager.add(useJRegexAction);
    manager.add(useJakartaRegexpAction);
    manager.add(new Separator("UseRESeparator")); //$NON-NLS-1$
    manager.add(jcopyAction);
    manager.add(grepAction);
    manager.add(new Separator("StoreHandleSeparator1")); //$NON-NLS-1$
    manager.add(keepREAction);
    manager.add(saveTextAction);
    manager.add(loadTextAction);
  }

  private void fillLocalPullDown(IMenuManager manager) {
    manager.add(useJDKREAction);
    manager.add(useOROPerlREAction);
    manager.add(useOROAWKAction);
    manager.add(useJRegexAction);
    manager.add(useJakartaRegexpAction);
    manager.add(new Separator("UseRESeparator")); //$NON-NLS-1$
    manager.add(jcopyAction);
    manager.add(grepAction);
    manager.add(new Separator("StoreHandleSeparator1")); //$NON-NLS-1$
    manager.add(keepREAction);
    manager.add(saveTextAction);
    manager.add(loadTextAction);
    manager.add(new Separator("StoreHandleSeparator2")); //$NON-NLS-1$
    manager.add(organizeREsAction);
    manager.add(organizeTestTextsAction);
  }

  private void redrawFourthLine() {
    nextButton.redraw();
    previousButton.redraw();
    matches.redraw();
  }

  private void redrawFifthLine() {
    nextGroupButton.redraw();
    previousGroupButton.redraw();
    groups.redraw();
  }

  private ITextEditor getActiveEditor() {
    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof ITextEditor) {
      return (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    } else {
      return null;
    }
  }

  public void setUseJavaRE() {
    QuickRExPlugin.getDefault().useJavaRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  public void setUseOROPerlRE() {
    QuickRExPlugin.getDefault().useOROPerlRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  public void setUseOROAwkRE() {
    QuickRExPlugin.getDefault().useOROAwkRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }
  
  public void setUseJRegex() {
    QuickRExPlugin.getDefault().useJRegex();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  public void setUseJakartaRegexp() {
    QuickRExPlugin.getDefault().useJakartaRegexp();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  public void handleOrganizeREs() {
    OrganizeREsDialog dlg = new OrganizeREsDialog(getSite().getShell());
    dlg.open();
    regExpCombo.setItems(QuickRExPlugin.getDefault().getRegularExpressions());
  }

  public void handleOrganizeTexts() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_ORGANIZE);
    dlg.open();
  }

  public void handleGrepButtonPressed() {
    SimpleTextDialog dlg = new SimpleTextDialog(getSite().getShell(), Messages.getString("views.QuickRExView.dlg.title"), hits.grep()); //$NON-NLS-1$
    dlg.open();
  }

  public void handleCopyButtonPressed() {
    copyToEditor(StringUtils.escapeForJava(regExpCombo.getText()));
  }

  private void copyToEditor(String string) {
    try {
      int currentOffset = ((ITextSelection)getActiveEditor().getSelectionProvider().getSelection()).getOffset();
      int currentLength = ((ITextSelection)getActiveEditor().getSelectionProvider().getSelection()).getLength();
      getActiveEditor().getDocumentProvider().getDocument(getActiveEditor().getEditorInput()).replace(currentOffset, currentLength, string);
      getActiveEditor().getSelectionProvider().setSelection(new TextSelection(currentOffset, string.length()));
    } catch (Throwable t) {
      // nop...
    }
  }

  public void handleLoadTextButtonPressed() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_LOAD);
    dlg.open();
    if (dlg.getSelectedText() != null) {
      testText.setText(dlg.getSelectedText().getText());
    }
  }

  public void handleSaveTextButtonPressed() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_SAVE);
    dlg.setTextToSave(testText.getText());
    dlg.open();
    if (dlg.getSaveInformation() != null) {
      QuickRExPlugin.getDefault().addTestText(dlg.getSaveInformation());
    }
  }

  private void handleNextGroupButtonPressed() {
    hits.getCurrentMatch().toNextGroup();
    groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[] { new Integer(hits.getCurrentMatch().getNumberOfGroups()), //$NON-NLS-1$
        fetchGroupID(), hits.getCurrentMatch().getCurrentGroup().getText() }));
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
    updateMatchView(hits.getCurrentMatch());
  }

  private void handlePreviousGroupButtonPressed() {
    hits.getCurrentMatch().toPreviousGroup();
    groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[] { new Integer(hits.getCurrentMatch().getNumberOfGroups()), //$NON-NLS-1$
        fetchGroupID(), hits.getCurrentMatch().getCurrentGroup().getText() }));
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
    updateMatchView(hits.getCurrentMatch());
  }

  private void handleNextButtonPressed() {
    hits.toNextMatch();
    Match match = hits.getCurrentMatch();
    matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[] { new Integer(hits.getNumberOfMatches()), //$NON-NLS-1$
        new Integer(match.getStart()), new Integer(match.getEnd()) }));
    updateMatchView(match);
    nextButton.setEnabled(hits.hasNextMatch());
    previousButton.setEnabled(hits.hasPreviousMatch());
    if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
      groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[] { new Integer(hits.getCurrentMatch().getNumberOfGroups()), //$NON-NLS-1$
          fetchGroupID(), hits.getCurrentMatch().getCurrentGroup().getText() }));
    } else {
      groups.setText(Messages.getString("views.QuickRExView.result.group.none")); //$NON-NLS-1$
    }
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
  }

  private void handlePreviousButtonPressed() {
    hits.toPreviousMatch();
    Match match = hits.getCurrentMatch();
    matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[] { new Integer(hits.getNumberOfMatches()), //$NON-NLS-1$
        new Integer(match.getStart()), new Integer(match.getEnd()) }));
    updateMatchView(match);
    nextButton.setEnabled(hits.hasNextMatch());
    previousButton.setEnabled(hits.hasPreviousMatch());
    if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
      groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[] { new Integer(hits.getCurrentMatch().getNumberOfGroups()), //$NON-NLS-1$
          fetchGroupID(), hits.getCurrentMatch().getCurrentGroup().getText() }));
    } else {
      groups.setText(Messages.getString("views.QuickRExView.result.group.none")); //$NON-NLS-1$
    }
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
  }

  private void handleTestTextModified() {
    updateEvaluation();
  }

  private void handleRegExpModified() {
    updateEvaluation();
  }

  private void updateMatchView(Match match) {
    updateMatchView(match, true);
  }

  private void updateMatchView(Match match, boolean evaluated) {
    testText.setStyleRange(new StyleRange(0, testText.getText().length(), null, null));
    if (!evaluated) {
      testText.setBackground(JFaceResources.getColorRegistry().get(NOT_EVALUATED_BG_COLOR_KEY));
    } else {
      testText.setBackground(null);
    }
    if (hits.getAllMatches() != null && hits.getAllMatches().length > 0) {
      testText.setStyleRanges(getStyleRanges(hits.getAllMatches()));
    }
    if (match != null) {
      testText.setStyleRange(new StyleRange(match.getStart(), match.getEnd() - match.getStart(), JFaceResources.getColorRegistry().get(
          CURRENT_MATCH_FG_COLOR_KEY), JFaceResources.getColorRegistry().get(CURRENT_MATCH_BG_COLOR_KEY), SWT.NORMAL));
      if (match.getCurrentGroup() != null && match.getCurrentGroup().getStart() >= 0) {
        testText.setStyleRange(new StyleRange(match.getCurrentGroup().getStart(), match.getCurrentGroup().getEnd()
            - match.getCurrentGroup().getStart(), JFaceResources.getColorRegistry().get(CURRENT_MATCH_FG_COLOR_KEY), JFaceResources
            .getColorRegistry().get(CURRENT_MATCH_BG_COLOR_KEY), SWT.BOLD));
      }
      // scroll horizontally if needed
      testText.setTopIndex(testText.getLineAtOffset(match.getStart()));
    }
  }

  private StyleRange[] getStyleRanges(Match[] p_matches) {
    StyleRange[] ranges = new StyleRange[p_matches.length];
    for (int i = 0; i < p_matches.length; i++) {
      ranges[i] = new StyleRange(p_matches[i].getStart(), p_matches[i].getEnd() - p_matches[i].getStart(), JFaceResources.getColorRegistry().get(
          MATCH_FG_COLOR_KEY), JFaceResources.getColorRegistry().get(MATCH_BG_COLOR_KEY));
    }
    return ranges;
  }

  private void updateEvaluation() {
    if (liveEval) {
      Point selection = regExpCombo.getSelection();
      evaluate();
      regExpCombo.setSelection(selection);
    } else {
      Point selection = regExpCombo.getSelection();
      hits.reset();
      matches.setText(Messages.getString("views.QuickRExView.matches.notEvaluated.text")); //$NON-NLS-1$
      groups.setText(""); //$NON-NLS-1$
      globalMatch.setText(""); //$NON-NLS-1$
      updateMatchView(null, false);
      nextButton.setEnabled(false);
      previousButton.setEnabled(false);
      nextGroupButton.setEnabled(false);
      previousGroupButton.setEnabled(false);
      regExpCombo.setSelection(selection);
    }
  }

  private void evaluate() {
    if (regExpCombo.getText() != null && testText.getText() != null) {
      matches.setForeground(null);
      matches.setText(""); //$NON-NLS-1$
      groups.setText(""); //$NON-NLS-1$
      
      final Boolean[] canceledEvaluation = new Boolean[1];
      try {
        final String sRegExpCombo = regExpCombo.getText();
        final String sTestText = testText.getText();
        getSite().getWorkbenchWindow().getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {
          public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

            // set progress-thread to low priority - so the workbench has a higher priority
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY + 1);

            final boolean dontStop = !QuickRExPlugin.getDefault().getPreferenceStore().getBoolean(QuickRExPreferencesPage.P_DO_TIMEOUT);
            long stopAfterSeconds = QuickRExPlugin.getDefault().getPreferenceStore().getLong(QuickRExPreferencesPage.P_TIMEOUT);
            final long maxTime = System.currentTimeMillis() + 1000 * stopAfterSeconds;

            // create a processing thread with low priority
            Thread regExpThread = new Thread("QuickREx-Processing") { //$NON-NLS-1$
              public void run() {
                try {
                  hits.init(sRegExpCombo, sTestText, currentFlags);
                } catch (Throwable throwable) {
                  hits.setException(throwable);
                }
              }
            };
            regExpThread.setPriority(Thread.MIN_PRIORITY);
            regExpThread.start();

            // control the regExpThread and kill him if time exceeded or user canceled process
            boolean monitorHasTask = false;
            while (regExpThread.isAlive() && (dontStop || System.currentTimeMillis() < maxTime) && monitor.isCanceled() == false) {

              // do nothing
              Thread.yield();

              // update progressBar
              if (monitorHasTask == false) {
                monitor.beginTask(Messages.getString("views.QuickRExView.tasks.processing.name"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                monitorHasTask = true;
              }
            }

            if (regExpThread.isAlive()) {
              // force thread to stop
              regExpThread.stop();
              canceledEvaluation[0] = Boolean.TRUE;
            }

            monitor.done();
          }
        });
      } catch (InvocationTargetException ex) {
        ex.printStackTrace();

      } catch (InterruptedException ex) {
        canceledEvaluation[0] = Boolean.TRUE;
      }

      if (Boolean.TRUE.equals(canceledEvaluation[0])) {
        // reset hits because the thread was killed and hits has an unexpected state
        hits.reset();
        // user canceled progressDialog
        matches.setText(Messages.getString("views.QuickRExView.matches.cancelledEvaluation.text")); //$NON-NLS-1$
        groups.setText(""); //$NON-NLS-1$
        globalMatch.setText(""); //$NON-NLS-1$
        updateMatchView(null, false);
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextGroupButton.setEnabled(false);
        previousGroupButton.setEnabled(false);
        return;
      }

      if (hits.containsException()) {
        Throwable t = hits.getException();        
        if(t instanceof PatternSyntaxException) {
          matches.setText(Messages.getString("views.QuickRExView.result.match.illegalPattern", new Object[]{StringUtils.firstLine(t.getMessage())})); //$NON-NLS-1$
        } else {
          String msg = t.getMessage();
          if(msg == null) msg = t.toString();
          matches.setText(Messages.getString("views.QuickRExView.result.match.parserException", new Object[]{msg})); //$NON-NLS-1$          
        }
        
        matches.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
        hits.reset();
        updateMatchView(null);
        regExpCombo.setFocus();
        groups.setText(""); //$NON-NLS-1$
        globalMatch.setText(""); //$NON-NLS-1$
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextGroupButton.setEnabled(false);
        previousGroupButton.setEnabled(false);
      } else if (hits.containsMatches()) {
        Match match = hits.getCurrentMatch();
        updateMatchView(match);
        matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[] { new Integer(hits.getNumberOfMatches()), //$NON-NLS-1$
            new Integer(match.getStart()), new Integer(match.getEnd()) }));
        globalMatch.setText(Messages.getString("views.QuickRExView.result.globalMatch", new Object[] { new Boolean(hits.isGlobalMatch())})); //$NON-NLS-1$
        nextButton.setEnabled(hits.hasNextMatch());
        previousButton.setEnabled(hits.hasPreviousMatch());
        if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
          groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[] { new Integer(hits.getCurrentMatch().getNumberOfGroups()), //$NON-NLS-1$
              fetchGroupID(), hits.getCurrentMatch().getCurrentGroup().getText() }));
        } else {
          groups.setText(Messages.getString("views.QuickRExView.result.group.none")); //$NON-NLS-1$
        }
        nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
        previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());        
      } else {
        updateMatchView(null);
        matches.setText(Messages.getString("views.QuickRExView.result.match.none")); //$NON-NLS-1$
        groups.setText(""); //$NON-NLS-1$
        globalMatch.setText(Messages.getString("views.QuickRExView.result.globalMatch", new Object[] { new Boolean(hits.isGlobalMatch())})); //$NON-NLS-1$
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextGroupButton.setEnabled(false);
        previousGroupButton.setEnabled(false);
      }
      redrawFourthLine();
      redrawFifthLine();
    }
  }

  private String fetchGroupID() {
    int index = hits.getCurrentMatch().getCurrentGroup().getIndex();
    String ret = ""+index;

    String groupID = hits.getCurrentMatch().getCurrentGroup().getID();
    if(groupID != null)
      ret +=" - {" + groupID + "}";
    
    return ret;
  }

  public void handleKeepButtonPressed() {
    regExpCombo.add(regExpCombo.getText(), 0);
    QuickRExPlugin.getDefault().addRegularExpression(new RegularExpression(regExpCombo.getText()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  public void setFocus() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    QuickRExPlugin.getDefault().saveSelectedFlagValues(currentFlags);
    super.dispose();
  }

  public void setRegularExpression(String re) {
    regExpCombo.setText(re);
  }

  public void setTestText(String text) {
    testText.setText(text);
  }
}