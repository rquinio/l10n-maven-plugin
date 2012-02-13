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
  public void setUp(){
    urlValidator = new UrlValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }

  @Test
  public void testUrlValidationPattern(){
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://example.com#").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("http://example.com?a=1&b=2").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("https://example.com#").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("//example.com").matches());
    assertTrue(UrlValidator.URL_VALIDATION_PATTERN.matcher("mailto:test@example.com").matches());
    
    assertFalse(UrlValidator.URL_VALIDATION_PATTERN.matcher("www.example.com").matches());
    assertFalse(UrlValidator.URL_VALIDATION_PATTERN.matcher("test@example.com").matches());
  }

  @Test
  public void testValidUrl() {
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "//www.google.com", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "https://www.google.com/search", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "http://www.google.com.au/search/misc/help_search/detect-context", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "http://www.google.au?param1=value1&param2=value2", null, reportItems));
    // URL should allow HTML escaping
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "http://www.google.au?param1=value1&amp;param2=value2", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "//www.google.com/file.js#anchor", null, reportItems));
  }
  
  @Test
  public void testMailToScheme() {
    //assertEquals(1, urlValidator.validateProperty("ALLP.url.valid", "mailto:", null));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "mailto:e-mail@example.com", null, reportItems));
    assertEquals(0, urlValidator.validate("ALLP.url.valid", "mailto:username@example.com?subject=Topic", null, reportItems));
  }
  
  @Test
  public void urlWithoutProtocolShouldBeInvalid() {
    assertEquals(1, urlValidator.validate("ALLP.url.invalid","www.google.com", null, reportItems));
    assertEquals(1, urlValidator.validate("ALLP.url.invalid","email@example.com", null, reportItems));
    assertEquals(1, urlValidator.validate("ALLP.url.invalid","/img/logo.png", null, reportItems));
  }
  
  @Test
  public void urlTrailingSpaceShouldBeInvalid() {
    assertEquals(1, urlValidator.validate("ALLP.url.invalid","//www.google.com ", null, reportItems));
  }
  
  @Test
  public void testParametricURL(){
    assertEquals(0, urlValidator.validate("ALLP.url.parametric.valid","http://{0}/{1}/{2}", null, reportItems));
    //Check MessageFormat parsing exceptions are catched
    assertEquals(1, urlValidator.validate("ALLP.url.parametric.invalid","http://{0 }/{1}", null, reportItems));
  }
}
