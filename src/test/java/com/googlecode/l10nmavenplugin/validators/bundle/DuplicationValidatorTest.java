package com.googlecode.l10nmavenplugin.validators.bundle;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class DuplicationValidatorTest extends AbstractL10nValidatorTest<PropertiesFamily> {

  private static final String KEY_1 = "key1";
  private static final String KEY_2 = "key2";
  private static final String KEY_3 = "key3";

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new DuplicationValidator(logger);
  }

  @Test
  public void testNoDuplication() {
    bundleA.put(KEY_1, "A");
    bundleB.put(KEY_1, "B");
    bundleC.put(KEY_1, "C");
    bundleD.put(KEY_1, "D");
    bundleE.put(KEY_1, "E");

    bundleA.put(KEY_2, "notA");
    bundleB.put(KEY_2, "notB");
    bundleC.put(KEY_2, "notC");
    bundleD.put(KEY_2, "notD");
    bundleE.put(KEY_2, "notE");

    validator.validate(propertiesFamily, items);

    assertEquals(0, items.size());
  }

  @Test
  public void testStrictDuplication() {
    bundleA.put(KEY_1, "A");
    bundleB.put(KEY_1, "B");
    bundleC.put(KEY_1, "C");
    bundleD.put(KEY_1, "D");
    bundleE.put(KEY_1, "E");

    bundleA.put(KEY_2, "A");
    bundleB.put(KEY_2, "B");
    bundleC.put(KEY_2, "C");
    bundleD.put(KEY_2, "D");
    bundleE.put(KEY_2, "E");

    validator.validate(propertiesFamily, items);

    assertEquals(1, items.size());
  }

  @Test
  public void testAlmostDuplication() {
    bundleA.put(KEY_1, "A");
    bundleB.put(KEY_1, "B");
    bundleC.put(KEY_1, "C");
    bundleD.put(KEY_1, "D");
    bundleE.put(KEY_1, "E");

    bundleA.put(KEY_2, "A");
    bundleB.put(KEY_2, "B");
    bundleC.put(KEY_2, "notC");
    bundleD.put(KEY_2, "D");
    bundleE.put(KEY_2, "E");

    validator.validate(propertiesFamily, items);

    // assertEquals(1, items.size());
  }

  @Test
  public void testMultiplePropertyDuplication() {
    bundleA.put(KEY_1, "A");
    bundleB.put(KEY_1, "B");
    bundleC.put(KEY_1, "C");
    bundleD.put(KEY_1, "D");
    bundleE.put(KEY_1, "E");

    bundleA.put(KEY_2, "A");
    bundleB.put(KEY_2, "B");
    bundleC.put(KEY_2, "notC");
    bundleD.put(KEY_2, "D");
    bundleE.put(KEY_2, "E");

    bundleA.put(KEY_3, "A");
    bundleB.put(KEY_3, "notB");
    bundleC.put(KEY_3, "C");
    bundleD.put(KEY_3, "D");
    bundleE.put(KEY_3, "E");

    validator.validate(propertiesFamily, items);

    // No grouping
    // assertEquals(2, items.size());
  }
}
