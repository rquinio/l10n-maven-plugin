/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;

/**
 * Unit tests for {@link ValidateMojo}
 * 
 * @author romain.quinio
 */
public class ValidateMojoTest {

  private static final PropertiesFile FILE = new BundlePropertiesFile("junit.properties", null);

  private static final String BUNDLE = "Junit.properties";

  private ValidateMojo plugin;

  private List<L10nReportItem> items;

  @Before
  public void setUp() {
    items = new ArrayList<L10nReportItem>();
    plugin = new ValidateMojo();
    // Use XHTML5 as it is much faster
    plugin.setXhtmlSchema(HtmlValidator.XHTML5);

    File dictionaryDir = new File(this.getClass().getClassLoader().getResource("").getFile());
    plugin.setDictionaryDir(dictionaryDir);

    plugin.setLog(new SystemStreamLog());

    CustomPattern listPattern = new CustomPattern("List", "([A-Z](:[A-Z])+)?", ".list.");
    plugin.setCustomPatterns(new CustomPattern[] { listPattern });

    // Use default configuration for the rest
    plugin.initialize();
  }

  /**
   * Mojo should not fail if resource path is empty/do not exist
   * 
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Test
  public void testNoPropertiesResources() throws MojoFailureException, MojoExecutionException {
    plugin.setPropertyDir(new File("non-existing"));
    plugin.executeInternal();
  }

  @Test
  public void testValidateProperties() {
    Properties properties = new Properties();
    properties.put("ALLP.text.valid.1", "Some text");
    properties.put("ALLP.text.valid.2", "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>");
    properties.put("ALLP.text.valid.3", "<a href=\"www.google.fr\" target=\"_blank\">Google</a>");
    properties.put("ALLP.text.valid.4", "&nbsp;&copy;&ndash;");
    properties.put("ALLP.text.valid.5", "<a href='http://example.com'>link</a>");

    PropertiesFile propertiesFile = new BundlePropertiesFile(BUNDLE, properties);

    int nbErrors = plugin.validatePropertiesFile(propertiesFile, items);
    assertEquals(0, nbErrors);
  }

  @Test
  public void excludedKeysShouldBeIgnored() {
    String[] excludedKeys = new String[] { "ALLP.text.excluded" };
    plugin.setExcludedKeys(excludedKeys);

    assertEquals(0, plugin.validateProperty(new PropertyImpl("ALLP.text.excluded", "<div>Some text", FILE), items));
    assertEquals(0, plugin.validateProperty(new PropertyImpl("ALLP.text.excluded.longer", "<div>Some text", FILE), items));
  }

  @Test
  public void emptyMessagesShouldBeIgnored() {
    assertEquals(0, plugin.validateProperty(new PropertyImpl("ALLP.text.empty", "", FILE), items));
  }

  /**
   * Test tricky escaping of \ before " when loading Properties
   */
  @Test
  public void testJsEscapeFromProperties() throws IOException {
    String fileName = "js/BundleJs.properties";
    Properties properties = new Properties();
    File file = getFile(fileName);
    properties.load(new FileInputStream(file));

    PropertiesFile propertiesFile = new BundlePropertiesFile(fileName, properties);

    int nbErrors = plugin.validatePropertiesFile(propertiesFile, items);
    assertEquals(3, nbErrors);
  }

  @Test
  public void testBundleLocale() throws MojoExecutionException {
    File directory = getFile("locales");
    PropertiesFamily propertiesFamily = plugin.loadPropertiesFamily(directory);

    assertEquals("Bundle", propertiesFamily.getBaseName());
    assertEquals(3, propertiesFamily.getNbPropertiesFiles());
    assertNotNull(propertiesFamily.getRootPropertiesFile());

    plugin.validatePropertiesFamily(propertiesFamily, items);
    // SpellCheck warnings
    assertEquals(2, items.size());
  }

  @Test
  public void testMalfomedProperties() throws MojoExecutionException {
    File file = getFile("malformed/malformed.properties");
    try {
      plugin.loadPropertiesFile(file);
      fail("Malformed properties file should fail");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  private File getFile(String path) {
    return new File(this.getClass().getClassLoader().getResource(path).getFile());
  }

}
