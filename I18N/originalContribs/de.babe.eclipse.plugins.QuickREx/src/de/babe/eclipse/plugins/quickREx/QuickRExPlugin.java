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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.babe.eclipse.plugins.quickREx.objects.NamedText;
import de.babe.eclipse.plugins.quickREx.objects.NamedTextXMLHandler;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpression;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpressionsXMLHandler;
import de.babe.eclipse.plugins.quickREx.regexp.CompletionProposalXMLHandler;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory;

/**
 * @author bastian.bergerhoff
 */
public class QuickRExPlugin extends AbstractUIPlugin {
  // The shared instance.
  private static QuickRExPlugin plugin;

  // Resource bundle.
  private ResourceBundle resourceBundle;

  private ArrayList regularExpressions;

  private ArrayList testTexts;

  private static final String RE_FILE_NAME = "regularExpressions.xml"; //$NON-NLS-1$

  private static final String TEST_TEXT_FILE_NAME = "testTexts.xml"; //$NON-NLS-1$

  private static final String ID = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin"; //$NON-NLS-1$

  private static final String RE_FLAVOUR = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin.REFlavour"; //$NON-NLS-1$
  //TODO changed.
  private static final String ORO_AWK_PROPOSAL_FILE_NAME = "$nl$/oroAwkCompletion.xml"; //$NON-NLS-1$
  //TODO changed.
  private static final String ORO_PERL_PROPOSAL_FILE_NAME = "$nl$/oroPerlCompletion.xml"; //$NON-NLS-1$
  //TODO changed.
  private static final String JDK_PROPOSAL_FILE_NAME = "$nl$/jdkCompletion.xml"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  public QuickRExPlugin() {
    super();
    plugin = this;
    try {
      resourceBundle = ResourceBundle.getBundle("de.babe.eclipse.plugins.quickREx.QuickRExPluginResources"); //$NON-NLS-1$
    } catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
   */
  protected ImageRegistry createImageRegistry() {
    return new PluginImageRegistry(this);
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    regularExpressions = initREsFromFile();
    testTexts = initTestTextsFromFile();
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    writeREsToFile(regularExpressions);
    writeTestTextsToFile(testTexts);
  }

  /**
   * Returns the shared instance.
   */
  public static QuickRExPlugin getDefault() {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = QuickRExPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  /**
   * Returns the currently kept regular expressions (as Strings-array) or an
   * empty array. Regular Expressions are persisted to a file and loaded from
   * there on plug-in activation.
   * 
   * @return the currently kept regular expressions (as Strings-array) or an
   *         empty array
   */
  public String[] getRegularExpressions() {
    String[] retArray = new String[regularExpressions.size()];
    for (int i = 0; i < retArray.length; i++) {
      retArray[i] = ((RegularExpression)regularExpressions.get(i)).getString();
    }
    return retArray;
  }

  /**
   * Adds the passed Regular Expression to the list of Reg. Exp.s kept with the
   * plugin and persisted to a file on plugin-dectivation.
   * 
   * @param p_expression
   *          The RegularExpression to be saved
   */
  public void addRegularExpression(RegularExpression p_expression) {
    regularExpressions.add(0, p_expression);
  }

  /**
   * Returns the currently kept test-texts (as NamedTexts-array) or an empty
   * array. Test Texts are persisted to a file and loaded from there on plug-in
   * activation.
   * 
   * @return the currently kept test-texts (as NamedTexts-array) or an empty
   *         array
   */
  public NamedText[] getTestTexts() {
    return (NamedText[])testTexts.toArray(new NamedText[testTexts.size()]);
  }

  /**
   * Adds the passed NamedText to the list of Test-Texts kept with the plugin
   * and persisted to a file on plugin-dectivation.
   * 
   * @param p_expression
   *          The NamedText to be saved
   */
  public void addTestText(NamedText p_text) {
    int i = getTestTextIndexByName(p_text.getName());
    if (i > -1) {
      testTexts.remove(i);
    }
    testTexts.add(0, p_text);
  }

  /**
   * Returns the NamedText with the passed name, if existing. If no such Text
   * exists, <code>null</code> is returned.
   * 
   * @param p_name
   *          the name of the text to return
   * @return the NamedText or null
   */
  public NamedText getTestTextByName(String p_name) {
    for (Iterator iter = testTexts.iterator(); iter.hasNext();) {
      NamedText element = (NamedText)iter.next();
      if (p_name.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if and only if a NamedText with the passed name
   * is among the texts currently held with the plugin.
   * 
   * @param p_name
   *          the name of the text which should be looked for
   * @return <code>true</code> if a text with the passed name exists
   */
  public boolean testTextNameExists(String p_name) {
    return getTestTextIndexByName(p_name) > -1;
  }

  /**
   * Returns a String-array with all names of saved texts.
   * 
   * @return an array with test-text names (or an empty array if no test-texts
   *         are saved)
   */
  public String[] getTestTextNames() {
    String[] retArray = new String[testTexts.size()];
    for (int i = 0; i < retArray.length; i++) {
      retArray[i] = ((NamedText)testTexts.get(i)).getName();
    }
    return retArray;
  }

  /**
   * Deletes the test-text with the passed name from the list of test-texts
   * saved.
   * 
   * param p_name the name of the text which should be deleted
   */
  public void deleteTestTextByName(String p_name) {
    int index = getTestTextIndexByName(p_name);
    testTexts.remove(index);
  }

  /**
   * Deletes all RegularExpressions with String-values among the Strings passed
   * in the array from the list of Regular-Expressions saved with the plugin
   * 
   * @param p_regExps
   *          the String-representations of Reg. Exp.s to be removed from memory
   */
  public void deleteRegularExpressions(String[] p_regExps) {
    for (int i = 0; i < p_regExps.length; i++) {
      deleteRegularExpression(p_regExps[i]);
    }
  }

  private ArrayList initREsFromFile() {
    IPath reFilePath = getStateLocation().append(RE_FILE_NAME);
    File reFile = reFilePath.toFile();
    if (reFile.exists() && reFile.canRead()) {
      ArrayList res = new ArrayList();
      try {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(reFile, new RegularExpressionsXMLHandler(res));
      } catch (Exception ex) {
        // nop, to be save
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message1"), ex); //$NON-NLS-1$
        getLog().log(status);
      }
      return res;
    } else {
      try {
        reFile.createNewFile();
      } catch (IOException e) {
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message2"), null); //$NON-NLS-1$
        getLog().log(status);
      }
      return new ArrayList();
    }
  }

  private ArrayList initTestTextsFromFile() {
    IPath ttFilePath = getStateLocation().append(TEST_TEXT_FILE_NAME);
    File ttFile = ttFilePath.toFile();
    if (ttFile.exists() && ttFile.canRead()) {
      ArrayList res = new ArrayList();
      try {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(ttFile, new NamedTextXMLHandler(res));
      } catch (Exception ex) {
        // nop, to be save
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message3"), ex); //$NON-NLS-1$
        getLog().log(status);
      }
      return res;
    } else {
      try {
        ttFile.createNewFile();
      } catch (IOException e) {
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message4"), null); //$NON-NLS-1$
        getLog().log(status);
      }
      return new ArrayList();
    }
  }

  private void writeREsToFile(ArrayList p_regularExpressions) {
    IPath reFilePath = getStateLocation().append(RE_FILE_NAME);
    File reFile = reFilePath.toFile();
    try {
      FileOutputStream fos = new FileOutputStream(reFile);
      fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<regularExpressions>\r\n".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator it = p_regularExpressions.iterator();
      while (it.hasNext()) {
        fos.write(((RegularExpression)it.next()).toXMLString("\t").getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      }
      fos.write("</regularExpressions>".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      fos.close();
    } catch (Exception e) {
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message5"), e); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  private void writeTestTextsToFile(ArrayList p_testTexts) {
    IPath ttFilePath = getStateLocation().append(TEST_TEXT_FILE_NAME);
    File reFile = ttFilePath.toFile();
    try {
      FileOutputStream fos = new FileOutputStream(reFile);
      fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<testTexts>\r\n".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator it = p_testTexts.iterator();
      while (it.hasNext()) {
        fos.write(((NamedText)it.next()).toXMLString("\t").getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      }
      fos.write("</testTexts>".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      fos.close();
    } catch (Exception e) {
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message6"), e); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  private int getTestTextIndexByName(String p_name) {
    int i = 0;
    for (Iterator iter = testTexts.iterator(); iter.hasNext();) {
      NamedText element = (NamedText)iter.next();
      if (p_name.equals(element.getName())) {
        return i;
      }
      i++;
    }
    return -1;
  }

  private void deleteRegularExpression(String p_string) {
    int index = getRegularExpressionIndexByString(p_string);
    regularExpressions.remove(index);
  }

  private int getRegularExpressionIndexByString(String p_string) {
    int i = 0;
    for (Iterator iter = regularExpressions.iterator(); iter.hasNext();) {
      RegularExpression element = (RegularExpression)iter.next();
      if (p_string.equals(element.getString())) {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Returns <code>true</code> if and only if currently the JDK-implementation of
   * regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the JDK-implementation of
   *    regular expressions is used
   */
  public boolean isUsingJavaRE() {
    return getREFlavour() == MatchSetFactory.JAVA_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the ORO-Perl-implementation of
   * regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the ORO-Perl-implementation of
   *    regular expressions is used
   */
  public boolean isUsingOROPerlRE() {
    return getREFlavour() == MatchSetFactory.ORO_PERL_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the ORO-Awk-implementation of
   * regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the ORO-Awk-implementation of
   *    regular expressions is used
   */
  public boolean isUsingOROAwkRE() {
    return getREFlavour() == MatchSetFactory.ORO_AWK_FLAVOUR;
  }

  /**
   * Tells the Plugin to use the JDK-implementation of regular expressions.
   */
  public void useJavaRE() {
    getPreferenceStore().setValue(RE_FLAVOUR, MatchSetFactory.JAVA_FLAVOUR);
  }

  /**
   * Tells the Plugin to use the ORO-Perl-implementation of regular expressions.
   */
  public void useOROPerlRE() {
    getPreferenceStore().setValue(RE_FLAVOUR, MatchSetFactory.ORO_PERL_FLAVOUR);
  }

  /**
   * Tells the Plugin to use the ORO-Awk-implementation of regular expressions.
   */
  public void useOROAwkRE() {
    getPreferenceStore().setValue(RE_FLAVOUR, MatchSetFactory.ORO_AWK_FLAVOUR);
  }

  /**
   * Returns the currently used RE-implementation-flavour (actually, a flag corresponding 
   * to it as defined in MatchSetFactory).
   * 
   * @return the currently used RE-implementation-flavour
   */
  public int getREFlavour() {
    int flavour = getPreferenceStore().getInt(RE_FLAVOUR);
    if (flavour == 0) {
      // default: JAVA
      return MatchSetFactory.JAVA_FLAVOUR;
    } else {
      return flavour;
    }
  }

  /**
   * Reads in the JDK-Completion-proposals file and creates the proposal-structures
   * in the passed Collections/Maps.
   *  
   * @param p_proposals a HashMap to be filled with the proposals
   * @param p_keys an ArrayList to be filled with the keys
   */
  public void initJDKCompletionsFromFile(HashMap p_proposals, ArrayList p_keys) {
    try {
      InputStream propFileStream = openStream(new Path(JDK_PROPOSAL_FILE_NAME), true); //TODO changed.
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(propFileStream, new CompletionProposalXMLHandler(p_proposals, p_keys));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message7"), ex); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  /**
   * Reads in theORO-Awk-Completion-proposals file and creates the proposal-structures
   * in the passed Collections/Maps.
   *  
   * @param p_proposals a HashMap to be filled with the proposals
   * @param p_keys an ArrayList to be filled with the keys
   */
  public void initOROAwkCompletionsFromFile(HashMap p_proposals, ArrayList p_keys) {
    try {
      InputStream propFileStream = openStream(new Path(ORO_AWK_PROPOSAL_FILE_NAME), true); //TODO changed.
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(propFileStream, new CompletionProposalXMLHandler(p_proposals, p_keys));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message8"), ex); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  /**
   * Reads in the ORO-Perl-Completion-proposals file and creates the proposal-structures
   * in the passed Collections/Maps.
   *  
   * @param p_proposals a HashMap to be filled with the proposals
   * @param p_keys an ArrayList to be filled with the keys
   */
  public void initOROPerlCompletionsFromFile(HashMap p_proposals, ArrayList p_keys) {
    try {
      InputStream propFileStream = openStream(new Path(ORO_PERL_PROPOSAL_FILE_NAME), true); //TODO changed
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(propFileStream, new CompletionProposalXMLHandler(p_proposals, p_keys));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString("QuickRExPlugin.error.message9"), ex); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  /**
   * Saves the values of all flags to the PreferenceStore, where any flag
   * contained in the passed collection is saved as 'set', any flag known 
   * to the MatchSetFactory but not contained in the passed Collection is
   * saved as 'not set'.
   * 
   * @param flags a Collection holding the actually set flags
   */
  public void saveSelectedFlagValues(Collection flags) {
    Collection allSupported = MatchSetFactory.getAllSupportedFlags();
    for (Iterator iter = allSupported.iterator(); iter.hasNext();) {
      Flag element = (Flag)iter.next();
      getPreferenceStore().setValue(element.getCode(), flags.contains(element));
    }
  }

  /**
   * Returns <code>true</code> if and only if the passed Flag is saved
   * as 'set' in the PreferenceStore.
   * 
   * @param flag the flag to check for
   * @return the state for the flag in the store (set: true, not set: false)
   */
  public boolean isFlagSaved(Flag flag) {
    return getPreferenceStore().getBoolean(flag.getCode());
  }
}
