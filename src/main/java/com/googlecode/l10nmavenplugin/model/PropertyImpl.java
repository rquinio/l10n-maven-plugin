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
import java.util.Locale;

/**
 * Basic property POJO
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class PropertyImpl implements Property {

  private String key;

  private Locale locale;

  private Collection<PropertiesFile> containingPropertiesFiles;

  private String message;

  public PropertyImpl(String key, String message, PropertiesFile propertiesFile) {
    this.key = key;
    this.message = message;
    this.containingPropertiesFiles = new ArrayList<PropertiesFile>();
    if (propertiesFile != null) {
      this.locale = propertiesFile.getLocale();
      this.containingPropertiesFiles.add(propertiesFile);
    }
  }

  public String getKey() {
    return key;
  }

  public String getMessage() {
    return message;
  }

  public Collection<PropertiesFile> getContainingPropertiesFiles() {
    return containingPropertiesFiles;
  }

  public void addContainingPropertiesFile(PropertiesFile propertiesFile) {
    containingPropertiesFiles.add(propertiesFile);
  }

  public PropertiesFile getPropertiesFile() {
    return containingPropertiesFiles.iterator().next();
  }

  @Override
  public String toString() {
    return "ResourceBundleProperty [key=" + key + ", containingPropertiesFiles=" + containingPropertiesFiles + ", message="
        + message + "]";
  }

  public Locale getLocale() {
    return locale;
  }
}
