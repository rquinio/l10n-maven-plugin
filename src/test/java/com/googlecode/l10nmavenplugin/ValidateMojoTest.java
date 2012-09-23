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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;

/**
 * Unit tests for {@link ValidateMojo}
 * 
 * @author romain.quinio
 */
public class ValidateMojoTest {

  /**
   * Mocked mojo to test failures/Exception handling and skip/ignore flags
   */
  private ValidateMojo failingMojo;

  /**
   * Real mojo
   */
  private ValidateMojo plugin;

  private List<L10nReportItem> items;

  private Log log;

  @Before
  public void setUp() throws IOException, PlexusContainerException, ComponentLookupException, PlexusConfigurationException {
    items = new ArrayList<L10nReportItem>();
    plugin = new ValidateMojo();

    // Use XHTML5 as it is much faster
    plugin.setXhtmlSchema(HtmlValidator.XHTML5);

    File dictionaryDir = new File(this.getClass().getClassLoader().getResource("").getFile());
    plugin.setDictionaryDir(dictionaryDir);

    log = spy(new SystemStreamLog());
    plugin.setLog(log);

    CustomPattern listPattern = new CustomPattern("List", "([A-Z](:[A-Z])+)?", ".list.");
    CustomPattern anotherPattern = new CustomPattern("List", "([A-Z](:[A-Z])+)?", new String[] { ".pattern1.", ".pattern2." });
    plugin.setCustomPatterns(new CustomPattern[] { listPattern, anotherPattern });

    // Junit bug can't use tmpFolder
    plugin.setReportsDir(new TemporaryFolder().newFolder());

    // Use default configuration for the rest
    plugin.initialize();

    failingMojo = new ValidateMojo() {
      @Override
      public int validate(File directory, List<L10nReportItem> reportItems) throws MojoExecutionException {
        return 1;
      }
    };
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

  /**
   * Test tricky escaping of \ before " when loading Properties
   * 
   * @throws MojoExecutionException
   */
  @Test
  public void testJsEscapeFromProperties() throws IOException, MojoExecutionException {
    File directory = getFile("js");
    int nbErrors = plugin.validate(directory, items);

    assertEquals(3, nbErrors);
    assertTrue(items.size() >= 3);
    verify(log, atLeast(3)).error(any(CharSequence.class));
  }

  @Test
  public void testBundlePropertiesFamilySpellcheck() throws MojoExecutionException {
    File directory = getFile("locales");

    plugin.validate(directory, items);
    // SpellCheck warnings
    assertEquals(2, items.size());
  }

  @Test
  public void testSkip() throws MojoExecutionException, MojoFailureException {
    failingMojo.setSkip(true);

    failingMojo.execute();

    assertTrue(true);
  }

  @Test
  public void testFailure() throws MojoExecutionException {
    try {
      failingMojo.executeInternal();
      fail("Exceution should have raised a failure");
    } catch (MojoFailureException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testIgnoreFailure() throws MojoExecutionException, MojoFailureException {
    failingMojo.setIgnoreFailure(true);

    failingMojo.executeInternal();

    assertTrue(true);
  }

  private File getFile(String path) {
    return new File(this.getClass().getClassLoader().getResource(path).getFile());
  }

}
