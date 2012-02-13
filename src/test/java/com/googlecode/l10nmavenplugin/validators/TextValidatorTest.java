package com.googlecode.l10nmavenplugin.validators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class TextValidatorTest {
  
  private L10nValidator textValidator;
  
  private List<L10nReportItem> reportItems;
  
  @Before
  public void setUp(){
    textValidator = new TextValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testUrlPattern(){
    assertFalse(TextValidator.URL_PATTERN.matcher("Some text").matches());
    assertTrue(TextValidator.URL_PATTERN.matcher("http://example.com").matches());
  }
  
  @Test
  public void testHtmlPattern(){
    assertFalse(TextValidator.HTML_PATTERN.matcher("Some text").matches());
    //assertFalse(ValidateMojo.HTML_PATTERN.matcher("a < b and b > c").matches());
    
    assertTrue(TextValidator.HTML_PATTERN.matcher("<a href=''></a>").matches());
  }
  
  @Test
  public void testInvalidTextResource() {
    assertEquals(1, textValidator.validate("ALLP.title.invalid","<div>Some text</div>", null, reportItems));
    assertEquals(1, textValidator.validate("ALLP.title.invalid","Some <br />text", null, reportItems));
  }
}
