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

import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Wrapper around a {@link java.util.Properties} object, to add info on the file itself.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public interface PropertiesFile {

  /**
   * The file name (ex: bundle_en.properties), the naming convention being bundleName_locale.properties
   * 
   * @return
   */
  String getFileName();

  /**
   * The name of the bundle (i.e file name without locale nor file extension)
   */
  String getBundleName();

  /**
   * The {@link Locale} of the file
   * 
   * @return null if unknown, or file is root
   */
  Locale getLocale();

  /**
   * The {@link Properties} object loaded from the file
   * 
   * @return
   */
  Properties getProperties();

  /**
   * Set of keys for resources whose value is not unique in the file
   * 
   * @return
   */
  Set<String> getDuplicatedResourceKeys();

}
