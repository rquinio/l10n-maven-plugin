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
 * Performs validation of absolute (http(s),ftp and mailto) or scheme relative URLs.
 * 
 * @author romain.quinio
 * 
 */
public class UrlValidator implements L10nValidator {

  /**
   * URL regex validation (cf ESAPI.properties)
   */
  private static final String RELATIVE_URL_VALIDATION_REGEXP = "//[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(/?)([a-zA-Z0-9-\\.\\?,:'/\\\\+=&%\\$#_]*)?";

  /**
   * E-mail regex validation (cf ESAPI.properties)
   */
  private static final String EMAIL_VALIDATION_REGEXP = "[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}";

  /**
   * Validation of an absolute URL.
   * 
   * Protocol must be included in URL (either http(s), mailto, or scheme relative)
   */
  private static final String URL_VALIDATION_REGEXP = "^(((ht|f)tp(s?):)?" + RELATIVE_URL_VALIDATION_REGEXP + ")|(mailto:)"
      + EMAIL_VALIDATION_REGEXP + ".*$";

  protected static final Pattern URL_VALIDATION_PATTERN = Pattern.compile(URL_VALIDATION_REGEXP);

  private L10nValidatorLogger logger;

  public UrlValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Validate URLs using regexp.
   * 
   * Performs a MessageFormat if needed.
   * 
   * @note the URLValidator from Apache does not seem to support scheme relative URLs.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String formattedMessage = message;

    try {
      if (ParametricMessageValidator.isParametric(key, formattedMessage, propertiesName)) {
        formattedMessage = ParametricMessageValidator.defaultFormat(message);
      }
      // Unescape HTML in case URL is used in HTML context (ex: &amp; -> &)
      String url = StringEscapeUtils.unescapeHtml(formattedMessage);
      Matcher m = URL_VALIDATION_PATTERN.matcher(url);

      if (!m.matches()) {
        nbErrors++;
        L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.URL_VALIDATION, "Invalid absolute URL.",
            propertiesName, key, message, formattedMessage);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    } catch (IllegalArgumentException e) {
      // Catch MessageFormat errors in case of malformed message
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.MALFORMED_PARAMETER,
          "URL contains malformed parameters: " + e.getMessage(), propertiesName, key, message, formattedMessage);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    throw new UnsupportedOperationException();
  }

}
