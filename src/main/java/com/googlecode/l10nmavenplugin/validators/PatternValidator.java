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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Validate against a customizable Regex.
 * 
 * @author romain.quinio
 */
public class PatternValidator implements L10nValidator {

  private String name;
  private Pattern pattern;
  private L10nValidatorLogger logger;

  public PatternValidator(L10nValidatorLogger logger, String name, String regex) {
    this.logger = logger;
    this.pattern = Pattern.compile(regex);
    this.name = name;
  }

  /**
   * Try to match the pattern
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    Matcher m = pattern.matcher(message);
    if (!m.matches()) {
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.CUSTOM_PATTERN, "Failed to match pattern '" + name
          + "' with regex " + pattern.pattern(), propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
      nbErrors++;
    }
    return nbErrors;
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    throw new UnsupportedOperationException();
  }

}
