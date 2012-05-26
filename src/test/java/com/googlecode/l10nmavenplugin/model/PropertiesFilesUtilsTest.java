package com.googlecode.l10nmavenplugin.model;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

public class PropertiesFilesUtilsTest {

  @Test
  public void testLocale() {
    assertNull(PropertiesFileUtils.getLocale(""));
    assertEquals(Locale.ENGLISH, PropertiesFileUtils.getLocale(Locale.ENGLISH.toString()));
    assertEquals(Locale.SIMPLIFIED_CHINESE, PropertiesFileUtils.getLocale(Locale.SIMPLIFIED_CHINESE.toString()));
  }

  @Test
  public void testParentLocale() {
    assertNull(PropertiesFileUtils.getParentLocale(null));
    assertNull(PropertiesFileUtils.getParentLocale(Locale.ENGLISH));
    assertNull(PropertiesFileUtils.getParentLocale(new Locale("", "US")));
    assertNull(PropertiesFileUtils.getParentLocale(new Locale("en")));
    assertNull(PropertiesFileUtils.getParentLocale(new Locale("")));

    assertEquals(Locale.ENGLISH, PropertiesFileUtils.getParentLocale(Locale.UK));
    assertEquals(Locale.UK, PropertiesFileUtils.getParentLocale(new Locale("en", "GB", "variant")));
  }
}
