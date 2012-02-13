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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class ParametricMessageValidatorTest {
  
  private L10nValidator parametricMessageValidator;
  
  private List<L10nReportItem> reportItems;
  
  @Before
  public void setUp(){
    parametricMessageValidator = new ParametricMessageValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testParametricReplacePattern(){
    assertFalse(ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some text").matches());
    assertFalse(ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some quoted text: '{bla}' ").matches());
    
    assertTrue(ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some text: {0} {1}").matches());
    assertTrue(ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some date: {0,date}").matches());
    assertTrue(ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some date: {0,number,integer}").matches());
  }
  
  
  @Test
  public void testUnescapedQuotesPattern(){
    assertFalse(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some text").matches());
    assertFalse(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some '' text").matches());
    assertFalse(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some ''{0}'' text").matches());
    
    assertTrue(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some ' text").matches());
    assertTrue(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some text with a '").matches());
   
    //TODO assertFalse(ParametricMessageValidator.UNESCAPED_QUOTE_PATTERN.matcher("Some 'quoted' text").matches());
  }
  
  @Test
  public void testParametricReplacepatternCapture(){
    Matcher m = ParametricMessageValidator.DETECT_PARAMETERS_PATTERN.matcher("Some {0} parametrized text {1,date}");
    assertTrue(m.matches());
    //Only last is saved
    assertEquals(1, m.groupCount());
    assertEquals("1",m.group(1));
  }
  
  /**
   * Order of property should not matter
   */
  @Test
  public void testCoherentParameters(){
    Set<String> propertiesNames = new HashSet<String>();
    Collections.addAll(propertiesNames, "BundleA","BundleB","BundleC");

    String key = "key.ok";
    parametricMessageValidator.validate(key, "{0}{1}{2}", "BundleA", reportItems);
    parametricMessageValidator.validate(key, "{1} {0} {2}", "BundleB", reportItems);
    parametricMessageValidator.validate(key, "{1}-{2}-{0}", "BundleC", reportItems);
    assertEquals(0, parametricMessageValidator.report(propertiesNames, reportItems));
  }
  
  @Test
  public void testIncoherentParameters(){
    Set<String> propertiesNames = new HashSet<String>();
    Collections.addAll(propertiesNames, "BundleA","BundleB","BundleC");

    String key = "key.incoherent";
    parametricMessageValidator.validate(key, "{0}{1}{2}", "BundleA", reportItems);
    parametricMessageValidator.validate(key, "{0}{1}", "BundleB", reportItems);
    parametricMessageValidator.validate(key, "{0}", "BundleC", reportItems);
    
    //Only warning
    assertEquals(/*2*/0, parametricMessageValidator.report(propertiesNames, reportItems));
  }
  
  @Test
  public void testGroupingIncoherentParameters(){
    Set<String> propertiesNames = new HashSet<String>();
    Collections.addAll(propertiesNames, "BundleA","BundleB","BundleC","BundleD","BundleE");

    String key = "key.incoherent";
    parametricMessageValidator.validate(key, "{0}{1}{2}", "BundleA", reportItems);
    parametricMessageValidator.validate(key, "{0}{1}{2}", "BundleB", reportItems);
    parametricMessageValidator.validate(key, "{0}{1}{2}", "BundleC", reportItems);
    parametricMessageValidator.validate(key, "{0}{1}", "BundleD", reportItems);
    parametricMessageValidator.validate(key, "{0}{1}", "BundleE", reportItems);
    
    //Only 1 warning thanks to grouping
    assertEquals(/*1*/0, parametricMessageValidator.report(propertiesNames, reportItems));
  }
  
  /**
   * Check regexp on longer text for StackOverFlowError
   */
  @Test
  public void testLongMessage() {
    String longMessage = "Lorem ipsum dolor sit amet {0}, consectetur adipiscing elit. Quisque dapibus iaculis erat, ut sollicitudin odio pulvinar non. Maecenas scelerisque mi at ipsum mattis et aliquet nisi iaculis. Mauris faucibus ligula sit amet erat rhoncus consectetur. " +
    		"Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. {1}" +
    		"Nam sollicitudin feugiat purus, id tempus nulla pretium eu. " +
    		"Morbi velit mi, porta sed tempus commodo, lobortis vel purus. {1} Fusce sed molestie purus. " +
    		"Fusce leo elit, euismod accumsan elementum vitae, porta vitae nunc. " +
    		"Mauris venenatis, nisi eget suscipit accumsan, est est sollicitudin risus, vitae faucibus massa massa vitae ipsum. " +
    		"Fusce volutpat mattis porttitor. {2} Sed lobortis, mi at vehicula tristique, tortor ante iaculis felis, vitae mollis libero ipsum ac lectus. " +
    		"Nullam eros libero, tempor in venenatis sit amet, molestie elementum risus. {3} Nullam fermentum justo vel turpis tristique {4} congue. " +
    		"Nulla '{5}' velit lorem, ultricies nec tempus laoreet, {6} congue at lorem. ";

    assertEquals(0, parametricMessageValidator.validate("key", longMessage, null, reportItems));
  }
  
  @Test
  public void testUnnecessaryQuoteEscaping(){
    //Only a warning
    assertEquals(0, parametricMessageValidator.validate("ALLP.text.parametric", "Some '' text", null, reportItems));
  }
}
