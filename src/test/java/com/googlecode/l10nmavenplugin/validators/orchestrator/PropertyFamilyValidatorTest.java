package com.googlecode.l10nmavenplugin.validators.orchestrator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class PropertyFamilyValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  private L10nValidator<PropertyFamily> validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new PropertyFamilyValidator(logger, new String[] { ".excluded" });
  }

  @Test
  public void excludedKeysShouldBeIgnored() {
    String keyExcluded = "ALLP.text.excluded";
    bundleA.put(keyExcluded, "Some text");
    bundleB.put(keyExcluded, "");

    bundleA.put(KEY_OK, "Some text");
    bundleB.put(KEY_OK, "");

    assertFalse(validator.shouldValidate(new BundlePropertyFamily(keyExcluded, propertiesFamily)));
    assertTrue(validator.shouldValidate(new BundlePropertyFamily(KEY_OK, propertiesFamily)));
  }
}
