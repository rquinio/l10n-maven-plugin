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
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class PropertiesFamilyValidatorTest extends AbstractL10nValidatorTest<PropertiesFamily> {

  @Rule
  public TemporaryFolder tmpFolder;

  private PropertiesFamilyValidator validator;

  /**
   * Used different name because of eclipse auto-format bug preventing from throwing IOException if @Override
   * 
   * @throws IOException
   */
  @Before
  public void setUpBefore() throws IOException {
    super.setUp();

    // Junit bug can't use tmpFolder in @Before
    // Test with 2 nested directory
    File reportsDir = new File(new TemporaryFolder().newFolder(), "l10n-reports");

    validator = new PropertiesFamilyValidator(logger, reportsDir, new AlwaysSucceedingValidator<PropertyFamily>());
  }

  @Test
  public void testIgnoredPropertiesShouldBeRepoprted() {
    validator = new PropertiesFamilyValidator(logger, null, new AlwaysRefusingValidator<PropertyFamily>());
    bundleA.put(KEY_OK, "");
    bundleB.put(KEY_KO, "");

    assertEquals(0, validator.validate(propertiesFamily, items));
    assertEquals(2, items.size());
  }

  @Test
  public void testGenerateCsv() {
    items.add(new L10nReportItem(Type.HTML_VALIDATION, "blabla", "base_EN", "KO.key", "blabla", ""));
    File csvFile = validator.generateCsv(items, "base", Severity.ERROR);

    assertTrue(csvFile.length() > 0);
  }

  @Test
  public void csvShouldNotBeGeneratedIfNoKey() {
    File csvFile = validator.generateCsv(items, "base", Severity.ERROR);
    assertEquals(0, csvFile.length());
  }

  @Test
  public void csvShouldNotBeGeneratedIfNoReportFolder() {
    validator = new PropertiesFamilyValidator(logger, null, new AlwaysSucceedingValidator<PropertyFamily>());

    File csvFile = validator.generateCsv(items, "base", Severity.ERROR);
    assertNull(csvFile);
  }

  @Test
  public void testLogSummary() {
    items.add(new L10nReportItem(Type.HTML_VALIDATION, "", "", "", "", ""));
    items.add(new L10nReportItem(Type.INCOHERENT_TAGS, "", "", "", "", ""));
    items.add(new L10nReportItem(Type.EXCLUDED, "", "", "", "", ""));

    validator.logBundleValidationSummary(items, "bundle");

    verify(log, atLeast(4)).info(any(CharSequence.class));
    verify(log, atLeast(1)).warn(any(CharSequence.class));
    verify(log, atLeast(1)).error(any(CharSequence.class));
  }

}
