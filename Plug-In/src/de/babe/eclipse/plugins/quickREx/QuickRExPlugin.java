/*******************************************************************************
 * Copyright (c) 2005, 2007 Bastian Bergerhoff and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Bastian Bergerhoff - initial API and implementation, all but:
 *     Georg Sendt - added JRegexp-related implementations
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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.babe.eclipse.plugins.quickREx.objects.NamedText;
import de.babe.eclipse.plugins.quickREx.objects.NamedTextXMLHandler;
import de.babe.eclipse.plugins.quickREx.objects.REBook;
import de.babe.eclipse.plugins.quickREx.objects.REBooksXMLHandler;
import de.babe.eclipse.plugins.quickREx.objects.RECategoriesXMLHandler;
import de.babe.eclipse.plugins.quickREx.objects.RECategory;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpression;
import de.babe.eclipse.plugins.quickREx.objects.RegularExpressionsXMLHandler;
import de.babe.eclipse.plugins.quickREx.preferences.QuickRExPreferencesPage;
import de.babe.eclipse.plugins.quickREx.regexp.CompletionProposalXMLHandler;
import de.babe.eclipse.plugins.quickREx.regexp.CompletionProposals;
import de.babe.eclipse.plugins.quickREx.regexp.EditorCategoryMappingXMLHandler;
import de.babe.eclipse.plugins.quickREx.regexp.Flag;
import de.babe.eclipse.plugins.quickREx.regexp.MatchSetFactory;
import de.babe.eclipse.plugins.quickREx.regexp.REEditorCategoryMapping;

/**
 * @author bastian.bergerhoff, georg.sendt
 */
public class QuickRExPlugin extends AbstractUIPlugin {

  private static QuickRExPlugin plugin;

  private ResourceBundle resourceBundle;

  private ArrayList regularExpressions;

  private ArrayList testTexts;

  private ArrayList reBooks;

  private ArrayList listeners;

  private HashMap jakCatMappings;

  private HashMap jdkCatMappings;

  private HashMap jRegexCatMappings;

  private HashMap oroAwkCatMappings;

  private HashMap oroPerlCatMappings;

  private ArrayList jakCategories;

  private ArrayList jdkCategories;

  private ArrayList jRegexCategories;

  private ArrayList oroAwkCategories;

  private ArrayList oroPerlCategories;

  private CompletionProposals proposals;

  private static final String RE_FILE_NAME = "regularExpressions.xml"; //$NON-NLS-1$

  private static final String RE_LIB_FILE_NAME = "$nl$/reLibrary.xml"; //$NON-NLS-1$

  private static final String RE_BOOKS_FILE_NAME = "reBooks.xml"; //$NON-NLS-1$

  private static final String TEST_TEXT_FILE_NAME = "testTexts.xml"; //$NON-NLS-1$

  public static final String ID = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin"; //$NON-NLS-1$

  public static final String EXPAND_NAVIGATION_SECTION = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin.ExpandNavigationSection"; //$NON-NLS-1$

  private static final String RE_FLAVOUR = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin.REFlavour"; //$NON-NLS-1$

  private static final String LAST_SEARCH_SCOPE = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin.LastSearchScope"; //$NON-NLS-1$

  private static final String LINK_RE_LIB_VIEW_WITH_EDITOR = "de.babe.eclipse.plugins.quickREx.QuickRExPlugin.LinkRELibViewWithEditor"; //$NON-NLS-1$

  private static final String ORO_AWK_PROPOSAL_FILE_NAME = "$nl$/oroAwkCompletion.xml"; //$NON-NLS-1$

  private static final String ORO_PERL_PROPOSAL_FILE_NAME = "$nl$/oroPerlCompletion.xml"; //$NON-NLS-1$

  private static final String JDK_PROPOSAL_FILE_NAME = "$nl$/jdkCompletion.xml"; //$NON-NLS-1$
  
