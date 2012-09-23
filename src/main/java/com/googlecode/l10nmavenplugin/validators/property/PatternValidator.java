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

import com.googlecode.l10nmavenplugin.CustomPattern;
import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.PropertiesKeyConventionValidator;

/**
 * Validator to check properties against a customizable Regex.
 * 
 * This allows basic customization, without having to implement a specific validator.
 * 
 * @author romain.quinio
 * @since 1.3
 */
public class PatternValidator extends PropertiesKeyConventionValidator implements L10nValidator<Property> {

  private final String name;
  private final Pattern pattern;

  public PatternValidator(L10nValidatorLogger logger, CustomPattern customPattern) {
    super(logger, customPattern.getKeys());
    this.pattern = Pattern.compile(customPattern.getRegex());
    this.name = customPattern.getName();
  }

  /**
   * Try to match the pattern
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    Matcher m = pattern.matcher(property.getMessage());
    if (!m.matches()) {
      L10nReportItem reportItem = new L10nReportItem(Type.CUSTOM_PATTERN, "Failed to match pattern '" + name + "' with regex " + pattern.pattern(), property,
          null);
      reportItems.add(reportItem);
      logger.log(reportItem);
      nbErrors++;
    }
    return nbErrors;
  }

  public boolean shouldValidate(Property property) {
    return matches(property.getKey());
  }
}
