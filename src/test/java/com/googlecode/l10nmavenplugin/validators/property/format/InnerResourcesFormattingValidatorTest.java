package com.googlecode.l10nmavenplugin.validators.property.format;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class InnerResourcesFormattingValidatorTest extends AbstractL10nValidatorTest<Property> {

  private static final String REGEX_DOLLAR_SIGN = "\\$\\{([A-Za-z0-9\\._]+)\\}";

  private Properties properties = new Properties();

  private PropertiesFile file = new BundlePropertiesFile("SomeFile.properties", properties);

  @Override
  @Before
  public void setUp() {
    super.setUp();

    validator = new InnerResourcesFormattingValidator(logger, REGEX_DOLLAR_SIGN);
  }

  @Test
  public void test() {
    properties.setProperty("some.key", "some.value");

    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some value without inner keys", file), items));
    assertEquals(0, validator.validate(new PropertyImpl(KEY_OK, "Some value with existing inner key ${some.key}",
        file), items));
    assertEquals(1, validator.validate(new PropertyImpl(KEY_KO,
        "Some value with non existing inner key ${some.other.key}", file), items));
  }
}
