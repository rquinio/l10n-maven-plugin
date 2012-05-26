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
 * Group of {@link PropertiesFile} belonging to the same family
 * 
 * @author romain.quinio
 * 
 */
public interface PropertiesFamily {

  /**
   * The Properties files of the bundle
   * 
   * @return
   */
  public Collection<PropertiesFile> getPropertiesFiles();

  /**
   * Number of files (i.e languages)
   * 
   * @return
   */
  public int getNbPropertiesFiles();

  /**
   * Exclude the root Properties file
   * 
   * @return
   */
  public PropertiesFamily getPropertiesFamilyExcludingRoot();

  /**
   * Root Properties files
   * 
   * @return can be null
   */
  public PropertiesFile getRootPropertiesFile();

  /**
   * Set of keys used in the Properties
   */
  public Set<String> getKeys();

  /**
   * Base name of family
   */
  public String getBaseName();

  public PropertyFamily getPropertyFamily(String key);

  /**
   * Iterator over keys
   * 
   * @return
   */
  public Iterator<PropertyFamily> iterator();

}
