/*******************************************************************************
 * Copyright (c) 2006 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation
 *******************************************************************************/
package de.babe.eclipse.applications.quickREx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import de.babe.eclipse.plugins.quickREx.PluginImageRegistry;
import de.babe.eclipse.plugins.quickREx.QuickRExPlugin;
import de.babe.eclipse.plugins.quickREx.actions.GrepAction;
import de.babe.eclipse.plugins.quickREx.actions.KeepREAction;
import de.babe.eclipse.plugins.quickREx.actions.LoadTestTextAction;
import de.babe.eclipse.plugins.quickREx.actions.OrganizeREsAction;
import de.babe.eclipse.plugins.quickREx.actions.OrganizeTestTextsAction;
import de.babe.eclipse.plugins.quickREx.actions.SaveTestTextAction;
import de.babe.eclipse.plugins.quickREx.actions.SearchREAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJDKREAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJRegexAction;
import de.babe.eclipse.plugins.quickREx.actions.UseJakartaRegexpAction;
import de.babe.eclipse.plugins.quickREx.actions.UseOROAwkREAction;
import de.babe.eclipse.plugins.quickREx.actions.UseOROPerlREAction;
import de.babe.eclipse.plugins.quickREx.views.RELibraryView;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

  private IWorkbenchAction exitAction;

  private IWorkbenchAction newWizardAction;

  private IAction useJDKREAction;

  private IAction useOROPerlREAction;

  private IAction useOROAwkREAction;

  private IAction useJRegexAction;

  private IAction useJakartaRegexpAction;

  private IAction grepAction;

  private IAction keepREAction;

  private IAction saveTestTextAction;

  private IAction loadTestTextAction;

  private IAction organizeREsAction;

  private IAction organizeTestTextAction;

  private IAction searchREAction;

  private IAction helpContentsAction;

  private IAction helpSearchAction;

  private IAction aboutAction;

  private IAction preferencesAction;

  private IAction showLibraryAction;

  public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
    super(configurer);
  }

  protected void makeActions(final IWorkbenchWindow window) {
    exitAction = ActionFactory.QUIT.create(window);
    register(exitAction);
    newWizardAction = ActionFactory.NEW.create(window);
    ((NewWizardAction)newWizardAction).setCategoryId("de.babe.eclipse.plugins.quickREx.wizardCategory");
    register(newWizardAction);
    
    useJDKREAction = new UseJDKREAction();
    register(useJDKREAction);
    useOROPerlREAction = new UseOROPerlREAction();
    register(useOROPerlREAction);
    useOROAwkREAction = new UseOROAwkREAction();
    register(useOROAwkREAction);
    useJRegexAction = new UseJRegexAction();
    register(useJRegexAction);
    useJakartaRegexpAction = new UseJakartaRegexpAction();
    register(useJakartaRegexpAction);
    grepAction = new GrepAction();
    register(grepAction);
    keepREAction = new KeepREAction();
    register(keepREAction);
    saveTestTextAction = new SaveTestTextAction();
    register(saveTestTextAction);
    loadTestTextAction = new LoadTestTextAction();
    register(loadTestTextAction);
    organizeREsAction = new OrganizeREsAction();
    register(organizeREsAction);
    organizeTestTextAction = new OrganizeTestTextsAction();
    register(organizeTestTextAction);
    
    searchREAction = new SearchREAction();
    register(searchREAction);

    helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
    register(helpContentsAction);
    helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
    register(helpSearchAction);
    aboutAction = ActionFactory.ABOUT.create(window);
    register(aboutAction);
    preferencesAction = ActionFactory.PREFERENCES.create(window);
    register(preferencesAction);

    showLibraryAction = new Action() {
      public void run() {
        try {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RELibraryView.ID);
        } catch (PartInitException e) {
          // Bad luck...
        }
      }      
    };
    showLibraryAction.setText("Open Library...");
    showLibraryAction.setToolTipText("Open the Reg.-Exp.-Libraries");
    showLibraryAction.setImageDescriptor(((PluginImageRegistry)QuickRExPlugin.getDefault().getImageRegistry())
        .getImageDescriptor(PluginImageRegistry.IMG_BOOK));
    showLibraryAction.setId("de.babe.eclipse.applications.quickREx.actions.ShowLibraryAction");
    register(showLibraryAction);
  }

  protected void fillMenuBar(IMenuManager menuBar) {
    MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
    menuBar.add(fileMenu);
    fileMenu.add(newWizardAction);
    fileMenu.add(new Separator());
    fileMenu.add(exitAction);
    
    MenuManager qrViewMenu = new MenuManager("&Evaluating", "de.babe.eclipse.applications.quickREx.qrViewMenu");
    menuBar.add(qrViewMenu);
    qrViewMenu.add(useJDKREAction);
    qrViewMenu.add(useOROPerlREAction);
    qrViewMenu.add(useOROAwkREAction);
    qrViewMenu.add(useJRegexAction);
    qrViewMenu.add(useJakartaRegexpAction);
    qrViewMenu.add(new Separator());
    qrViewMenu.add(grepAction);
    qrViewMenu.add(new Separator());
    qrViewMenu.add(keepREAction);
    qrViewMenu.add(saveTestTextAction);
    qrViewMenu.add(loadTestTextAction);
    qrViewMenu.add(new Separator());
    qrViewMenu.add(organizeREsAction);
    qrViewMenu.add(organizeTestTextAction);
    
    MenuManager reLibViewMenu = new MenuManager("&Library", "de.babe.eclipse.applications.quickREx.reLibViewMenu");
    menuBar.add(reLibViewMenu);
    reLibViewMenu.add(showLibraryAction);
    reLibViewMenu.add(new Separator());
    reLibViewMenu.add(searchREAction);

    MenuManager helpMenu = new MenuManager("&Help", "de.babe.eclipse.applications.quickREx.helpMenu");
    menuBar.add(helpMenu);
    helpMenu.add(helpContentsAction);
    helpMenu.add(helpSearchAction);
    helpMenu.add(new Separator());
    helpMenu.add(aboutAction);
    helpMenu.add(new Separator());
    helpMenu.add(preferencesAction);

    // Real bad! This is to get rid of the search-menu which is added probably due to the
    // fact that the QR-Plugin uses the search-result-view... 
    MenuManager searchMenu = new MenuManager("", "org.eclipse.search.menu");
    menuBar.add(searchMenu);
    searchMenu.setVisible(false);
    searchMenu.setRemoveAllWhenShown(true);
  }
}
