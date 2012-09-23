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
