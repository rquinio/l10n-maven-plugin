/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.orchestrator;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class PropertiesFileValidatorTest extends AbstractL10nValidatorTest<PropertiesFile> {

  private PropertiesFileValidator validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    validator = new PropertiesFileValidator(logger, new AlwaysSucceedValidator<Property>());
  }

  @Test
  public void testValidateProperties() {
    Properties properties = new Properties();
    properties.put("ALLP.text.valid.1", "Some text");
    properties.put("ALLP.text.valid.2", "<div>Some Text on<a href=\"www.google.fr\">Google</a></div>");
    properties.put("ALLP.text.valid.3", "<a href=\"www.google.fr\" target=\"_blank\">Google</a>");
    properties.put("ALLP.text.valid.4", "&nbsp;&copy;&ndash;");
    properties.put("ALLP.text.valid.5", "<a href='http://example.com'>link</a>");
    PropertiesFile propertiesFile = new BundlePropertiesFile(BUNDLE, properties);

    int nbErrors = validator.validate(propertiesFile, items);

    assertEquals(0, nbErrors);
  }

}
