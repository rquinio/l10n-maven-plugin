/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.family;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.family.IdenticalTranslationValidator;

public class IdenticalTranslationValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new IdenticalTranslationValidator(logger);
  }

  /**
   * Translations are different => no warn
   */
  @Test
  public void testValidTranslation() {
    bundleA.put(KEY_OK, "A");
    bundleB.put(KEY_OK, "B");
    bundleC.put(KEY_OK, "C");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  /**
   * Translations are all equal => info
   */
  @Test
  public void testIdenticalTranslation() {
    bundleA.put(KEY_KO, "same");
    bundleB.put(KEY_KO, "same");
    bundleC.put(KEY_KO, "same");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(1, items.size());
  }

  /**
   * Translations are almost equal, except 1 => warn
   */
  @Test
  public void testAlmostIdenticalTranslation() {
    bundleA.put(KEY_KO, "same");
    bundleB.put(KEY_KO, "same");
    bundleC.put(KEY_KO, "same");
    bundleD.put(KEY_KO, "same");
    bundleE.put(KEY_KO, "different");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(1, items.size());
  }

  /**
   * Translations are almost equal, except 2 => warn
   */
  @Test
  public void testMultipleDifferentTranslation() {
    bundleA.put(KEY_KO, "same");
    bundleB.put(KEY_KO, "same");
    bundleC.put(KEY_KO, "same");
    bundleD.put(KEY_KO, "different1");
    bundleE.put(KEY_KO, "different2");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(1, items.size());
  }

  /**
   * Translations are almost all different, except 1 => no warn
   */
  @Test
  public void testAlmostDifferentTranslation() {
    bundleA.put(KEY_KO, "A");
    bundleB.put(KEY_KO, "B");
    bundleC.put(KEY_KO, "C");
    bundleD.put(KEY_KO, "C");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  /**
   * Validation should be skipped if resource exists only in 1 language
   */
  @Test
  public void testSingleTranslation() {
    bundleA.put(KEY_OK, "same");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }
}
