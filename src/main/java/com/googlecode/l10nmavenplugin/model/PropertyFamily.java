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
import java.util.Properties;

/**
 * Values of resource from different {@link Properties} files belonging to the same family
 * 
 * @author romain.quinio
 * 
 */
public interface PropertyFamily {

  /**
   * Key of the property
   * 
   * @return
   */
  public String getKey();

  /**
   * Different values of the property, with associated files (empty values are ignored).
   * 
   * @return
   */
  public Collection<Property> getValues();

  /**
   * List of properties files for which resource is not defined (either null or empty)
   * 
   * @return
   */
  public Collection<PropertiesFile> getMissingPropertyFiles();

  /**
   * List of properties files for which resource is defined
   * 
   * This is the opposite of {@link #getMissingPropertyFiles}
   * 
   * @return
   */
  public Collection<PropertiesFile> getExistingPropertyFiles();

  /**
   * Family excluding root bundle
   * 
   * @return
   */
  public PropertyFamily getPropertyFamilyExcludingRoot();

  /**
   * Number of properties files in the bundle
   * 
   * @return
   */
  public int getNbPropertiesFiles();

}
