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

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.CustomPattern;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class PatternValidatorTest extends AbstractL10nValidatorTest<Property> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    CustomPattern listPattern = new CustomPattern("List validator", "([A-Z](:[A-Z])+)?", ".list");
    validator = new PatternValidator(logger, listPattern);
  }

  @Test
  public void test() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "A:B:C:D", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, " A:B:C:D ", FILE), items));
  }

  @Test
  public void testShouldValidate() {
    assertTrue(validator.shouldValidate(new PropertyImpl("page.list.key", "Some text", FILE)));
    assertFalse(validator.shouldValidate(new PropertyImpl(KEY_OK, "Some text", FILE)));
  }
}
