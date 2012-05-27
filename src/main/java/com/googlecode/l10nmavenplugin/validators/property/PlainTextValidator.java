/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.property;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Performs validation of plain text properties (non HTML, non URL).
 * 
 * For instance when using some HTML tags in title attribute, the markup is not evaluated and displayed 'as is'.
 * 
 * @author romain.quinio
 * @since 1.1
 * 
 */
public class PlainTextValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  /**
   * Basic detection of HTML tags
   */
  private static final String HTML_REGEXP = "^.*\\<[^>]+>.*$";

  /**
   * Basic detection of URL
   */
  private static final String URL_REGEXP = "^.*(?://|mailto).*$";

  protected static final Pattern HTML_PATTERN = Pattern.compile(HTML_REGEXP);
  protected static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

  private L10nValidator<Property> spellCheckValidator;

  public PlainTextValidator(L10nValidatorLogger logger, L10nValidator<Property> spellCheckValidator) {
    super(logger);
    this.spellCheckValidator = spellCheckValidator;
  }

  /**
   * Check resource does not contain HTML/URL
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    if (isHtml(property.getMessage())) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.TEXT_VALIDATION_NO_HTML,
          "Text resource must not contain HTML.", property, null);
      reportItems.add(reportItem);
      logger.log(reportItem);

    } else if (isUrl(property.getMessage())) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.TEXT_VALIDATION_NO_URL,
          "Text resource must not contain URL.", property, null);
      reportItems.add(reportItem);
      logger.log(reportItem);

    } else if (spellCheckValidator != null) {
      // Chain with spellCheck validation
      nbErrors += spellCheckValidator.validate(property, reportItems);
    }
    return nbErrors;
  }

  public static boolean isHtml(String message) {
    Matcher htmlMatcher = HTML_PATTERN.matcher(message);
    return htmlMatcher.matches();
  }

  public static boolean isUrl(String message) {
    Matcher urlMatcher = URL_PATTERN.matcher(message);
    return urlMatcher.matches();
  }

  public void setSpellCheckValidator(L10nValidator<Property> spellCheckValidator) {
    this.spellCheckValidator = spellCheckValidator;
  }
}