  private static final String JREGEX_PROPOSAL_FILE_NAME = "$nl$/jregexCompletion.xml"; //$NON-NLS-1$

  private static final String JAKARTA_REGEX_PROPOSAL_FILE_NAME = "$nl$/jakartaRegexpCompletion.xml"; //$NON-NLS-1$

  private static final String ORO_AWK_CATEGORIES_FILE_NAME = "$nl$/oroAwkCategories.xml"; //$NON-NLS-1$

  private static final String ORO_PERL_CATEGORIES_FILE_NAME = "$nl$/oroPerlCategories.xml"; //$NON-NLS-1$

  private static final String JDK_CATEGORIES_FILE_NAME = "$nl$/jdkCategories.xml"; //$NON-NLS-1$
  
  private static final String JREGEX_CATEGORIES_FILE_NAME = "$nl$/jregexCategories.xml"; //$NON-NLS-1$

  private static final String JAKARTA_REGEX_CATEGORIES_FILE_NAME = "$nl$/jakartaRegexpCategories.xml"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  public QuickRExPlugin() {
    super();
    plugin = this;
    listeners = new ArrayList();
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
  public void start(BundleContext p_context) throws Exception {
    super.start(p_context);
    regularExpressions = initREsFromFile();
    testTexts = initTestTextsFromFile();
    reBooks = initREBooksFromFile();
    initProposals();
    prepareRegexpCategories();
  }

  private void prepareRegexpCategories() {
    jakCategories = new ArrayList();
    jakCatMappings = new HashMap();
    initCategoriesFromFile(jakCatMappings, jakCategories, MatchSetFactory.JAKARTA_REGEXP_FLAVOUR);
    addProposalsToMappings(jakCategories, jakCatMappings, MatchSetFactory.JAKARTA_REGEXP_FLAVOUR);

    jdkCategories = new ArrayList();
    jdkCatMappings = new HashMap();
    initCategoriesFromFile(jdkCatMappings, jdkCategories, MatchSetFactory.JAVA_FLAVOUR);
    addProposalsToMappings(jdkCategories, jdkCatMappings, MatchSetFactory.JAVA_FLAVOUR);

    jRegexCategories = new ArrayList();
    jRegexCatMappings = new HashMap();
    initCategoriesFromFile(jRegexCatMappings, jRegexCategories, MatchSetFactory.JREGEX_FLAVOUR);
    addProposalsToMappings(jRegexCategories, jRegexCatMappings, MatchSetFactory.JREGEX_FLAVOUR);

    oroAwkCategories = new ArrayList();
    oroAwkCatMappings = new HashMap();
    initCategoriesFromFile(oroAwkCatMappings, oroAwkCategories, MatchSetFactory.ORO_AWK_FLAVOUR);
    addProposalsToMappings(oroAwkCategories, oroAwkCatMappings, MatchSetFactory.ORO_AWK_FLAVOUR);

    oroPerlCategories = new ArrayList();
    oroPerlCatMappings = new HashMap();
    initCategoriesFromFile(oroPerlCatMappings, oroPerlCategories, MatchSetFactory.ORO_PERL_FLAVOUR);
    addProposalsToMappings(oroPerlCategories, oroPerlCatMappings, MatchSetFactory.ORO_PERL_FLAVOUR);
  }

  private void addProposalsToMappings(ArrayList p_categories, HashMap p_catMappings, int p_flavour) {
    for (Iterator iter = p_categories.iterator(); iter.hasNext();) {
      String category = (String) iter.next();
      ArrayList proposalKeys = (ArrayList)p_catMappings.get(category);
      ArrayList proposalsForCat = new ArrayList();
      for (Iterator iterator = proposalKeys.iterator(); iterator.hasNext();) {
        String currentKey = ((REEditorCategoryMapping) iterator.next()).getProposalKey();
        proposalsForCat.add(proposals.getProposal(p_flavour, currentKey));
      }
      p_catMappings.put(category, proposalsForCat);
    }
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext p_context) throws Exception {
    super.stop(p_context);
    writeREsToFile(regularExpressions);
    writeTestTextsToFile(testTexts);
    writeREBooksToFile(reBooks);
  }

  /**
   * Returns the shared instance.
   */
  public static QuickRExPlugin getDefault() {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getResourceString(String p_key) {
    ResourceBundle bundle = QuickRExPlugin.getDefault().getResourceBundle();
    try {
      return (bundle != null) ? bundle.getString(p_key) : p_key;
    } catch (MissingResourceException e) {
      return p_key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  /**
   * Returns the currently kept regular expressions (as Strings-array) or an empty array. Regular Expressions are persisted to a file and loaded from
   * there on plug-in activation.
   * 
   * @return the currently kept regular expressions (as Strings-array) or an empty array
   */
  public String[] getRegularExpressions() {
    String[] retArray = new String[regularExpressions.size()];
    for (int i = 0; i < retArray.length; i++) {
      retArray[i] = ((RegularExpression)regularExpressions.get(i)).getString();
    }
    return retArray;
  }

  /**
   * Adds the passed Regular Expression to the list of Reg. Exp.s kept with the plugin and persisted to a file on plugin-dectivation.
   * 
   * @param p_expression
   *          The RegularExpression to be saved
   */
  public void addRegularExpression(RegularExpression p_expression) {
    regularExpressions.add(0, p_expression);
  }

  /**
   * Returns the currently kept test-texts (as NamedTexts-array) or an empty array. Test Texts are persisted to a file and loaded from there on
   * plug-in activation.
   * 
   * @return the currently kept test-texts (as NamedTexts-array) or an empty array
   */
  public NamedText[] getTestTexts() {
    return (NamedText[])testTexts.toArray(new NamedText[testTexts.size()]);
  }

  /**
   * Adds the passed NamedText to the list of Test-Texts kept with the plugin and persisted to a file on plugin-dectivation.
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
   * Adds the passed REBook to the list of books currently kept in the Reg. Exp. Library.
   * 
   * @param p_book The REBook to add
   */
  public void addREBook(REBook p_book) {
    reBooks.add(p_book);
    if (listeners.size() > 0) {
      PropertyChangeEvent event = new PropertyChangeEvent(this, "reBooks", this.reBooks, null); //$NON-NLS-1$
      for (Iterator iter = listeners.iterator(); iter.hasNext();) {
        IPropertyChangeListener listener = (IPropertyChangeListener)iter.next();
        listener.propertyChange(event);
      }
    }
  }

  /**
   * Removes the passed REBook from the list of books currently kept in the Reg. Exp. Library.
   * 
   * @param p_book The REBook to remove
   */
  public void removeREBook(REBook p_book) {
    reBooks.remove(p_book);
    if (listeners.size() > 0) {
      PropertyChangeEvent event = new PropertyChangeEvent(this, "reBooks", this.reBooks, null); //$NON-NLS-1$
      for (Iterator iter = listeners.iterator(); iter.hasNext();) {
        IPropertyChangeListener listener = (IPropertyChangeListener)iter.next();
        listener.propertyChange(event);
      }
    }
  }

  /**
   * Returns the NamedText with the passed name, if existing. If no such Text exists, <code>null</code> is returned.
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
   * Returns <code>true</code> if and only if a NamedText with the passed name is among the texts currently held with the plugin.
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
   * @return an array with test-text names (or an empty array if no test-texts are saved)
   */
  public String[] getTestTextNames() {
    String[] retArray = new String[testTexts.size()];
    for (int i = 0; i < retArray.length; i++) {
      retArray[i] = ((NamedText)testTexts.get(i)).getName();
    }
    return retArray;
  }

  /**
   * Deletes the test-text with the passed name from the list of test-texts saved. param p_name the name of the text which should be deleted
   */
  public void deleteTestTextByName(String p_name) {
    int index = getTestTextIndexByName(p_name);
    testTexts.remove(index);
  }

  /**
   * Deletes all RegularExpressions with String-values among the Strings passed in the array from the list of Regular-Expressions saved with the
   * plugin
   * 
   * @param p_regExps
   *          the String-representations of Reg. Exp.s to be removed from memory
   */
  public void deleteRegularExpressions(String[] p_regExps) {
    for (int i = 0; i < p_regExps.length; i++) {
      deleteRegularExpression(p_regExps[i]);
    }
  }

  /**
   * Returns an array of all REBooks currently in the list of books in the Reg. Exp. Library.
   * Since there always is the default book, this array is never empty.
   * 
   * @return an array of all books from the library
   */
  public REBook[] getREBooks() {
    return (REBook[])reBooks.toArray(new REBook[reBooks.size()]);
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

  private ArrayList initREBooksFromFile() {
    IPath reBooksFilePath = getStateLocation().append(RE_BOOKS_FILE_NAME);
    File reBooksFile = reBooksFilePath.toFile();
    ArrayList res = new ArrayList();
    REBook standardBook = new REBook(REBook.DEFAULT_BOOK_NAME, (new Path(RE_LIB_FILE_NAME)).makeAbsolute().toString());
    standardBook.setContents(readStandardRELibraryFromFile());
    if (reBooksFile.exists() && reBooksFile.canRead()) {
      try {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(reBooksFile, new REBooksXMLHandler(res));
      } catch (Exception ex) {
        // nop, to be save
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 5, Messages.getString("QuickRExPlugin.error.message10"), ex); //$NON-NLS-1$
        getLog().log(status);
      }
      for (Iterator iter = res.iterator(); iter.hasNext();) {
        REBook element = (REBook)iter.next();
        element.setContents(readRELibraryFromFile(element.getPath()));
      }
      res.add(0, standardBook);
      return res;
    } else {
      try {
        reBooksFile.createNewFile();
      } catch (IOException e) {
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 5, Messages.getString("QuickRExPlugin.error.message11"), null); //$NON-NLS-1$
        getLog().log(status);
      }
      res.add(0, standardBook);
      return res;
    }
  }

  private ArrayList readStandardRELibraryFromFile() {
    ArrayList res = new ArrayList();
    try {
      InputStream libFileStream = openStream(new Path(RE_LIB_FILE_NAME), true);
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(libFileStream, new RECategoriesXMLHandler(res));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 4, Messages.getString("QuickRExPlugin.error.message12"), ex); //$NON-NLS-1$
      getLog().log(status);
    }
    return res;
  }

  private ArrayList readRELibraryFromFile(String p_path) {
    IPath reLibFilePath = new Path(p_path);
    File reLibFile = reLibFilePath.toFile();
    if (reLibFile.exists() && reLibFile.canRead()) {
      ArrayList res = new ArrayList();
      try {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(reLibFile, new RECategoriesXMLHandler(res));
      } catch (Exception ex) {
        // nop, to be save
        IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 6, Messages.getString("QuickRExPlugin.error.message13", new Object[] {p_path}), ex); //$NON-NLS-1$ //$NON-NLS-2$
        getLog().log(status);
      }
      return res;
    } else {
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 6, Messages.getString("QuickRExPlugin.error.message14", new Object[] {p_path}), null); //$NON-NLS-1$ //$NON-NLS-2$
      getLog().log(status);
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

  private void writeREBooksToFile(ArrayList p_reBooks) {
    IPath reBooksFilePath = getStateLocation().append(RE_BOOKS_FILE_NAME);
    File reBooksFile = reBooksFilePath.toFile();
    try {
      FileOutputStream fos = new FileOutputStream(reBooksFile);
      fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<reBooks>\r\n".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator it = p_reBooks.iterator();
      while (it.hasNext()) {
        REBook book = (REBook)it.next();
        if (!REBook.DEFAULT_BOOK_NAME.equals(book.getName())) {
          writeBookContentsToFile(book);
          fos.write(book.toXMLString("\t", 1).getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      fos.write("</reBooks>".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      fos.close();
    } catch (Exception e) {
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 4, Messages.getString("QuickRExPlugin.error.message15"), e); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  private void writeBookContentsToFile(REBook p_book) {
    IPath bookFilePath = new Path(p_book.getPath());
    File bookFile = bookFilePath.toFile();
    try {
      FileOutputStream fos = new FileOutputStream(bookFile);
      fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<regularExpressionLibrary>\r\n".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      Iterator it = p_book.getContents().iterator();
      while (it.hasNext()) {
        fos.write(((RECategory)it.next()).toXMLString("\t").getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      }
      fos.write("</regularExpressionLibrary>".getBytes("UTF8")); //$NON-NLS-1$ //$NON-NLS-2$
      fos.close();
    } catch (Exception e) {
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 5, Messages.getString("QuickRExPlugin.error.message16"), e); //$NON-NLS-1$
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
   * Returns <code>true</code> if and only if currently the JDK-implementation of regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the JDK-implementation of regular expressions is used
   */
  public boolean isUsingJavaRE() {
    return getREFlavour() == MatchSetFactory.JAVA_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the ORO-Perl-implementation of regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the ORO-Perl-implementation of regular expressions is used
   */
  public boolean isUsingOROPerlRE() {
    return getREFlavour() == MatchSetFactory.ORO_PERL_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the ORO-Awk-implementation of regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the ORO-Awk-implementation of regular expressions is used
   */
  public boolean isUsingOROAwkRE() {
    return getREFlavour() == MatchSetFactory.ORO_AWK_FLAVOUR;
  }
  
  /**
   * Returns <code>true</code> if and only if currently the JRegex-implementation of regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the JRegex-implementation of regular expressions is used
   */
  public boolean isUsingJRegex() {
    return getREFlavour() == MatchSetFactory.JREGEX_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the Jakarta-Regexp-implementation of regular expressions is used.
   * 
   * @return <code>true</code> if and only if currently the Jakarta-Regexp-implementation of regular expressions is used
   */
  public boolean isUsingJakartaRegexp() {
    return getREFlavour() == MatchSetFactory.JAKARTA_REGEXP_FLAVOUR;
  }

  /**
   * Returns <code>true</code> if and only if currently the Reg. Exp. Lib.-View is linked with the RE-Entry-editor.
   * 
   * @return <code>true</code> if and only if currently the Reg. Exp. Lib.-View is linked with the RE-Entry-editor
   */
  public boolean isLinkRELibViewWithEditor() {
    return getPreferenceStore().getBoolean(LINK_RE_LIB_VIEW_WITH_EDITOR);
  }

  /**
   * Set (and store) the flag governing if the Reg. Exp. Lib.-View is linked with the RE-Entry-editor.
   * 
   * @param p_flag the state of the flag to set and store
   */
  public void setLinkRELibViewWithEditor(boolean p_flag) {
    getPreferenceStore().setValue(LINK_RE_LIB_VIEW_WITH_EDITOR, p_flag);
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
   * Tells the Plugin to use the JRegex-implementation of regular expressions.
   */
  public void useJRegex() {
    getPreferenceStore().setValue(RE_FLAVOUR, MatchSetFactory.JREGEX_FLAVOUR);
  }

  /**
   * Tells the Plugin to use the Jakarta-Regexp-implementation of regular expressions.
   */
  public void useJakartaRegexp() {
    getPreferenceStore().setValue(RE_FLAVOUR, MatchSetFactory.JAKARTA_REGEXP_FLAVOUR);
  }

  /**
   * Returns the currently used RE-implementation-flavour (actually, a flag corresponding to it as defined in MatchSetFactory).
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
   * Initializes the passed structure with the completion proposals defined in XML-files.
   * In case the files have already been parsed, the information is only copied to the
   * passed instance. If the files were not parsed yet, this is done now...
   * 
   * @param p_proposals the structure to initialize
   */
  public void initCompletionProposals(CompletionProposals p_proposals) {
    if (this.proposals == null) {
      initProposals();
    }
    proposals.copyValuesTo(p_proposals);
  }
  
  private synchronized void initProposals() {
    proposals = new CompletionProposals();

    HashMap jdkProposals = new HashMap();
    ArrayList jdkKeys = new ArrayList();
    initCompletionsFromFile(jdkProposals, jdkKeys, MatchSetFactory.JAVA_FLAVOUR);
    proposals.setKeys(MatchSetFactory.JAVA_FLAVOUR, jdkKeys);
    proposals.setProposals(MatchSetFactory.JAVA_FLAVOUR, jdkProposals);
    
    HashMap oroPerlProposals = new HashMap();
    ArrayList oroPerlKeys = new ArrayList();
    initCompletionsFromFile(oroPerlProposals, oroPerlKeys, MatchSetFactory.ORO_PERL_FLAVOUR);
    proposals.setKeys(MatchSetFactory.ORO_PERL_FLAVOUR, oroPerlKeys);
    proposals.setProposals(MatchSetFactory.ORO_PERL_FLAVOUR, oroPerlProposals);
    
    HashMap oroAwkProposals = new HashMap();
    ArrayList oroAwkKeys = new ArrayList();
    initCompletionsFromFile(oroAwkProposals, oroAwkKeys, MatchSetFactory.ORO_AWK_FLAVOUR);
    proposals.setKeys(MatchSetFactory.ORO_AWK_FLAVOUR, oroAwkKeys);
    proposals.setProposals(MatchSetFactory.ORO_AWK_FLAVOUR, oroAwkProposals);
    
    HashMap jRegexpProposals = new HashMap();
    ArrayList jRegexpKeys = new ArrayList();
    initCompletionsFromFile(jRegexpProposals, jRegexpKeys, MatchSetFactory.JREGEX_FLAVOUR);
    proposals.setKeys(MatchSetFactory.JREGEX_FLAVOUR, jRegexpKeys);
    proposals.setProposals(MatchSetFactory.JREGEX_FLAVOUR, jRegexpProposals);
    
    HashMap jakartaProposals = new HashMap();
    ArrayList jakartaKeys = new ArrayList();
    initCompletionsFromFile(jakartaProposals, jakartaKeys, MatchSetFactory.JAKARTA_REGEXP_FLAVOUR);
    proposals.setKeys(MatchSetFactory.JAKARTA_REGEXP_FLAVOUR, jakartaKeys);
    proposals.setProposals(MatchSetFactory.JAKARTA_REGEXP_FLAVOUR, jakartaProposals);
  }

  private void initCompletionsFromFile(HashMap p_proposals, ArrayList p_keys, int p_flavour) {
    String filepath = null;
    String errorMsgKey = null;
    switch (p_flavour) {
      case MatchSetFactory.JAKARTA_REGEXP_FLAVOUR:
        filepath = JAKARTA_REGEX_PROPOSAL_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.jakartaRegex.completion"; //$NON-NLS-1$
        break;
      case MatchSetFactory.JAVA_FLAVOUR:
        filepath = JDK_PROPOSAL_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.message7"; //$NON-NLS-1$
        break;
      case MatchSetFactory.JREGEX_FLAVOUR:
        filepath = JREGEX_PROPOSAL_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.jregex.completion"; //$NON-NLS-1$
        break;
      case MatchSetFactory.ORO_PERL_FLAVOUR:
        filepath = ORO_PERL_PROPOSAL_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.message9"; //$NON-NLS-1$
        break;
      case MatchSetFactory.ORO_AWK_FLAVOUR:
        filepath = ORO_AWK_PROPOSAL_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.message8"; //$NON-NLS-1$
        break;
      default:
        throw new RuntimeException("Unknown Regexp-flavour!"); //$NON-NLS-1$
    }
    try {
      InputStream propFileStream = openStream(new Path(filepath), true);
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(propFileStream, new CompletionProposalXMLHandler(p_proposals, p_keys));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString(errorMsgKey), ex); //$NON-NLS-1$
      getLog().log(status);
    }
  }

  public void initCategoriesFromFile(HashMap p_mappings, ArrayList p_categories, int p_flavour) {
    String filepath = null;
    String errorMsgKey = null;
    switch (p_flavour) {
      case MatchSetFactory.JAKARTA_REGEXP_FLAVOUR:
        filepath = JAKARTA_REGEX_CATEGORIES_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.jakartaRegex.categories"; //$NON-NLS-1$
        break;
      case MatchSetFactory.JAVA_FLAVOUR:
        filepath = JDK_CATEGORIES_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.jdk.categories"; //$NON-NLS-1$
        break;
      case MatchSetFactory.JREGEX_FLAVOUR:
        filepath = JREGEX_CATEGORIES_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.jregex.categories"; //$NON-NLS-1$
        break;
      case MatchSetFactory.ORO_PERL_FLAVOUR:
        filepath = ORO_PERL_CATEGORIES_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.oroperl.categories"; //$NON-NLS-1$
        break;
      case MatchSetFactory.ORO_AWK_FLAVOUR:
        filepath = ORO_AWK_CATEGORIES_FILE_NAME;
        errorMsgKey = "QuickRExPlugin.error.readerror.oroawk.categories"; //$NON-NLS-1$
        break;
      default:
        throw new RuntimeException("Unknown Regexp-flavour!"); //$NON-NLS-1$
    }
    try {
      InputStream propFileStream = openStream(new Path(filepath), true);
      SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      parser.parse(propFileStream, new EditorCategoryMappingXMLHandler(p_mappings, p_categories));
    } catch (Exception ex) {
      // nop, to be save
      IStatus status = new Status(IStatus.WARNING, QuickRExPlugin.ID, 3, Messages.getString(errorMsgKey), ex); //$NON-NLS-1$
      getLog().log(status);
    }
  }

 /**
   * Saves the values of all flags to the PreferenceStore, where any flag contained in the passed collection is saved as 'set', any flag known to the
   * MatchSetFactory but not contained in the passed Collection is saved as 'not set'.
   * 
   * @param p_flags
   *          a Collection holding the actually set flags
   */
  public void saveSelectedFlagValues(Collection p_flags) {
    Collection allSupported = MatchSetFactory.getAllSupportedFlags();
    for (Iterator iter = allSupported.iterator(); iter.hasNext();) {
      Flag element = (Flag)iter.next();
      getPreferenceStore().setValue(element.getCode(), p_flags.contains(element));
    }
  }

  /**
   * Returns <code>true</code> if and only if the passed Flag is saved as 'set' in the PreferenceStore.
   * 
   * @param p_flag
   *          the flag to check for
   * @return the state for the flag in the store (set: true, not set: false)
   */
  public boolean isFlagSaved(Flag p_flag) {
    return getPreferenceStore().getBoolean(p_flag.getCode());
  }

  /**
   * Returns the scope used for the last search of the Reg. Exp. Library.
   * 
   * @return the scope used
   */
  public int getLastSearchScope() {
    return getPreferenceStore().getInt(LAST_SEARCH_SCOPE);
  }

  /**
   * Set (and store) the scope used for the last search of the Reg. Exp. Library.
   * 
   * @param p_scope the scope to set and store
   */
  public void setLastSearchScope(int p_scope) {
    getPreferenceStore().setValue(LAST_SEARCH_SCOPE, p_scope);
  }

  /**
   * Adds the passed listened to the list of listeners which get informed when the RE-Library
   * changes (structurally, i.e. when books are added or removed)
   * 
   * @param p_listener the listener to add
   */
  public void addRELibraryListener(IPropertyChangeListener p_listener) {
    if (!listeners.contains(p_listener)) {
      this.listeners.add(p_listener);
    }
  }

  /**
   * Removes the passed listened from the list of listeners which get informed when the RE-Library
   * changes (structurally, i.e. when books are added or removed)
   * 
   * @param p_listener the listener to remove
   */
  public void removeRELibraryListener(IPropertyChangeListener p_listener) {
    if (listeners.contains(p_listener)) {
      this.listeners.remove(p_listener);
    }
  }

  /**
   * Returns <code>true</code> if and only if a book with the passed name already exists in the 
   * list of books in teh Reg. Exp. Library 
   * 
   * @param p_bookName The name to check for
   * @return <code>true</code> if and only if a book with the passed name exists
   */
  public boolean reBookWithNameExists(String p_bookName) {
    return getReBookWithName(p_bookName) != null;
  }

  /**
   * Returns the REBook with the passed name from the Reg. Exp. Library (if no book with 
   * the name exists, <code>null</code> is returned).
   *  
   * @param p_bookName The name of the book to return
   * @return the REBook with the passed name or <code>null</code>
   */
  public REBook getReBookWithName(String p_bookName) {
    for (Iterator iter = reBooks.iterator(); iter.hasNext();) {
      REBook book = (REBook)iter.next();
      if (book.getName().equals(p_bookName)) {
        return book;
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if and only if currently the QuickREx-View operates
   * in 'Live-evaluation-mode'.
   * 
   * @return <code>true</code> if and only if currently the QuickREx-View operates in 'Live-evaluation-mode'
   */
  public boolean isLiveEvaluation() {
    if (getPreferenceStore().contains(QuickRExPreferencesPage.P_LIVE_EVAL)) {
      return getPreferenceStore().getBoolean(QuickRExPreferencesPage.P_LIVE_EVAL);
    } else {
      return true;
    }
  }

  /**
   * Returns an ArrayList of Categories (in fact, Category-names) defined for the 
   * passed RE-Flavour.
   * 
   * @param p_flavour the Flavour, one of the constants defined in MatchSetFactory
   * @return an ArrayList of category-names (Strings) or null if the flavour is unkown
   */
  public ArrayList getRECategories(int p_flavour) {
    switch (p_flavour) {
      case MatchSetFactory.JAVA_FLAVOUR:
        return jdkCategories;
      case MatchSetFactory.ORO_PERL_FLAVOUR:
        return oroPerlCategories;
      case MatchSetFactory.ORO_AWK_FLAVOUR:
        return oroAwkCategories;
      case MatchSetFactory.JREGEX_FLAVOUR:
        return jRegexCategories;
      case MatchSetFactory.JAKARTA_REGEXP_FLAVOUR:
        return jakCategories;
      default:
        return null;
    }
  }

  /**
   * Returns an HashMap of Expressions mapped to Categories defined for the 
   * passed RE-Flavour.
   * 
   * @param p_flavour the Flavour, one of the constants defined in MatchSetFactory
   * @return a HashMap containing category-names as keys and ArrayListe of RECompletionProposal-
   *          instances as Objects.
   */
  public HashMap getREMappings(int p_flavour) {
    switch (p_flavour) {
      case MatchSetFactory.JAVA_FLAVOUR:
        return jdkCatMappings;
      case MatchSetFactory.ORO_PERL_FLAVOUR:
        return oroPerlCatMappings;
      case MatchSetFactory.ORO_AWK_FLAVOUR:
        return oroAwkCatMappings;
      case MatchSetFactory.JREGEX_FLAVOUR:
        return jRegexCatMappings;
      case MatchSetFactory.JAKARTA_REGEXP_FLAVOUR:
        return jakCatMappings;
      default:
        return null;
    }
  }
}