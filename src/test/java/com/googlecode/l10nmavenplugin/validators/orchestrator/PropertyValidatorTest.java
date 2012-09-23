package com.googlecode.l10nmavenplugin.validators.orchestrator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class PropertyValidatorTest extends AbstractL10nValidatorTest<Property> {

  private L10nValidator<Property> validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new PropertyValidator(logger, new String[] { ".excluded" });
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
}
