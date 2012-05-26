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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

public class BundlePropertiesFile implements PropertiesFile {

  private String fileName;

  private Properties properties;

  private Locale locale;

  public BundlePropertiesFile(String fileName, Properties properties) {
    this.fileName = fileName;
    this.properties = properties;

    String[] parts = FilenameUtils.getBaseName(fileName).split("_", 2);
    if (parts.length == 2) {
      this.locale = PropertiesFileUtils.getLocale(parts[1]);
    }
  }

  public String getFileName() {
    return fileName;
  }

  public Locale getLocale() {
    return locale;
  }

  public Properties getProperties() {
    return properties;
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getDuplicatedResourceKeys() {
    Set<String> duplicatedResourceKeys = new HashSet<String>();

    List<Map.Entry<String, String>> list = new ArrayList(properties.entrySet());
    // Sort the list by values, to be able to detect duplicates
    Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
      public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });

    String previousValue = null;
    for (Map.Entry<String, String> entry : list) {
      if (entry.getValue().equals(previousValue)) {
        duplicatedResourceKeys.add(entry.getKey());
      }
      previousValue = entry.getValue();
    }

    return duplicatedResourceKeys;
  }

  @Override
  public String toString() {
    return fileName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BundlePropertiesFile)) {
      return false;
    }
    BundlePropertiesFile other = (BundlePropertiesFile) obj;
    if (fileName == null) {
      if (other.fileName != null) {
        return false;
      }
    } else if (!fileName.equals(other.fileName)) {
      return false;
    }
    return true;
  }
}
