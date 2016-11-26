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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class DirectoryValidatorTest extends AbstractL10nValidatorTest<File> {

  private DirectoryValidator validator;

  private List<PropertiesFamily> propertiesFamilies;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new DirectoryValidator(logger, new AlwaysSucceedingValidator<PropertiesFamily>(),
        new AlwaysSucceedingValidator<File>());
    propertiesFamilies = new ArrayList<PropertiesFamily>();
  }

  /**
   * Validation should not fail if resource path is empty/do not exist
   */
  @Test
  public void testNoPropertiesResources() {
    validator.validate(new File("non-existing"), items);
  }

  @Test
  public void testSingleBundleePropertiesFamilyLoading() throws MojoExecutionException {
    File directory = getFile("locales");
    int nbErrors = validator.loadPropertiesFamily(directory, items, propertiesFamilies);

    assertEquals(0, nbErrors);
    assertEquals(1, propertiesFamilies.size());

    PropertiesFamily propertiesFamily = propertiesFamilies.get(0);
    assertEquals("Bundle", propertiesFamily.getBaseName());
    assertEquals(3, propertiesFamily.getNbPropertiesFiles());
    assertNotNull(propertiesFamily.getRootPropertiesFile());
  }

  @Test
  public void testMultipleBundlePropertiesFamilyLoading() throws MojoExecutionException {
    File directory = getFile("multi-bundle");
    int nbErrors = validator.loadPropertiesFamily(directory, items, propertiesFamilies);

    assertEquals(0, nbErrors);
    assertEquals(3, propertiesFamilies.size());
    assertThat(propertiesFamilies, hasItem(HasPropertyWithValue.<PropertiesFamily> hasProperty("baseName", is("1"))));
    assertThat(propertiesFamilies, hasItem(HasPropertyWithValue.<PropertiesFamily> hasProperty("baseName", is("2"))));
    assertThat(propertiesFamilies, hasItem(HasPropertyWithValue.<PropertiesFamily> hasProperty("baseName", is("3"))));
  }

  /**
   * Test the behavior of {@link Properties#load(java.io.Reader)} regarding newline character.
   */
  @Test
  public void testLoadingMultilineProperties() throws MojoExecutionException {
    File directory = getFile("multi-line");

    int nbErrors = validator.loadPropertiesFamily(directory, items, propertiesFamilies);

    assertEquals(0, nbErrors);

    Properties properties = propertiesFamilies.get(0).getPropertiesFiles().iterator().next().getProperties();

    assertEquals("Some text.", properties.getProperty("ALLP.text.key"));

    // Value formatted on multiple lines: newline character needs to be escaped
    assertEquals("Some text continuing on next line.", properties.getProperty("ALLP.text.multilineValue"));

    // \n is valid
    assertEquals("Some text continuing on next line and with newline character \n (displayed as 2 lines)",
        properties.getProperty("ALLP.text.valueWithNewline"));

    // If forgetting to escape newline character, the value is truncated ...
    assertEquals("Some text continuing", properties.getProperty("ALLP.text.badMultilineValue"));

    // ... and first world of 2nd line becomes a key (space is valid key/valuer separator)
    assertEquals("next line, but without escaping.", properties.getProperty("on"));
  }

  @Test
  public void directoriesShouldBeScannedRecursively() {
    File directory = getFile("recursive");

    validator.loadPropertiesFamily(directory, items, propertiesFamilies);

    assertEquals(1, propertiesFamilies.size());
  }
}
