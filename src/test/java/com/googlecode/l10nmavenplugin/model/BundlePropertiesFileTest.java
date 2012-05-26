package com.googlecode.l10nmavenplugin.model;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

public class BundlePropertiesFileTest {

  @Test
  public void testLocale() {
    PropertiesFile file = new BundlePropertiesFile("Bundle_en.properties", null);
    assertEquals(Locale.ENGLISH, file.getLocale());
  }

  @Test
  public void testLocale2() {
    PropertiesFile file = new BundlePropertiesFile("Bundle_zh_CN.properties", null);
    assertEquals(Locale.SIMPLIFIED_CHINESE, file.getLocale());
  }

  @Test
  public void testNullLocale() {
    PropertiesFile file = new BundlePropertiesFile("Bundle.properties", null);
    assertNull(file.getLocale());
  }
}
