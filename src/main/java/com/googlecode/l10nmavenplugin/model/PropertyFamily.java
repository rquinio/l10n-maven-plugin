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

/**
 * Representation of a multi-language property, i.e. the various values of a property across a bundle of {@link java.util.Properties} files.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public interface PropertyFamily {

  /**
   * Key of the property
   * 
   * @return
   */
  String getKey();

  /**
   * Different values of the property, with associated files (empty values are ignored).
   * 
   * @return
   */
  Collection<Property> getValues();

  /**
   * List of properties files for which resource is not defined (either null or empty)
   * 
   * @return
   */
  Collection<PropertiesFile> getMissingPropertyFiles();

  /**
   * List of properties files for which resource is defined
   * 
   * This is the opposite of {@link #getMissingPropertyFiles}
   * 
   * @return
   */
  Collection<PropertiesFile> getExistingPropertyFiles();

  /**
   * Family excluding root bundle
   * 
   * @return
   */
  PropertyFamily getPropertyFamilyExcludingRoot();

  /**
   * Number of properties files in the bundle
   * 
   * @return
   */
  int getNbPropertiesFiles();

}
