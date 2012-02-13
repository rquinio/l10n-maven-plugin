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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class HtmlValidatorTest {
  
  private static L10nValidator htmlValidator;
  
  private List<L10nReportItem> reportItems;
  
  /**
   * Initialize only once as the XHTML schema loading can be slow
   * 
   */
  @BeforeClass
  public static void setUpClass() throws SAXException{
    htmlValidator = new HtmlValidator(new L10nValidatorLogger());
  }
  
  @Before
  public void setUp(){
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testValidHtml() {
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "Some text", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<a href=\"www.google.fr\" target=\"_blank\">Google</a>", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "&nbsp;&copy;&ndash;", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<a href='http://example.com'>link</a>", null, reportItems));
    //Nested HTML escaping "
    //assertEquals(0, plugin.validateProperty("ALLP.text.valid", "<a onclick=\"javascript:alert(\\\"plop\\\");\"></a>", null));
  }
  
  @Test
  public void testInvalidHtml() {
    //Escaped = or : not consumed by Properties#load ?
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid", "<a href\\='http\\://example.com'>link</a>", null, reportItems));
  }
  
  @Test
  public void testUnescapedHtmlEntity() {
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid","<a href='http://example.com?param1=1&param2=2'>Text<a>", null, reportItems));
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid","A & B", null, reportItems));
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid","&nbsp;&nbsp", null, reportItems));
  }
  
  @Test
  public void testUnclosedHtmlTag() {
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid", "<br>", null, reportItems));
    assertEquals(1, htmlValidator.validate("ALLP.text.invalid", "<div>Some Text", null, reportItems));
  }

  @Test
  public void testParametricHTML(){
    assertEquals(0, htmlValidator.validate("ALLP.text.parametric.valid","Some {0} text", null, reportItems));
    //<div id='0'></div> is valid
    assertEquals(0, htmlValidator.validate("ALLP.text.parametric.valid","<div id=''id{0}''></div>", null, reportItems));
    //Workaround to pass validation: use single quotes on the inside
    assertEquals(0, htmlValidator.validate("ALLP.text.parametric.valid","<a href=\"javascript:alert(''{0}'');\"></a>", null, reportItems));
    //<div id=0></div> is not valid
    assertTrue(1 <= htmlValidator.validate("ALLP.text.parametric.invalid","<div id='id{0}'></div>", null, reportItems));
    
    //Check '' is unescaped before validation
    assertEquals(0, htmlValidator.validate("ALLP.text.parametric.valid","<div id=''id0''></div>", null, reportItems));
    
    //Quoted text
    assertEquals(0, htmlValidator.validate("ALLP.text.parametric.valid","'{Some quoted text}'", null, reportItems));
    //TODO Quoted text with parameters
    //assertEquals(0, plugin.validateProperty("ALLP.text.parametric.valid","'{Some quoted text}' with {0}", null));
  }
  
  @Test
  public void testMalformedMessageFormat(){
    assertEquals(1, htmlValidator.validate("ALLP.text.parametric.invalid","{0<span>{1}}", null, reportItems));
  }
}
