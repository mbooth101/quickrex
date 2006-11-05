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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author bastian.bergerhoff
 */
public class PluginImageRegistry extends ImageRegistry {

  private static URL iconBaseURL = null;

  public final static String IMG_ORGANIZE_RES = "IMG_ORGANIZE_RES";
  
  public final static String IMG_ORGANIZE_TEXTS = "IMG_ORGANIZE_TEXTS";

  public static final String IMG_JAVA_LOGO = "IMG_JAVA_LOGO";

  public static final String IMG_ORO_PERL_LOGO = "IMG_ORO_PERL_LOGO";

  public static final String IMG_ORO_AWK_LOGO = "IMG_ORO_AWK_LOGO";

  public static final String IMG_KEEP_RE = "IMG_KEEP_RE";

  public static final String IMG_SAVE_TT = "IMG_SAVE_TT";

  public static final String IMG_LOAD_TT = "IMG_LOAD_TT";

  public static final String IMG_JCOPY = "IMG_JCOPY";

  public static final String IMG_GREP = "IMG_GREP";

  private static HashMap imagesMap = new HashMap();

  public PluginImageRegistry() {
    super();
  }

  public PluginImageRegistry(Display p_display) {
    super(p_display);
  }

  /**
   * @param p_plugin
   */
  public PluginImageRegistry(QuickRExPlugin p_plugin) {
    this();
    try {
      if (iconBaseURL == null) {
        iconBaseURL = p_plugin.find(new Path("/icons/"));
      }
      put(IMG_ORGANIZE_RES, ImageDescriptor.createFromURL(new URL(iconBaseURL, "orgREs.gif")));
      put(IMG_ORGANIZE_TEXTS, ImageDescriptor.createFromURL(new URL(iconBaseURL, "orgTestTexts.gif")));
      put(IMG_JAVA_LOGO, ImageDescriptor.createFromURL(new URL(iconBaseURL, "JavalogoSmall.gif")));
      put(IMG_ORO_PERL_LOGO, ImageDescriptor.createFromURL(new URL(iconBaseURL, "OROPerllogoSmall.gif")));
      put(IMG_ORO_AWK_LOGO, ImageDescriptor.createFromURL(new URL(iconBaseURL, "OROAwklogoSmall.gif")));
      put(IMG_KEEP_RE, ImageDescriptor.createFromURL(new URL(iconBaseURL, "saveRE.gif")));
      put(IMG_SAVE_TT, ImageDescriptor.createFromURL(new URL(iconBaseURL, "saveText.gif")));
      put(IMG_LOAD_TT, ImageDescriptor.createFromURL(new URL(iconBaseURL, "loadText.gif")));
      put(IMG_JCOPY, ImageDescriptor.createFromURL(new URL(iconBaseURL, "jcopy.gif")));
      put(IMG_GREP, ImageDescriptor.createFromURL(new URL(iconBaseURL, "grep.gif")));
    } catch (MalformedURLException me) {
      // nop
    }
  }

  public Image getImage(String p_key) {
    if (imagesMap.containsKey(p_key)) {
      return (Image)imagesMap.get(p_key);
    }
    ImageDescriptor iDesc = getDescriptor(p_key);
    if (iDesc == null) {
      return null;
    } else {
      Image img = iDesc.createImage();
      imagesMap.put(p_key, img);
      return img;
    }
  }

  public ImageDescriptor getImageDescriptor(String p_key) {
    return getDescriptor(p_key);
  }
}