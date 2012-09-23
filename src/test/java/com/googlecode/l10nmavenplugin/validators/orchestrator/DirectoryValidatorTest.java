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
    validator = new DirectoryValidator(logger, new AlwaysSucceedValidator<PropertiesFamily>());
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
