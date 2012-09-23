/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.orchestrator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class DirectoryValidatorTest extends AbstractL10nValidatorTest<File> {

  private DirectoryValidator validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new DirectoryValidator(logger, new AlwaysSucceedingValidator<PropertiesFamily>());
  }

  /**
   * Validation should not fail if resource path is empty/do not exist
   */
  @Test
  public void testNoPropertiesResources() {
    validator.validate(new File("non-existing"), items);
  }

  @Test
  public void testBundlePropertiesFamilyLoading() throws MojoExecutionException {
    File directory = getFile("locales");

    PropertiesFamily propertiesFamily = validator.loadPropertiesFamily(directory);

    assertEquals("Bundle", propertiesFamily.getBaseName());
    assertEquals(3, propertiesFamily.getNbPropertiesFiles());
    assertNotNull(propertiesFamily.getRootPropertiesFile());
  }

  @Test
  public void malfomedPropertiesShouldFailExecution() throws MojoExecutionException {
    File file = getFile("malformed/malformed.properties");
    try {
      validator.loadPropertiesFile(file);
      fail("Malformed properties file should fail");
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testLogSummary() {
    items.add(new L10nReportItem(Type.HTML_VALIDATION, "", "", "", "", ""));
    items.add(new L10nReportItem(Type.INCOHERENT_TAGS, "", "", "", "", ""));
    items.add(new L10nReportItem(Type.EXCLUDED, "", "", "", "", ""));

    validator.logSummary(items);

    verify(log, atLeast(4)).info(any(CharSequence.class));
    verify(log, atLeast(1)).warn(any(CharSequence.class));
    verify(log, atLeast(1)).error(any(CharSequence.class));
  }

  private File getFile(String path) {
    return new File(this.getClass().getClassLoader().getResource(path).getFile());
  }

}
