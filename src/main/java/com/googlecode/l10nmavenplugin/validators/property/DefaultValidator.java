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

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Default validator, used in case no other specific validator was triggered.
 * 
 * It should detect if the resource could have been validated by another validator, meaning either that the keys naming convention
 * was not followed, or that the plugin configuration might not be correct.
 * 
 * @author romain.quinio
 * @since 1.0
 * 
 */
public class DefaultValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  public DefaultValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  /**
   * Warn if resources contain HTML or some URL.
   * 
   * @param property
   * @return Number of errors
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    if (PlainTextValidator.isHtml(property.getMessage())) {
      L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.UNDECLARED_HTML_RESOURCE,
          "Resource may contain HTML, but is not declared as such. No validation was performed.", property.getPropertiesFile()
              .toString(), property.getKey(), property.getMessage(), null);
      reportItems.add(reportItem);
      logger.log(reportItem);

    } else if (PlainTextValidator.isUrl(property.getMessage())) {
      L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.UNDECLARED_URL_RESOURCE,
          "Resource may contain URL, but is not declared as such. No validation was performed.", property.getPropertiesFile()
              .toString(), property.getKey(), property.getMessage(), null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }
}
