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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.googlecode.l10nmavenplugin.ValidateMojo;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem;

/**
 * Unit tests for {@link ValidateMojo}
 * @author romain.quinio
 */
public class ValidateMojoTest {

  private static ValidateMojo plugin;
  
  private List<L10nReportItem> reportItems;

  /**
   * Initialize only once as the XHTML schema loading can be slow
   * 
   * @throws URISyntaxException
   * @throws SAXException
   */
  @BeforeClass
  public static void setUpClass() throws URISyntaxException, SAXException {
    plugin = new ValidateMojo();
    plugin.setLog(new SystemStreamLog());
  }
  
  @Before
  public void setUp(){
    reportItems = new ArrayList<L10nReportItem>();
  }

  @Test
  public void testFromPropertiesResources() throws MojoExecutionException, MojoFailureException {
    // Look for properties from src/test/resources added to test classpath
    String propertiesDir = this.getClass().getClassLoader().getResource("").getFile();
    plugin.setPropertyDir(new File(propertiesDir));

    try {
      plugin.execute();
    } catch (MojoFailureException e) {
      return;
    }
    fail();
  }
  
  /**
   * Mojo should not fail if resource path is empty/do not exist
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Test
  public void testNoPropertiesResources() throws MojoFailureException, MojoExecutionException {
    plugin.setPropertyDir(new File("non-existing"));
    plugin.execute();
  }

  @Test
  public void testValidateProperties() {
    Properties properties = new Properties();
    properties.put("ALLP.text.valid.1", "Some text");
    properties.put("ALLP.text.valid.2", "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>");
    properties.put("ALLP.text.valid.3", "<a href=\"www.google.fr\" target=\"_blank\">Google</a>");
    properties.put("ALLP.text.valid.4", "&nbsp;&copy;&ndash;");
    properties.put("ALLP.text.valid.5", "<a href='http://example.com'>link</a>");

    int nbErrors = plugin.validateProperties(properties, "Junit.properties", reportItems);
    assertEquals(0, nbErrors);
  }

  @Test
  public void excludedKeysShouldBeIgnored() {
    String[] excludedKeys = new String[] { "ALLP.text.excluded" };
    plugin.setExcludedKeys(excludedKeys);

    assertEquals(0, plugin.validateProperty("ALLP.text.excluded","<div>Some text", null, reportItems));
    assertEquals(0, plugin.validateProperty("ALLP.text.excluded.longer","<div>Some text", null, reportItems));
  }

  @Test
  public void emptyMessagesShouldBeIgnored() {
    assertEquals(0, plugin.validateProperty("ALLP.text.empty","", null, reportItems));
  }

  /**
   * Test tricky escaping of \ before " when loading Properties
   */
  @Test
  public void testJsEscapeFromProperties() throws IOException {
    String fileName = "BundleJsEscape.properties";
    Properties properties = new Properties();
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    properties.load(new FileInputStream(file));
    
    int nbErrors = plugin.validateProperties(properties, fileName, reportItems);
    assertEquals(3, nbErrors);
  }
}
