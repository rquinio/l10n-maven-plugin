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
import java.util.Iterator;
import java.util.Set;

/**
 * Representation of a multi-language bundle, i.e. a group of {@link PropertiesFile} belonging to the same "family"
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public interface PropertiesFamily {

  /**
   * The Properties files of the bundle
   * 
   * @return
   */
  Collection<PropertiesFile> getPropertiesFiles();

  /**
   * Number of files (i.e languages)
   * 
   * @return
   */
  int getNbPropertiesFiles();

  /**
   * Exclude the root Properties file
   * 
   * @return
   */
  PropertiesFamily getPropertiesFamilyExcludingRoot();

  /**
   * Root Properties files
   * 
   * @return can be null
   */
  PropertiesFile getRootPropertiesFile();

  /**
   * Set of keys used in the Properties
   */
  Set<String> getKeys();

  /**
   * Base name of family
   */
  String getBaseName();

  /**
   * Get a property from this family with given key
   * 
   * @param key
   * @return
   */
  PropertyFamily getPropertyFamily(String key);

  /**
   * Iterator over keys
   * 
   * @return
   */
  Iterator<PropertyFamily> iterator();

}
