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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * {@link java.util.ResourceBundle} based implementation of {@link PropertiesFamily}, using file name prefix as bundle base name.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class BundlePropertiesFamily implements PropertiesFamily {

  private static final String SEPARATOR = "_";

  private PropertiesFile rootPropertiesFile = null;

  private final Collection<PropertiesFile> propertiesFiles;

  private final Collection<PropertiesFile> propertiesFilesNoRoot = new ArrayList<PropertiesFile>();

  /**
   * 
   * @param propertiesFiles
   *          the Properties that are part of the bundle
   */
  public BundlePropertiesFamily(Collection<PropertiesFile> propertiesFiles) {
    this.propertiesFiles = propertiesFiles;

    // Determine root bundle (if any)
    for (PropertiesFile propertiesFile : propertiesFiles) {
      if (!propertiesFile.getFileName().contains(SEPARATOR)) {
        rootPropertiesFile = propertiesFile;
        break;
      }
    }

    propertiesFilesNoRoot.addAll(propertiesFiles);
    propertiesFilesNoRoot.remove(rootPropertiesFile);
  }

  /**
   * {@inheritDoc}
   */
  public Set<String> getKeys() {
    Set<String> keys = new HashSet<String>();
    for (PropertiesFile propertiesFile : propertiesFiles) {
      keys.addAll(new HashSet<String>(Collections.list((Enumeration<String>) propertiesFile.getProperties()
          .propertyNames())));
    }
    return keys;
  }

  /**
   * {@inheritDoc}
   */
  public String getBaseName() {
    if (propertiesFiles.size() > 0) {
      return getBundleBaseName(propertiesFiles.iterator().next().getFileName());
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public PropertiesFamily getPropertiesFamilyExcludingRoot() {
    return new BundlePropertiesFamily(propertiesFilesNoRoot);
  }

  /**
   * {@inheritDoc}
   */
  public int getNbPropertiesFiles() {
    return propertiesFiles.size();
  }

  /**
   * {@inheritDoc}
   */
  public PropertyFamily getPropertyFamily(String key) {
    return new BundlePropertyFamily(key, this);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<PropertyFamily> iterator() {
    return new Iterator<PropertyFamily>() {

      private final Iterator<String> keysIterator = getKeys().iterator();

      public boolean hasNext() {
        return keysIterator.hasNext();
      }

      public PropertyFamily next() {
        String key = keysIterator.next();
        return getPropertyFamily(key);
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  public Collection<PropertiesFile> getPropertiesFiles() {
    return propertiesFiles;
  }

  /**
   * Get the root bundle
   * 
   * @return rootBundle, or null if not found
   */
  public PropertiesFile getRootPropertiesFile() {
    return rootPropertiesFile;
  }

  /**
   * Get the base name of bundle, based on default {@link java.util.ResourceBundle} convention baseName[_language[_country[_variant]]].properties
   * 
   * @param propertiesName
   *          file name of 1 properties of the bundle
   * @return baseName of the bundle
   */
  protected String getBundleBaseName(String propertiesName) {
    int index = propertiesName.indexOf(SEPARATOR);
    if (index != -1) {
      return propertiesName.substring(0, index);
    } else {
      return FilenameUtils.getBaseName(propertiesName);
    }
  }

  @Override
  public String toString() {
    return "Bundle with base name " + getBaseName();
  }
}
