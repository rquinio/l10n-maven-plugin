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
import java.util.Map;
import java.util.Set;

/**
 * {@link java.util.ResourceBundle} based implementation of {@link PropertyFamily}, using the notion of hierarchical bundles with
 * a root usually containing non-localized resources.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class BundlePropertyFamily implements PropertyFamily {

  /**
   * The group of properties file to which the property belongs.
   */
  private PropertiesFamily family;

  /**
   * The key of the resource
   */
  private String key;

  public BundlePropertyFamily(String key, PropertiesFamily family) {
    this.family = family;
    this.key = key;
  }

  /**
   * {@inheritDoc}
   */
  public String getKey() {
    return key;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<PropertiesFile> getMissingPropertyFiles() {
    Set<PropertiesFile> missingPropertyFiles = new HashSet<PropertiesFile>();

    for (PropertiesFile propertiesFile : family.getPropertiesFiles()) {
      String message = propertiesFile.getProperties().getProperty(key);
      if (message == null || message.length() == 0) {
        missingPropertyFiles.add(propertiesFile);
      }
    }
    return missingPropertyFiles;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<PropertiesFile> getExistingPropertyFiles() {
    Set<PropertiesFile> existingPropertyFiles = new HashSet<PropertiesFile>();
    Collection<PropertiesFile> missingPropertyFiles = getMissingPropertyFiles();

    for (PropertiesFile propertiesFile : family.getPropertiesFiles()) {
      if (!missingPropertyFiles.contains(propertiesFile)) {
        existingPropertyFiles.add(propertiesFile);
      }
    }
    return existingPropertyFiles;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<Property> getValues() {

    Map<String, Property> values = new HashMap<String, Property>();

    for (PropertiesFile propertiesFile : family.getPropertiesFiles()) {
      String message = propertiesFile.getProperties().getProperty(key);
      if (message != null && message.length() > 0) {
        if (values.get(message) == null) {
          values.put(message, new PropertyImpl(key, message, null));
        }
        values.get(message).addContainingPropertiesFile(propertiesFile);
      }
    }

    // List<Entry<String, Property>> list = new ArrayList<Entry<String, Property>>(values.entrySet());
    //
    // // Sort the list by frequency of the property value
    // Collections.sort(list, new Comparator<Map.Entry<String, Property>>() {
    // public int compare(Entry<String, Property> o1, Entry<String, Property> o2) {
    //
    // return o1.getValue().getContainingPropertiesFiles().size() - o2.getValue().getContainingPropertiesFiles().size();
    // }
    // });
    //
    // Collection<Property> sortedValues = new ArrayList<Property>();
    // for (Entry<String, Property> entry : list) {
    // sortedValues.add(entry.getValue());
    // }

    return values.values();
  }

  public PropertyFamily getPropertyFamilyExcludingRoot() {
    return new BundlePropertyFamily(key, family.getPropertiesFamilyExcludingRoot());
  }

  public int getNbPropertiesFiles() {
    return family.getNbPropertiesFiles();
  }
}
