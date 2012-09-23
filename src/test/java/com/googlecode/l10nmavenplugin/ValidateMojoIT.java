package com.googlecode.l10nmavenplugin;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;

/**
 * Testing of {@link ValidateMojo} with all its dependencies configured via {@link ValidateMojo#initialize()}
 */
public class ValidateMojoIT extends AbstractL10nValidatorTest<File> {

  /**
   * Real mojo
   */
  private ValidateMojo plugin;

  @Before
  public void setUpBefore() throws IOException {
    super.setUp();

    plugin = new ValidateMojo();
    plugin.setLog(log);

    // Use XHTML5 as it is much faster
    plugin.setXhtmlSchema(HtmlValidator.XHTML5);

    File dictionaryDir = new File(this.getClass().getClassLoader().getResource("").getFile());
    plugin.setDictionaryDir(dictionaryDir);

    CustomPattern listPattern = new CustomPattern("List", "([A-Z](:[A-Z])+)?", ".list.");
    CustomPattern anotherPattern = new CustomPattern("List", "([A-Z](:[A-Z])+)?", new String[] { ".pattern1.", ".pattern2." });
    plugin.setCustomPatterns(new CustomPattern[] { listPattern, anotherPattern });

    // Junit bug can't use tmpFolder
    plugin.setReportsDir(new TemporaryFolder().newFolder());

    // Use default configuration for the rest
    plugin.initialize();
  }

  @Test
  public void testBundlePropertiesFamilySpellcheck() throws MojoExecutionException {
    File directory = getFile("locales");

    plugin.validate(directory, items);
    // SpellCheck warnings
    assertEquals(2, items.size());
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

  private File getFile(String path) {
    return new File(this.getClass().getClassLoader().getResource(path).getFile());
  }
}
