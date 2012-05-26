/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators;

import java.util.List;
import java.util.Set;

/**
 * Interface for all l10n validation modules
 * 
 * @author romain.quinio
 */
public interface L10nValidator {

  /**
   * Validates a property
   * 
   * @param key
   *          key of the property
   * @param message
   *          value to validate, potentially different from the original property value in case of chaining validators
   * @param propertiesName
   *          identification of the Properties file, usually file name.
   * @return number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems);

  /**
   * Report on validation results
   * 
   * @param propertiesNames
   * @return
   * @throw java.lang.UnsupportedOperationException if the method is not applicable for the validator
   */
  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems);
}
