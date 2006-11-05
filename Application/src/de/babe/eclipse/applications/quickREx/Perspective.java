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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
    Activator.log("BASTIAN: Perspective.createInitialLayout called...");
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(true);

//		layout.addStandaloneView("de.babe.eclipse.plugins.quickREx.views.RELibraryView", false, IPageLayout.LEFT, 0.66f, editorArea);
    layout.addFastView("de.babe.eclipse.plugins.quickREx.views.RELibraryView");
		layout.addStandaloneView("de.babe.eclipse.plugins.quickREx.views.QuickRExView", false, IPageLayout.BOTTOM, 0.33f, editorArea);
	}
}
