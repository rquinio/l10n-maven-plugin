package com.googlecode.l10nmavenplugin.validators.bundle;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class HtmlTagCoherenceValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new HtmlTagCoherenceValidator(logger);
  }

  @Test
  public void testCoherentTags() {
    bundleA.put(KEY_OK, "<div><a href=''>A link</a></div>");
    bundleB.put(KEY_OK, "<div><a href=''> A translated link </a></div>");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  @Test
  public void testIncoherentTags() {
    bundleA.put(KEY_KO, "<div><a href=''>A link</a></div>");
    bundleB.put(KEY_KO, "<div><a href=''> A translated link </a></div>");
    bundleC.put(KEY_KO, "<a href=''>A link</a>");
    bundleD.put(KEY_KO, "<a href=''>A <br/> link</a>");
    bundleE.put(KEY_KO, "A \"link\"");
    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(3, items.size());
  }

}
