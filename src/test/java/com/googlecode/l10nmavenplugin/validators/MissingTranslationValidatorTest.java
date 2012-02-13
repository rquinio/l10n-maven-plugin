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

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class MissingTranslationValidatorTest {
  private L10nValidator missingValidator;
  
  private List<L10nReportItem> reportItems;
  
  @Before
  public void setUp(){
    missingValidator = new MissingTranslationValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testMissingTranslations(){
    Set<String> propertiesNames = new HashSet<String>();
    Collections.addAll(propertiesNames, "Bundle_en","Bundle_fr","Bundle_es","Bundle_de");

    missingValidator.validate("key1", "", "Bundle", reportItems);
    missingValidator.validate("key1", "blabla", "Bundle_en", reportItems);
    missingValidator.validate("key1", "blabla", "Bundle_fr", reportItems);
    missingValidator.validate("key1", "", "Bundle_es", reportItems);

    assertEquals(0, missingValidator.report(propertiesNames, reportItems));
  }
}
