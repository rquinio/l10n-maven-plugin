package com.googlecode.l10nmavenplugin.validators.orchestrator;

import static org.junit.Assert.*;

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

    validator = new PropertiesFamilyValidator(logger, reportsDir, new AlwaysSucceedValidator<PropertyFamily>());
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
    validator = new PropertiesFamilyValidator(logger, null, new AlwaysSucceedValidator<PropertyFamily>());

    File csvFile = validator.generateCsv(items, "base", Severity.ERROR);
    assertNull(csvFile);
  }

}
