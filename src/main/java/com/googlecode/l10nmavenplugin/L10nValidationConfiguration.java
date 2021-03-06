/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin;

import java.io.File;

/**
 * Interface for l10 validation/report Mojo configuration
 * 
 * @author romain.quinio
 * 
 */
public interface L10nValidationConfiguration {

  void setPropertyDir(File propertyDir);

  File getPropertyDir();

  void setExcludedKeys(String[] excludedKeys);

  String[] getExcludedKeys();

  void setIgnoreFailure(boolean ignoreFailure);

  boolean getIgnoreFailure();

  void setJsKeys(String[] jsKeys);

  String[] getJsKeys();

  void setJsDoubleQuoted(boolean jsDoubleQuoted);

  boolean getJsDoubleQuoted();

  void setUrlKeys(String[] urlKeys);

  String[] getUrlKeys();

  void setHtmlKeys(String[] htmlKeys);

  String[] getHtmlKeys();

  void setXhtmlSchema(File xhtmlSchema);

  File getXhtmlSchema();

  void setTextKeys(String[] textKeys);

  String[] getTextKeys();

  void setCustomPatterns(CustomPattern[] customPatterns);

  CustomPattern[] getCustomPatterns();

  void setDictionaryDir(File dictionaryDir);

  File getDictionaryDir();

  void setSkip(boolean skip);

  boolean getSkip();

  void setReportsDir(File reportsDir);

  File getReportsDir();

  String getFormatter();

  String getInnerResourceRegex();
}
