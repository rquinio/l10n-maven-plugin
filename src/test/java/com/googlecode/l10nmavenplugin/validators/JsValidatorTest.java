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
