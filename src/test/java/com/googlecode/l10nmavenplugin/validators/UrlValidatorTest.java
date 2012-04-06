/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class UrlValidatorTest {

  private L10nValidator urlValidator;

  private List<L10nReportItem> reportItems;

  @Before
  public void setUp() {
    urlValidator = new UrlValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
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
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "//www.google.com", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "https://www.google.com/search", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "http://www.google.com.au/search/misc/help_search/detect-context",
        null, reportItems));
    assertEquals(0,
        urlValidator.validate("ALLP.url.valid", "http://www.google.au?param1=value1&param2=value2", null, reportItems));
    // URL should allow HTML escaping
    assertEquals(0,
        urlValidator.validate("ALLP.url.valid", "http://www.google.au?param1=value1&amp;param2=value2", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "//www.google.com/file.js#anchor", null, reportItems));
  }

  @Test
  public void testMailToScheme() {
    // assertEquals(1, urlValidator.validateProperty("ALLP.url.valid", "mailto:", null));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "mailto:e-mail@example.com", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "mailto:username@example.com?subject=Topic", null, reportItems));
  }

  @Test
  public void urlWithoutProtocolShouldBeInvalid() {
    assertEquals(1, urlValidator.validate("ALLP.url.invalid", "www.google.com", null, reportItems));
    assertEquals(1, urlValidator.validate("ALLP.url.invalid", "email@example.com", null, reportItems));
    assertEquals(1, urlValidator.validate("ALLP.url.invalid", "/img/logo.png", null, reportItems));
  }

  @Test
  public void urlTrailingSpaceShouldBeInvalid() {
    assertEquals(1, urlValidator.validate("ALLP.url.invalid", "//www.google.com ", null, reportItems));
  }

  @Test
  public void testParametricURL() {
    assertEquals(0, urlValidator.validate("ALLP.url.parametric.valid", "http://{0}/{1}/{2}", null, reportItems));
    // Invalid param number should not be replaced qnd trigger an error
    assertEquals(1, urlValidator.validate("ALLP.url.parametric.valid", "http://{199}", null, reportItems));
    // Check MessageFormat parsing exceptions are catched
    assertEquals(1, urlValidator.validate("ALLP.url.parametric.invalid", "http://{0 }/{1}", null, reportItems));
  }
}
