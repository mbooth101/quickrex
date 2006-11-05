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
package de.babe.eclipse.plugins.quickREx.views;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.contentassist.ComboContentAssistSubjectAdapter;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import de.babe.eclipse.plugins.quickREx.Messages;
import de.babe.eclipse.plugins.quickREx.PluginImageRegistry;
import de.babe.eclipse.plugins.quickREx.QuickRExPlugin;
import de.babe.eclipse.plugins.quickREx.StringUtils;
import de.babe.eclipse.plugins.quickREx.dialogs.OrganizeREsDialog;
import de.babe.eclipse.plugins.quickREx.dialogs.OrganizeTestTextDialog;
import de.babe.eclipse.plugins.quickREx.dialogs.SimpleTextDialog;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpression;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.Match;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory;
import de.babe.eclipse.plugins.quickREx.regexp.RegExpContentAssistProcessor;
import de.babe.eclipse.plugins.quickREx.regexp.RegularExpressionHits;

/**
 * @author bastian.bergerhoff, andreas.studer
 */
public class QuickRExView extends ViewPart {
  private Combo regExpCombo;

  private StyledText testText;

  private Label matches;

  private Label groups;

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

  private static final String EDITOR_FONT_KEY = "de.babe.eclipse.plugins.QuickREx.textfontDefinition"; //$NON-NLS-1$

  private SubjectControlContentAssistant regExpContentAssistant;

  private HandlerSubmission regExpContentAssistantHandlerSubmission;

  private Action useJDKREAction;

  private Action useOROPerlREAction;

  private Action useOROAWKAction;

//  private Collection currentFlagsJDK = new Vector();
//
//  private Collection currentFlagsOROAwk = new Vector();
//
//  private Collection currentFlagsOROPerl = new Vector();

  private Collection currentFlags = new Vector();

  private int curSize;
  
  private String msg = ""; //$NON-NLS-1$

  private Action keepREAction;

  private Action saveTextAction;

  private Action loadTextAction;

  private Action jcopyAction;

