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

import org.codehaus.plexus.util.StringUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Check for trailing spaces/invisible characters at the end of a resource
 * 
 * @author romain.quinio
 * 
 */
public class TrailingWhitespaceValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  public TrailingWhitespaceValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  /**
   * Warn if resource ends with any Java whitespace char (" ", "\t", "\n", ...)
   * 
   * @see Character#isWhitespace
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    String message = property.getMessage();
    if (message.length() > 0) {
      Character tail = message.charAt(message.length() - 1);
      if (Character.isWhitespace(tail)) {
        L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.TRAILING_WHITESPACE,
            "Resource ends with whitespace character [" + StringUtils.escape(tail.toString())
                + "] which may indicate some resources concatenation", property, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    }
    return 0;
  }
}
