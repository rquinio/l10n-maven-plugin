package com.googlecode.l10nmavenplugin.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class JsValidatorTest {
  
  private static L10nValidator jsValidator;
  
  private List<L10nReportItem> reportItems;
  
  @BeforeClass
  public static void setUpClass() throws SAXException{
    L10nValidatorLogger logger = new L10nValidatorLogger();
    jsValidator = new JsValidator(new HtmlValidator(logger), logger);
  }
  
  @Before
  public void setUp(){
    reportItems = new ArrayList<L10nReportItem>();
  }

  @Test
  public void testJsValidationPattern(){
    assertTrue(JsValidator.JS_VALIDATION_PATTERN.matcher("Some text").matches());
    assertTrue(JsValidator.JS_VALIDATION_PATTERN.matcher("Some 'text'").matches());
    //Unescaped double quotes
    assertFalse(JsValidator.JS_VALIDATION_PATTERN.matcher("Some \"text\"").matches());
    //Unescaped newline
    assertFalse(JsValidator.JS_VALIDATION_PATTERN.matcher("Some text\n").matches());
    //Quotes/newline escaped
    assertTrue(JsValidator.JS_VALIDATION_PATTERN.matcher("<div id=\\\"id\\\" />").matches());
    assertTrue(JsValidator.JS_VALIDATION_PATTERN.matcher("Some text\\n").matches());
  }
  
  @Test
  public void testValidJs() {
    assertEquals(0, jsValidator.validate("ALLP.js.valid", "Some 'text' ", null, reportItems));
    assertEquals(0, jsValidator.validate("ALLP.js.valid", "<a href='www.google.fr' target='_blank'>Google</a>", null, reportItems));
  }
  
  @Test
  public void testJsSpecialCharacters() {
    //Unescaped "
    assertEquals(1, jsValidator.validate("ALLP.js.invalid","Some \"badly escaped text\"", null, reportItems));

    //Note: this only works because Properties#load is by passed
    assertEquals(0, jsValidator.validate("ALLP.js.valid", "Some \\\"js escaped text\\\" ", null, reportItems));
    
    assertEquals(1, jsValidator.validate("ALLP.js.invalid","Some text\r\n", null, reportItems));
  }
  
  @Test
  public void testInvalidHtmlInsideJs() {
    assertEquals(1, jsValidator.validate("ALLP.js.invalid","Some error <a href=''>Text<a>", null, reportItems));
    assertEquals(1, jsValidator.validate("ALLP.js.invalid","Some Text <br>", null, reportItems));
  }
}
