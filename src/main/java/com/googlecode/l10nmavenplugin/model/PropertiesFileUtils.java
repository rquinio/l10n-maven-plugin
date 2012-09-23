/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class on the model.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public final class PropertiesFileUtils {

  private static final int MAX_LOCALE_PARTS = 3;

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
      String[] parts = localeString.split("_", MAX_LOCALE_PARTS);

      if (parts.length == 1) {
        locale = new Locale(parts[0]);
      } else if (parts.length == 2) {
        locale = new Locale(parts[0], parts[1]);
      } else if (parts.length == MAX_LOCALE_PARTS) {
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
