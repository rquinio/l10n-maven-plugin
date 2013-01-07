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
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class HtmlValidatorTest extends AbstractL10nValidatorTest<Property> {

  private static HtmlValidator validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    // Use XHTML5 schema as it is much faster
    validator = new HtmlValidator(HtmlValidator.XHTML5, logger, null, new String[] { ".text." });
    validator.setSpellCheckValidator(new AlwaysSucceedingValidator<Property>());
  }

  @Test
  public void testDataAttributePattern() {
    assertFalse(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("length=\"\"").find());
    assertFalse(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-=\"\"").find());
    assertFalse(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-param=\"'").find());

    assertTrue(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-length=\"\"").find());
    // Single quotes
    assertTrue(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-length=''").find());

    // Spaces
    assertTrue(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher(" data-length=\" \" ").find());
    assertTrue(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-123-456=\"789\"").find());

    // Double quotes inside value
    // assertFalse(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-length=\"\"\"").find());
    assertTrue(HtmlValidator.DATA_ATTRIBUTE_PATTERN.matcher("data-length=\"\\\"\"").find());
  }

  @Test
  public void testValidHtml() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some text", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK,
        "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK,
        "<a href=\"www.google.fr\" target=\"_blank\">Google</a>", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "&nbsp;&copy;&ndash;", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<a href='http://example.com'>link</a>", FILE), items));
    // Nested HTML escaping "
    // assertEquals(0, plugin.validateProperty(KEY_OK, "<a onclick=\"javascript:alert(\\\"plop\\\");\"></a>", null));
  }

  @Test
  public void testInvalidHtml() {
    // Escaped = or : not consumed by Properties#load ?
    assertEquals(1,
        validator.validate(new PropertyImpl(KEY_KO, "<a href\\='http\\://example.com'>link</a>", FILE), items));
  }

  @Test
  public void testUnescapedHtmlEntity() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO,
        "<a href='http://example.com?param1=1&param2=2'>Text<a>", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "A & B", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "&nbsp;&nbsp", FILE), items));
  }

  @Test
  public void testUnclosedHtmlTag() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "<br>", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "<div>Some Text", FILE), items));
  }

  @Test
  public void testParametricHTML() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some {0} text", FILE), items));
    // <div id='0'></div> is valid
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<div id=''id{0}''></div>", FILE), items));
    // Workaround to pass validation: use single quotes on the inside
    assertEquals(0,
        validator.validate(new PropertyImpl(KEY_OK, "<a href=\"javascript:alert(''{0}'');\"></a>", FILE), items));
    // <div id=0></div> is not valid
    assertTrue(1 <= validator.validate(new PropertyImpl(KEY_KO, "<div id='id{0}'></div>", FILE), items));

    // Check '' is unescaped before validation
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<div id=''id0''></div>", FILE), items));

    // Quoted text
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "'{Some quoted text}'", FILE), items));
    // TODO Quoted text with parameters
    // assertEquals(0, plugin.validateProperty(KEY_OK,"'{Some quoted text}' with {0}", FILE)));
  }

  @Test
  public void testMalformedMessageFormat() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "{0<span>{1}}", FILE), items));
  }

  @Test
  public void customHtmlDataAttributesShouldPassValidation() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<div data-length=\"\" ></div>", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "<br data-j2e-value=\"1\" />", FILE), items));

    // Mixed double/single quotes
    assertEquals(1, validator.validate(new PropertyImpl(KEY_OK, "<div data-length=\"' ></div>", FILE), items));
  }

  @Test
  public void testSpellCheckChaining() {
    validator.setSpellCheckValidator(new AlwaysFailingValidator<Property>());

    assertEquals(2,
        validator.validate(new PropertyImpl(KEY_KO, "<div>Text1<a href=\"http://\">Text2</a></div>", FILE), items));
  }

  @Test
  public void testSpellCheck() {
    URL url = getClass().getClassLoader().getResource("");
    try {
      L10nValidator<Property> spellCheckValidator = new SpellCheckValidator(new L10nValidatorLogger(), new File(
          url.toURI()));
      validator.setSpellCheckValidator(spellCheckValidator);
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    validator.validate(new PropertyImpl(KEY_KO, "<div>Blablaplop<a href=\"http://\">Englis</a></div>", FILE), items);
    assertEquals(2, items.size());
  }

  @Test
  public void testShouldValidate() {
    assertTrue(validator.shouldValidate(new PropertyImpl("page.text.key", "Some text", FILE)));
    assertFalse(validator.shouldValidate(new PropertyImpl(KEY_OK, "Some text", FILE)));
  }

  @Test
  public void testSeveralLis() {
    validator.validate(new PropertyImpl(KEY_OK, "<li>Item 1</li> <li>Item 2</li> <li>Item 3</li>", FILE), items);
    assertEquals(items.toString(), 0, items.size());
    items.clear();

    validator.validate(new PropertyImpl(KEY_KO, "<div><li>Item 1</li> <li>Item 2</li> <li>Item 3</li></div>",
        FILE), items);
    assertEquals(items.toString(), 1, items.size());
  }
}
