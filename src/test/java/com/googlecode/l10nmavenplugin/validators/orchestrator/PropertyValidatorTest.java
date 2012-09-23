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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class PropertyValidatorTest extends AbstractL10nValidatorTest<Property> {

  private PropertyValidator validator;
  private L10nValidator<Property> defaultValidator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new PropertyValidator(logger, new String[] { ".excluded" });

    validator.setParametricMessageValidator(new AlwaysSucceedingValidator<Property>());
    validator.setTrailingWhitespaceValidator(new AlwaysSucceedingValidator<Property>());

    validator.setUrlValidator(new AlwaysRefusingValidator<Property>());
    validator.setHtmlValidator(new AlwaysRefusingValidator<Property>());
    validator.setJsValidator(new AlwaysRefusingValidator<Property>());
    validator.setPatternValidators(new L10nValidator[] { new AlwaysRefusingValidator<Property>() });
    validator.setPlainTextValidator(new AlwaysRefusingValidator<Property>());

    defaultValidator = spy(new AlwaysSucceedingValidator<Property>());
    validator.setDefaultValidator(defaultValidator);

  }

  @Test
  public void excludedKeysShouldBeIgnored() {
    assertFalse(validator.shouldValidate(new PropertyImpl("ALLP.text.excluded", "<div>Some text", FILE)));
    assertFalse(validator.shouldValidate(new PropertyImpl("ALLP.text.excluded.longer", "<div>Some text", FILE)));
  }

  @Test
  public void emptyMessagesShouldBeIgnored() {
    assertEquals(0, validator.validate(new PropertyImpl("ALLP.text.empty", "", FILE), items));
  }

  @Test
  public void shouldCallDefaultValidator() {
    validator.validate(new PropertyImpl("ALLP.default", "Some text", FILE), items);

    verify(defaultValidator).validate(any(Property.class), anyListOf(L10nReportItem.class));

  }
}
