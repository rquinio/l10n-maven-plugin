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

import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class JsValidatorTest extends AbstractL10nValidatorTest<Property> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    // Double quoted by default
    validator = new JsValidator(new HtmlValidator(HtmlValidator.XHTML5, logger, null, new String[] { ".text." }), logger, new String[] { ".js." });
  }

  @Test
  public void testJsQuoteValidationPattern() {
    assertTrue(JsValidator.JS_DOUBLE_QUOTED_VALIDATION_PATTERN.matcher("Some text").matches());
    assertTrue(JsValidator.JS_DOUBLE_QUOTED_VALIDATION_PATTERN.matcher("Some 'text'").matches());
    // Unescaped quotes
    assertFalse(JsValidator.JS_DOUBLE_QUOTED_VALIDATION_PATTERN.matcher("Some \"text\"").matches());
    assertFalse(JsValidator.JS_SINGLE_QUOTED_VALIDATION_PATTERN.matcher("Some 'text'").matches());

    // Quotes escaped
    assertTrue(JsValidator.JS_DOUBLE_QUOTED_VALIDATION_PATTERN.matcher("<div id=\\\"id\\\" />").matches());
    assertTrue(JsValidator.JS_SINGLE_QUOTED_VALIDATION_PATTERN.matcher("<div id=\\'id\\' />").matches());
  }

  @Test
  public void testJsNewlineValidationPattern() {
    // Unescaped newline
    assertFalse(JsValidator.JS_NEWLINE_VALIDATION_PATTERN.matcher("Some text\n").matches());
    // Newline escaped
    assertTrue(JsValidator.JS_NEWLINE_VALIDATION_PATTERN.matcher("Some text\\n").matches());
  }

  @Test
  public void testValidJsDoubleQuoted() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some 'text' ", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<a href='www.google.fr' target='_blank'>Google</a>", FILE), items));
  }

  @Test
  public void testValidJsSingleQuoted() {
    validator = new JsValidator(false, new HtmlValidator(HtmlValidator.XHTML5, logger, null, new String[] { ".text." }), logger, new String[] { ".js." });
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some \"text\" ", FILE), items));
  }

  @Test
  public void testJsSpecialCharacters() {
    // Unescaped "
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "Some \"badly escaped text\"", FILE), items));
    assertEquals(1, items.size());

    // Note: this only works because Properties#load is by passed
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some \\\"js escaped text\\\" ", FILE), items));

    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "Some text\r\n", FILE), items));
  }

  @Test
  public void testInvalidHtmlInsideJs() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "Some error <a href=''>Text<a>", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "Some Text <br>", FILE), items));
  }

  @Test
  public void testShouldValidate() {
    assertTrue(validator.shouldValidate(new PropertyImpl("page.js.key", "Some text", FILE)));
    assertFalse(validator.shouldValidate(new PropertyImpl(KEY_OK, "Some text", FILE)));
  }
}
