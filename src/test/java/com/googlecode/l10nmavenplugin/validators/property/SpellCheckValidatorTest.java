/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.property;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class SpellCheckValidatorTest extends AbstractL10nValidatorTest<Property> {

  private static L10nValidator<Property> validator;

  @BeforeClass
  public static void setUpClass() {
    URL url = SpellCheckValidatorTest.class.getClassLoader().getResource("");
    try {
      validator = new SpellCheckValidator(new L10nValidatorLogger(), new File(url.toURI()));
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Before
  public void setUp() {
    super.setUp();
  }

  @Test
  public void testValid() {
    validator.validate(new PropertyImpl(KEY_OK, "Some english text.", FILE), items);
    validator.validate(new PropertyImpl(KEY_OK, "A syntactically valid sentence.", FILE), items);
    // HTML tags are not supported by dictionary.
    // validator.validate(new PropertyImpl(KEY_OK, "<div>Some valid text</div>", FILE), items);

    assertEquals(0, items.size());
  }

  @Test
  public void testInvalid() {
    validator.validate(new PropertyImpl(KEY_KO, "Some engish text.", FILE), items);
    validator.validate(new PropertyImpl(KEY_KO, "A syntacticaly valid sentence.", FILE), items);
    validator.validate(new PropertyImpl(KEY_KO, "text tex te.", FILE), items);
    // assertEquals(3, items.size());
  }

  @Test
  public void validationShouldBeSkippedForMissingLocale() {
    validator.validate(new PropertyImpl(KEY_KO, "Un text en françai", new BundlePropertiesFile("junit_FR.properties",
        null)),
        items);

    assertEquals(0, items.size());
  }

  @Test
  public void rootBundleShouldDefaultToEN() {
    validator.validate(new PropertyImpl(KEY_OK, "Some english text.",
        new BundlePropertiesFile("junit.properties", null)), items);
    assertEquals(0, items.size());
  }

  @Test
  public void textMixedCaseIgnored() {
    validator.validate(new PropertyImpl(KEY_KO, "The city of Prague", FILE), items);
    // validator.validate(new PropertyImpl(KEY_KO, "Microsoft corporation", FILE), items);
    assertEquals(0, items.size());
  }

  @Test
  public void testSentenceCapitalization() {
    validator.validate(new PropertyImpl(KEY_KO, "some english text.", FILE), items);
    assertEquals(0, items.size());
  }

  @Test
  public void testCountryVariations() {
    validator.validate(
        new PropertyImpl(KEY_OK, "center color traveler", new BundlePropertiesFile("junit_en_US.properties", null)),
        items);
    validator.validate(new PropertyImpl(KEY_OK, "centre colour traveller", new BundlePropertiesFile(
        "junit_en_GB.properties",
        null)), items);

    assertEquals(0, items.size());
  }

  @Test
  public void testRootDictionary() {
    validator.validate(new PropertyImpl(KEY_KO, "abcdefghij", FILE), items);
    assertEquals(0, items.size());
  }

  /**
   * Validation should be skipped if no dictionary directory is provided
   */
  @Test
  public void noDictionaries() {
    SpellCheckValidator val = new SpellCheckValidator(new L10nValidatorLogger(), null);
    val.validate(new PropertyImpl(KEY_KO, "text", FILE), items);
    assertEquals(0, items.size());
  }

  /**
   * Validation should be skipped if no .dic files
   */
  @Test
  public void missingDictionaries() {
    SpellCheckValidator val = new SpellCheckValidator(new L10nValidatorLogger(), new File(""));
    val.validate(new PropertyImpl(KEY_KO, "engish", FILE), items);
    assertEquals(0, items.size());
  }
}
