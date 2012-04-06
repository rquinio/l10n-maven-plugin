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

import org.apache.commons.lang.StringEscapeUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Performs Js validation of a property, chaining with XHTML validation.
 * 
 * @author romain.quinio
 * 
 */
public class JsValidator implements L10nValidator {

  /**
   * Validation of javascript escaping
   * 
   * " \n \r are not allowed in js resources, as it would cause a script error.
   * 
   * \\\\\" => Regex escaping => \\\" => Java String escaping => \"
   * 
   * \\\\n => Regex escpaing => \\n => Java String escaping => \n
   */
  private static final String JS_DOUBLE_QUOTED_VALIDATION_REGEXP = "^(?:[^\"]|\\\\\")*$";
  private static final String JS_SINGLE_QUOTED_VALIDATION_REGEXP = "^(?:[^\']|\\\\\')*$";
  private static final String JS_NEWLINE_VALIDATION_REGEXP = "^(?:[^\\n\\r]|\\\\n|\\\\r)*$";

  protected static final Pattern JS_DOUBLE_QUOTED_VALIDATION_PATTERN = Pattern.compile(JS_DOUBLE_QUOTED_VALIDATION_REGEXP);
  protected static final Pattern JS_SINGLE_QUOTED_VALIDATION_PATTERN = Pattern.compile(JS_SINGLE_QUOTED_VALIDATION_REGEXP);
  protected static final Pattern JS_NEWLINE_VALIDATION_PATTERN = Pattern.compile(JS_NEWLINE_VALIDATION_REGEXP);

  private final L10nValidator xhtmValidator;

  private boolean jsDoubleQuoted;

  private L10nValidatorLogger logger;

  public JsValidator(L10nValidator xhtmValidator, L10nValidatorLogger logger) {
    this(true, xhtmValidator, logger);
  }

  public JsValidator(boolean jsDoubleQuoted, L10nValidator xhtmValidator, L10nValidatorLogger logger) {
    this.logger = logger;
    this.xhtmValidator = xhtmValidator;
    this.jsDoubleQuoted = jsDoubleQuoted;
  }

  /**
   * Validate js resource using regexp and then XHTML validation
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    // Check for quotes
    Matcher m;
    if (jsDoubleQuoted) {
      m = JS_DOUBLE_QUOTED_VALIDATION_PATTERN.matcher(message);
    } else {
      m = JS_SINGLE_QUOTED_VALIDATION_PATTERN.matcher(message);
    }

    if (!m.matches()) {
      nbErrors++;
      L10nReportItem reportItem;
      if (jsDoubleQuoted) {
        reportItem = new L10nReportItem(Severity.ERROR, Type.JS_DOUBLE_QUOTED_VALIDATION,
            "Double quoted js resources must not contain \"." + " Note that Properties silently drop \\ character before \", "
                + "so it mused be escaped as \\\\\" for proper js escaping", propertiesName, key, message, null);
      } else {
        reportItem = new L10nReportItem(Severity.ERROR, Type.JS_SINGLE_QUOTED_VALIDATION,
            "Single quoted js resources must not contain '" + " Note that Properties silently drop \\ character before ', "
                + "so it mused be escaped as \\\\' for proper js escaping", propertiesName, key, message, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    }

    // Check for newline
    m = JS_NEWLINE_VALIDATION_PATTERN.matcher(message);
    if (!m.matches()) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(
          Severity.ERROR,
          Type.JS_NEWLINE_VALIDATION,
          "Js resources must not contain \\n nor \\r, since newline is interpreted by browsers as the end of javascript statement",
          propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }

    // Unescaped any javascript escaping before HTML validation.
    String jsUnescapedMessage = StringEscapeUtils.unescapeJavaScript(message);

    // False positive if parameters are replaced client-side, and formatter does not follow MessageFormat ' escaping.
    nbErrors += xhtmValidator.validate(key, jsUnescapedMessage, propertiesName, reportItems);
    return nbErrors;
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    throw new UnsupportedOperationException();
  }
}
