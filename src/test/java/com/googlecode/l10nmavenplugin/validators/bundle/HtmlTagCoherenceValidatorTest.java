package com.googlecode.l10nmavenplugin.validators.bundle;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.BundlePropertyFamily;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class HtmlTagCoherenceValidatorTest extends AbstractL10nValidatorTest<PropertyFamily> {

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new HtmlTagCoherenceValidator(logger, new String[] { ".text." });
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
    assertThat(items, hasItem(new ItemTypeMatcher(Type.INCOHERENT_TAGS)));
  }

  @Test
  public void tagOrderShouldNotMatter() {
    bundleA.put(KEY_OK, "<div /><span />");
    bundleB.put(KEY_OK, "<span /><div />");

    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_OK, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(0, items.size());
  }

  @Test
  public void tagNestingShouldMatter() {
    bundleA.put(KEY_KO, "<div><span /></div>");
    bundleB.put(KEY_KO, "<span><div /></span>");

    PropertyFamily propertyFamily = new BundlePropertyFamily(KEY_KO, propertiesFamily);

    validator.validate(propertyFamily, items);
    assertEquals(1, items.size());
  }

  @Test
  public void testShouldValidate() {
    assertTrue(validator.shouldValidate(new BundlePropertyFamily("page.text.key", propertiesFamily)));
    assertFalse(validator.shouldValidate(new BundlePropertyFamily(KEY_OK, propertiesFamily)));
  }
}
