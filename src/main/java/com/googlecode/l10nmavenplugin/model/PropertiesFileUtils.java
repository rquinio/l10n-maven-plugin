package com.googlecode.l10nmavenplugin.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public final class PropertiesFileUtils {

  /**
   * Hide constructor
   */
  private PropertiesFileUtils() {
  }

  /**
   * Regroup identical values by keys
   * 
   * @param <T>
   * 
   * @param map
   * @return
   */
  public static <T> Map<T, Collection<PropertiesFile>> reverseMap(Map<PropertiesFile, T> map) {
    Map<T, Collection<PropertiesFile>> reverseMap = new HashMap<T, Collection<PropertiesFile>>();

    for (Entry<PropertiesFile, T> entry : map.entrySet()) {
      PropertiesFile propertiesFile = entry.getKey();
      T parameters = entry.getValue();

      Collection<PropertiesFile> matchedPropertiesFiles = reverseMap.get(parameters);
      if (matchedPropertiesFiles == null) {
        matchedPropertiesFiles = new HashSet<PropertiesFile>();
        reverseMap.put(parameters, matchedPropertiesFiles);
      }
      matchedPropertiesFiles.add(propertiesFile);
    }
    return reverseMap;
  }

  /**
   * Get the most common value
   * 
   * @param reverseMap
   * @return
   */
  public static <T> T getMajorityKey(Map<T, Collection<PropertiesFile>> reverseMap) {
    T majorityKey = null;
    Collection<PropertiesFile> majority = null;
    for (Entry<T, Collection<PropertiesFile>> entry : reverseMap.entrySet()) {
      if (majority == null || entry.getValue().size() > majority.size()) {
        majority = entry.getValue();
        majorityKey = entry.getKey();
      }
    }
    return majorityKey;
  }

  /**
   * Reverse logic of {@link Locale#toString}
   * 
   * @param localeString
   * @return
   */
  public static Locale getLocale(String localeString) {
    Locale locale = null;
    if (!StringUtils.isEmpty(localeString)) {
      String[] parts = localeString.split("_", 3);

      if (parts.length == 1) {
        locale = new Locale(parts[0]);
      } else if (parts.length == 2) {
        locale = new Locale(parts[0], parts[1]);
      } else if (parts.length == 3) {
        locale = new Locale(parts[0], parts[1], parts[2]);
      }
    }
    return locale;
  }

  /**
   * Parent locale , ex: en_US => en
   * 
   * @param locale
   * @return null if no parent locale
   */
  public static Locale getParentLocale(Locale locale) {
    Locale parentLocale = null;
    if (locale != null) {
      String language = locale.getLanguage();
      String country = locale.getCountry();
      String variant = locale.getVariant();

      if (!StringUtils.isEmpty(language)) {
        if (!StringUtils.isEmpty(variant)) {
          parentLocale = new Locale(language, country);
        } else if (!StringUtils.isEmpty(country)) {
          parentLocale = new Locale(language);
        }
      }
    }
    return parentLocale;
  }
}
