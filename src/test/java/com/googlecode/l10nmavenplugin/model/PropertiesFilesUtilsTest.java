package com.googlecode.l10nmavenplugin.model;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

public class PropertiesFilesUtilsTest {

  @Test
  public void noLocaleShouldBeNull() {
    assertNull(PropertiesFileUtils.getLocale(""));
  }

  @Test
  public void testLanguageLocale() {
    assertEquals(Locale.ENGLISH, PropertiesFileUtils.getLocale(Locale.ENGLISH.toString()));
  }

  @Test
  public void testLanguageAndVariantLocale() {
    assertEquals(Locale.SIMPLIFIED_CHINESE, PropertiesFileUtils.getLocale(Locale.SIMPLIFIED_CHINESE.toString()));
  }

  @Test
  public void nullParentLocaleShouldBeNull() {
    assertNull(PropertiesFileUtils.getParentLocale(null));
    assertNull(PropertiesFileUtils.getParentLocale(new Locale("")));
  }

  @Test
  public void languageParentLocaleShouldBeNull() {
    assertNull(PropertiesFileUtils.getParentLocale(Locale.ENGLISH));
  }

  @Test
  public void countryWithNoLanguageParentLocaleShouldBeNull() {
    assertNull(PropertiesFileUtils.getParentLocale(new Locale("", "US")));
  }

  @Test
  public void countryParentLocaleShouldBeLanguage() {
    assertEquals(Locale.ENGLISH, PropertiesFileUtils.getParentLocale(Locale.UK));
  }

  @Test
  public void variantParentLocaleShouldBeCountry() {
    assertEquals(Locale.UK, PropertiesFileUtils.getParentLocale(new Locale("en", "GB", "variant")));
  }
}
