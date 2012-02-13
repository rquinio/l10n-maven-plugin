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
  public void testValidText() {
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "Some text", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<a href=\"www.google.fr\" target=\"_blank\">Google</a>", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "&nbsp;&copy;&ndash;", null, reportItems));
    assertEquals(0, htmlValidator.validate("ALLP.text.valid", "<a href='http://example.com'>link</a>", null, reportItems));
    //Nested HTML escaping "
    //assertEquals(0, plugin.validateProperty("ALLP.text.valid", "<a onclick=\"javascript:alert(\\\"plop\\\");\"></a>", null));
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
