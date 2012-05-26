/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.bundle;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class MissingTranslationValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new MissingTranslationValidator(logger);
  }

  /**
   * Root bundle should be ignored
   */
  @Test
  public void testNoMissingTranslations() {
    // Root bundle is ignored
    bundleA.put(KEY_OK, "A");
    bundleB.put(KEY_OK, "B");
    bundleC.put(KEY_OK, "C");
    bundleD.put(KEY_OK, "D");
    bundleE.put(KEY_OK, "E");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  /**
   * If translation is missing in all languages except 1, no warning.
   */
  @Test
  public void onlyOneTranslationShouldNotTriggerWarn() {
    bundleA.put(KEY_OK, "A");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  /**
   * If translation missing in more than 1 language => warn
   */
  @Test
  public void testMissingTranslations() {
    root.put(KEY_KO, "");
    bundleA.put(KEY_KO, "blabla");
    bundleB.put(KEY_KO, "blabla");
    bundleC.put(KEY_KO, "");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);

    // bundleC, bundleD, bundleE warnings should be grouped into 1
    assertEquals(1, items.size());
  }
}
