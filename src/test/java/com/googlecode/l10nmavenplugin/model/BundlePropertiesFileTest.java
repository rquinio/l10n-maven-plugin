package com.googlecode.l10nmavenplugin.model;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

public class BundlePropertiesFileTest {

  @Test
  public void testLanguageLocale() {
    PropertiesFile file = new BundlePropertiesFile("Bundle_en.properties", null);
    assertEquals(Locale.ENGLISH, file.getLocale());
  }

  @Test
  public void testLanguageAndCountryLocale() {
    PropertiesFile file = new BundlePropertiesFile("Bundle_zh_CN.properties", null);
    assertEquals(Locale.SIMPLIFIED_CHINESE, file.getLocale());
  }

  @Test
  public void testLanguageAndCountryAndVariantLocale() {
    PropertiesFile file = new BundlePropertiesFile("Bundle_zh_CN_var.properties", null);
    assertEquals(new Locale("zh", "CN", "var"), file.getLocale());
  }

  @Test
  public void noLocaleShouldBeNull() {
    PropertiesFile file = new BundlePropertiesFile("Bundle.properties", null);
    assertNull(file.getLocale());
  }
}