  private Action grepAction;

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
    section.setDescription(Messages.getString("views.QuickRExView.global.flags.description")+msg); //$NON-NLS-1$
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        form.redraw();
      }
    });
    Composite client = tk.createComposite(section);
    GridLayout layout = new GridLayout();
    layout.numColumns = MatchSetFactory.getMaxFlagColumns()+1;
    client.setLayout(layout);
    gd = new GridData();
    gd.horizontalSpan = 1;
    gd.grabExcessHorizontalSpace = true;
    client.setLayoutData(gd);

    createFlagFlavourSection(tk,client,layout,gd,Messages.getString("views.QuickRExView.jdk.flags"), MatchSetFactory.JAVA_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk,client,layout,gd,Messages.getString("views.QuickRExView.perl.flags"), MatchSetFactory.ORO_PERL_FLAVOUR); //$NON-NLS-1$
    createFlagFlavourSection(tk,client,layout,gd,Messages.getString("views.QuickRExView.awk.flags"), MatchSetFactory.ORO_AWK_FLAVOUR); //$NON-NLS-1$

    section.setClient(client);
  }

  /*
   * Creates a line of flags. This is a helper for the Method createFlagSection.
   * @param tk      The FormToolkit to use
   * @param client  The Composite Client
   * @param layout  The GridLayout to use.
   * @param gd      The GridData to fill.
   * @param text    The text for the labe at the beginning.
   * @param flavour The Flavour to use from MatchSetFactory
   * @see de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory
   */
  private void createFlagFlavourSection(FormToolkit tk, Composite client, GridLayout layout, GridData gd, String text, int flavour)
  {
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

  private void createFourthRow(FormToolkit tk, Form form) {
    GridData gd;
    // Fourth row...
    Label groupsLabel = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.forthrow.label")); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    groupsLabel.setLayoutData(gd);
    previousGroupButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.forthrow.prev"), SWT.PUSH); //$NON-NLS-1$
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
    nextGroupButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.forthrow.next"), SWT.PUSH); //$NON-NLS-1$
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

  private void createThirdRow(FormToolkit tk, Form form) {
    GridData gd;
    // Third row...
    Label regExpResult = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.thridrow.label")); //$NON-NLS-1$
    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    regExpResult.setLayoutData(gd);
    previousButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.thridrow.prev"), SWT.PUSH); //$NON-NLS-1$
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
    nextButton = tk.createButton(form.getBody(), Messages.getString("views.QuickRExView.thirdrow.next"), SWT.PUSH); //$NON-NLS-1$
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
    matches = tk.createLabel(form.getBody(), Messages.getString("views.QuickRExView.thirdrow.message")); //$NON-NLS-1$
    gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    matches.setLayoutData(gd);
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
    regExpContentAssistantHandlerSubmission = new HandlerSubmission(null, regExpCombo.getShell(), null,
        ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, handler, Priority.MEDIUM);
    IWorkbenchCommandSupport commandSupport = PlatformUI.getWorkbench().getCommandSupport();
    commandSupport.addHandlerSubmission(regExpContentAssistantHandlerSubmission);
  }

  private void makeActions() {
    organizeREsAction = new Action() {
      public void run() {
        handleOrganizeREs();
      }
    };
    organizeREsAction.setText(Messages.getString("views.QuickRExView.organizeREsAction.text")); //$NON-NLS-1$
    organizeREsAction.setToolTipText(Messages.getString("views.QuickRExView.organizeREsAction.tooltip")); //$NON-NLS-1$
    organizeREsAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_ORGANIZE_RES));

    organizeTestTextsAction = new Action() {
      public void run() {
        handleOrganizeTexts();
      }
    };
    organizeTestTextsAction.setText(Messages.getString("views.QuickRExView.organizeTestTextsAction.text")); //$NON-NLS-1$
    organizeTestTextsAction.setToolTipText(Messages.getString("views.QuickRExView.organizeTestTextsAction.tooltip")); //$NON-NLS-1$
    organizeTestTextsAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_ORGANIZE_TEXTS));

    useJDKREAction = new Action("", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
      public void run() {
        if (isChecked()) {
          setUseJavaRE();
        }
      }
    };
    useJDKREAction.setText(Messages.getString("views.QuickRExView.useJDKREAction.text")); //$NON-NLS-1$
    useJDKREAction.setToolTipText(Messages.getString("views.QuickRExView.useJDKREAction.text.tooltip")); //$NON-NLS-1$
    useJDKREAction.setChecked(QuickRExPlugin.getDefault().isUsingJavaRE());
    useJDKREAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_JAVA_LOGO));

    useOROPerlREAction = new Action("", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
      public void run() {
        if (isChecked()) {
          setUseOROPerlRE();
        }
      }
    };
    useOROPerlREAction.setText(Messages.getString("views.QuickRExView.useOROPerlREAction.text")); //$NON-NLS-1$
    useOROPerlREAction.setToolTipText(Messages.getString("views.QuickRExView.useOROPerlREAction.tooltip")); //$NON-NLS-1$
    useOROPerlREAction.setChecked(QuickRExPlugin.getDefault().isUsingOROPerlRE());
    useOROPerlREAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_ORO_PERL_LOGO));

    useOROAWKAction = new Action("", IAction.AS_RADIO_BUTTON) { //$NON-NLS-1$
      public void run() {
        if (isChecked()) {
          setUseOROAwkRE();
        }
      }
    };
    useOROAWKAction.setText(Messages.getString("views.QuickRExView.useOROAWKAction.text")); //$NON-NLS-1$
    useOROAWKAction.setToolTipText(Messages.getString("views.QuickRExView.useOROAWKAction.tooltip")); //$NON-NLS-1$
    useOROAWKAction.setChecked(QuickRExPlugin.getDefault().isUsingOROAwkRE());
    useOROAWKAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_ORO_AWK_LOGO));
    
    keepREAction = new Action() {
      public void run() {
        handleKeepButtonPressed();
      }
    };
    keepREAction.setText(Messages.getString("views.QuickRExView.keepREAction.text")); //$NON-NLS-1$
    keepREAction.setToolTipText(Messages.getString("views.QuickRExView.keepREAction.tooltip")); //$NON-NLS-1$
    keepREAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_KEEP_RE));
        
    saveTextAction = new Action() {
      public void run() {
        handleSaveTextButtonPressed();
      }
    };
    saveTextAction.setText(Messages.getString("views.QuickRExView.saveTextAction.text")); //$NON-NLS-1$
    saveTextAction.setToolTipText(Messages.getString("views.QuickRExView.saveTextAction.tooltip")); //$NON-NLS-1$
    saveTextAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_SAVE_TT));
    
    loadTextAction = new Action() {
      public void run() {
        handleLoadTextButtonPressed();
      }
    };
    loadTextAction.setText(Messages.getString("views.QuickRExView.loadTextAction.text")); //$NON-NLS-1$
    loadTextAction.setToolTipText(Messages.getString("views.QuickRExView.loadTextAction.tooltip")); //$NON-NLS-1$
    loadTextAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_LOAD_TT));
    
    jcopyAction = new Action() {
      public void run() {
        handleCopyButtonPressed();
      }
    };
    jcopyAction.setText(Messages.getString("views.QuickRExView.jcopyAction.text")); //$NON-NLS-1$
    jcopyAction.setToolTipText(Messages.getString("views.QuickRExView.jcopyAction.tooltip")); //$NON-NLS-1$
    jcopyAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_JCOPY));
    
    grepAction = new Action() {
      public void run() {
        handleGrepButtonPressed();
      }
    };
    grepAction.setText(Messages.getString("views.QuickRExView.grepAction.text")); //$NON-NLS-1$
    grepAction.setToolTipText(Messages.getString("views.QuickRExView.grepAction.tooltip")); //$NON-NLS-1$
    grepAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_GREP));
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

  private void showMessage(String p_title, String p_message) {
    MessageDialog.openInformation(getSite().getShell(), p_title, p_message);
  }

  private void redrawThirdLine() {
    nextButton.redraw();
    previousButton.redraw();
    matches.redraw();
  }

  private void redrawFourthLine() {
    nextGroupButton.redraw();
    previousGroupButton.redraw();
    groups.redraw();
  }

  private ITextEditor getActiveEditor() {
    IEditorPart ePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof ITextEditor) {
      return (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    } else {
      return null;
    }
  }

  protected void setUseJavaRE() {
    QuickRExPlugin.getDefault().useJavaRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  protected void setUseOROPerlRE() {
    QuickRExPlugin.getDefault().useOROPerlRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  protected void setUseOROAwkRE() {
    QuickRExPlugin.getDefault().useOROAwkRE();
    // This is a hack since there is no direct way of getting rid of the
    // completion-proposal popup...
    String oldRegExp = regExpCombo.getText();
    regExpCombo.setText(oldRegExp + " "); //$NON-NLS-1$
    regExpCombo.setText(oldRegExp);
    updateEvaluation();
  }

  private void handleOrganizeREs() {
    OrganizeREsDialog dlg = new OrganizeREsDialog(getSite().getShell());
    dlg.open();
    regExpCombo.setItems(QuickRExPlugin.getDefault().getRegularExpressions());
  }

  private void handleOrganizeTexts() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_ORGANIZE);
    dlg.open();
  }

  private void handleGrepButtonPressed() {
    SimpleTextDialog dlg = new SimpleTextDialog(getSite().getShell(), Messages.getString("views.QuickRExView.dlg.title"), hits.grep()); //$NON-NLS-1$
    dlg.open();
  }

  private void handleCopyButtonPressed() {
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

  private void handleLoadTextButtonPressed() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_LOAD);
    dlg.open();
    if (dlg.getSelectedText() != null) {
      testText.setText(dlg.getSelectedText().getText());
    }
  }

  private void handleSaveTextButtonPressed() {
    OrganizeTestTextDialog dlg = new OrganizeTestTextDialog(getSite().getShell(), OrganizeTestTextDialog.TYPE_SAVE);
    dlg.setTextToSave(testText.getText());
    dlg.open();
    if (dlg.getSaveInformation() != null) {
      QuickRExPlugin.getDefault().addTestText(dlg.getSaveInformation());
    }
  }

  private void handleNextGroupButtonPressed() {
    hits.getCurrentMatch().toNextGroup();
    //TODO changed
    groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[]{ 
    		new Integer(hits.getCurrentMatch().getNumberOfGroups()),
    		new Integer(hits.getCurrentMatch().getCurrentGroup().getIndex()),
    		hits.getCurrentMatch().getCurrentGroup().getText()
    }));
    
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
    updateMatchView(hits.getCurrentMatch());
  }

  private void handlePreviousGroupButtonPressed() {
    hits.getCurrentMatch().toPreviousGroup();
    //TODO changed
    groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[]{ 
    		new Integer(hits.getCurrentMatch().getNumberOfGroups()),
    		new Integer(hits.getCurrentMatch().getCurrentGroup().getIndex()),
    		hits.getCurrentMatch().getCurrentGroup().getText()
    }));
    
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
    updateMatchView(hits.getCurrentMatch());
  }

  private void handleNextButtonPressed() {
    hits.toNextMatch();
    Match match = hits.getCurrentMatch();
    //TODO changed
    matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[]{
    		new Integer(hits.getNumberOfMatches()),
    		new Integer(match.getStart()),
    		new Integer(match.getEnd())
    }));
    
    nextButton.setEnabled(hits.hasNextMatch());
    previousButton.setEnabled(hits.hasPreviousMatch());
    if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
        groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[]{ 
        		new Integer(hits.getCurrentMatch().getNumberOfGroups()),
        		new Integer(hits.getCurrentMatch().getCurrentGroup().getIndex()),
        		hits.getCurrentMatch().getCurrentGroup().getText()
        }));
        } else {
      groups.setText(Messages.getString("views.QuickRExView.result.group.none")); //$NON-NLS-1$
    }
    nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
    previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
  }

  private void handlePreviousButtonPressed() {
    hits.toPreviousMatch();
    Match match = hits.getCurrentMatch();
    //TODO changed
    matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[]{
    		new Integer(hits.getNumberOfMatches()),
    		new Integer(match.getStart()),
    		new Integer(match.getEnd())
    }));
    
    updateMatchView(match);
    nextButton.setEnabled(hits.hasNextMatch());
    previousButton.setEnabled(hits.hasPreviousMatch());
    if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
        groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[]{ 
        		new Integer(hits.getCurrentMatch().getNumberOfGroups()),
        		new Integer(hits.getCurrentMatch().getCurrentGroup().getIndex()),
        		hits.getCurrentMatch().getCurrentGroup().getText()
        }));
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
    testText.setStyleRange(new StyleRange(0, testText.getText().length(), null, null));
    if (hits.getAllMatches() != null && hits.getAllMatches().length > 0) {
      testText.setStyleRanges(getStyleRanges(hits.getAllMatches()));
    }
    if (match != null) {
      testText.setStyleRange(new StyleRange(match.getStart(), match.getEnd() - match.getStart(), JFaceResources.getColorRegistry().get(
          CURRENT_MATCH_FG_COLOR_KEY), JFaceResources.getColorRegistry().get(CURRENT_MATCH_BG_COLOR_KEY), SWT.NORMAL));
      if (match.getCurrentGroup() != null) {
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
    if (regExpCombo.getText() != null && testText.getText() != null) {
      try {
        matches.setForeground(null);
        // TODO
        hits.init(regExpCombo.getText(), testText.getText(), currentFlags);
        if (hits.containsMatches()) {
          Match match = hits.getCurrentMatch();
          updateMatchView(match);
          //TODO changed.
          matches.setText(Messages.getString("views.QuickRExView.result.match", new Object[]{
          		new Integer(hits.getNumberOfMatches()),
          		new Integer(match.getStart()),
          		new Integer(match.getEnd())
          }));
          nextButton.setEnabled(hits.hasNextMatch());
          previousButton.setEnabled(hits.hasPreviousMatch());
          if (hits.getCurrentMatch().getNumberOfGroups() > 0) {
              groups.setText(Messages.getString("views.QuickRExView.result.group", new Object[]{ 
              		new Integer(hits.getCurrentMatch().getNumberOfGroups()),
              		new Integer(hits.getCurrentMatch().getCurrentGroup().getIndex()),
              		hits.getCurrentMatch().getCurrentGroup().getText()
              }));
          } else {
            groups.setText(Messages.getString("views.QuickRExView.result.group.none")); //$NON-NLS-1$
          }
          nextGroupButton.setEnabled(hits.getCurrentMatch().hasNextGroup());
          previousGroupButton.setEnabled(hits.getCurrentMatch().hasPreviousGroup());
        } else {
          updateMatchView(null);
          matches.setText(Messages.getString("views.QuickRExView.result.match.none")); //$NON-NLS-1$
          groups.setText(""); //$NON-NLS-1$
          nextButton.setEnabled(false);
          previousButton.setEnabled(false);
          nextGroupButton.setEnabled(false);
          previousGroupButton.setEnabled(false);
        }
      } catch (PatternSyntaxException pse) {
    	  //TODO changed.
        matches.setText(Messages.getString("views.QuickRExView.result.match.illigal", new Object[]{StringUtils.firstLine(pse.getMessage())})); //$NON-NLS-1$
        matches.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
        hits.reset();
        updateMatchView(null);
        regExpCombo.setFocus();
        groups.setText(""); //$NON-NLS-1$
        nextButton.setEnabled(false);
        previousButton.setEnabled(false);
        nextGroupButton.setEnabled(false);
        previousGroupButton.setEnabled(false);
      }
      redrawThirdLine();
      redrawFourthLine();
    }
  }

  private void handleKeepButtonPressed() {
    regExpCombo.add(regExpCombo.getText(), 0);
    QuickRExPlugin.getDefault().addRegularExpression(new RegularExpression(regExpCombo.getText()));
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  public void setFocus() {
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    IWorkbenchCommandSupport commandSupport = PlatformUI.getWorkbench().getCommandSupport();
    commandSupport.removeHandlerSubmission(regExpContentAssistantHandlerSubmission);
    QuickRExPlugin.getDefault().saveSelectedFlagValues(currentFlags);
    super.dispose();
  }
}