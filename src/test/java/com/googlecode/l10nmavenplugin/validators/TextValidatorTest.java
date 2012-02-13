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
