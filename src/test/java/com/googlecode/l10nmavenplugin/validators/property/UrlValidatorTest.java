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

public class UrlValidatorTest extends AbstractL10nValidatorTest<Property> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new UrlValidator(logger, new String[] { ".url." });
  }

  @Test
  public void testUrlValidationPattern() {
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://example.com#").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://example.com?a=1&b=2").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("https://example.com#").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("//example.com").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("mailto:test@example.com").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://www:80.example.com").matches());

    assertFalse(UrlValidator.URL_VALIDATION_PATTERN.matcher("www.example.com").matches());
    assertFalse(UrlValidator.URL_VALIDATION_PATTERN.matcher("test@example.com").matches());
    assertFalse(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://.example.com").matches());
  }

  @Test
  public void testValidUrl() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "//www.google.com", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "https://www.google.com/search", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://www.google.com.au/search/misc/help_search/detect-context", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://www.google.au?param1=value1&param2=value2", FILE), items));
    // URL should allow HTML escaping
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://www.google.au?param1=value1&amp;param2=value2", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "//www.google.com/file.js#anchor", FILE), items));
  }

  @Test
  public void testMailToScheme() {
    // assertEquals(1, urlValidator.validateProperty("ALLP.url.valid", "mailto:", FILE)));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "mailto:e-mail@example.com", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "mailto:username@example.com?subject=Topic", FILE), items));
  }

  @Test
  public void urlWithoutProtocolShouldBeInvalid() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "www.google.com", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "email@example.com", FILE), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "/img/logo.png", FILE), items));
  }

  @Test
  public void urlTrailingSpaceShouldBeInvalid() {
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "//www.google.com ", FILE), items));
  }

  @Test
  public void testParametricURL() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://{0}/{1}/{2}", FILE), items));
    // Invalid param number should not be replaced qnd trigger an error
    assertEquals(1, validator.validate(new PropertyImpl(KEY_OK, "http://{199}", FILE), items));
    // Check MessageFormat parsing exceptions are catched
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "http://{0 }/{1}", FILE), items));
  }

  @Test
  public void testSchemeRelative() {
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "//host/image.png", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "//host.com:80/js/script.js?param={0}", FILE), items));
    // Hardcoded http scheme
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO, "http://host/image.png", FILE), items));

    // html/htm extensions should be ignored
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://host/link.html", FILE), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "http://host/link.htm", FILE), items));
  }

  @Test
  public void testShouldValidate() {
    assertTrue(validator.shouldValidate(new PropertyImpl("page.url.text", "Some text", FILE)));
    assertFalse(validator.shouldValidate(new PropertyImpl(KEY_OK, "Some text", FILE)));
  }
}
