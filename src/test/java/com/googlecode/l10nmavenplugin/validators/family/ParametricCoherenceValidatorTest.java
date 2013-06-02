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

import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class ParametricCoherenceValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new ParametricCoherenceValidator(logger);
  }

  @Test
  public void testIsParametric() {
    assertFalse(ParametricCoherenceValidator.isParametric("Some text"));
    assertFalse(ParametricCoherenceValidator.isParametric("Some quoted text: '{bla}' "));

    assertTrue(ParametricCoherenceValidator.isParametric("Some text: {0} {1}"));
    assertTrue(ParametricCoherenceValidator.isParametric("Some date: {0,date}"));
    assertTrue(ParametricCoherenceValidator.isParametric("Some date: {0,number,integer}"));
  }

  @Test
  public void testIsParametricWithNewline() {
    assertTrue(ParametricCoherenceValidator.isParametric("Some text: {0} \n with newline"));
  }

  @Test
  @Ignore("Case not supported yet")
  public void singleQuoteShouldBeAnEscapeSequence() {
    assertFalse(ParametricCoherenceValidator.isParametric("'{0}'"));
  }

  @Test
  public void testParametricReplacePatternCapture() {
    Matcher m = ParametricCoherenceValidator.DETECT_PARAMETERS_PATTERN.matcher("Some {0} parametrized text {1,date}");
    assertTrue(m.matches());
    // Only last is saved
    assertEquals(1, m.groupCount());
    assertEquals("1", m.group(1));
  }

  /**
   * Verify order of property should not matter
   */
  @Test
  public void testCoherentParameters() {
    bundleA.put(KEY_OK, "{0}{1}{2}");
    bundleB.put(KEY_OK, "{1} {0} {2}");
    bundleC.put(KEY_OK, "{1}-{2}-{0}");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  /**
   * Incoherence of some parameters with index not used
   */
  @Test
  public void testIncoherentParameters() {
    bundleA.put(KEY_KO, "{0}{1}{2}");
    bundleB.put(KEY_KO, "{0}{1}");
    bundleC.put(KEY_KO, "{0}");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    // Only warning
    assertEquals(0, validator.validate(propertyFamily, items));
    assertEquals(2, items.size());
  }

  /**
   * Verify reportItems are grouped
   */
  @Test
  public void testGroupingIncoherentParameters() {
    bundleA.put(KEY_KO, "{0}{1}{2}");
    bundleB.put(KEY_KO, "{0}{1}{2}");
    bundleC.put(KEY_KO, "{0}{1}{2}");
    bundleD.put(KEY_KO, "{0}{1}");
    bundleE.put(KEY_KO, "{0}{1}");

    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    // Only 1 warning thanks to grouping
    assertEquals(0, validator.validate(propertyFamily, items));
    assertEquals(1, items.size());
  }

  @Test
  public void testRecursiveSubFormats() {
    String key = "ALLP.subformat.valid";
    bundleA.put(key, "{0,choice,0#none|0<many{0}}");
    bundleB.put(key, "{1,choice,0#none|0<many{1}}");

    PropertyFamily propertyFamily = new BundlePropertyFamily(key, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(1, items.size());
  }
}
