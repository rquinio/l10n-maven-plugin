package com.googlecode.l10nmavenplugin.validators;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class DefaultValidatorTest {
  
  private L10nValidator defaultValidator;
  
  private List<L10nReportItem> reportItems;
  
  @Before
  public void setUp(){
    defaultValidator = new DefaultValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testValidOther() {
    assertEquals(0, defaultValidator.validate("ALLP.other.valid", "Some \"text\"", null,reportItems));
    assertEquals(0, defaultValidator.validate("ALLP.other.valid", "a < b", null,reportItems));
  }
  
  @Test
  public void testOtherResource() {
    // Check it's only warning
    assertEquals(0, defaultValidator.validate("ALLP.other.invalid","<div>Some text</div>", null,reportItems));
    assertEquals(0, defaultValidator.validate("ALLP.other.invalid","Some <br />text", null,reportItems));
    assertEquals(0, defaultValidator.validate("ALLP.other.invalid","http://example.com", null,reportItems));
  }
}
