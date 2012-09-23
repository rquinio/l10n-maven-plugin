/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.orchestrator;

import java.util.List;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.PropertiesKeyConventionValidator;

/**
 * Orchestrates validation of a single property value
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class PropertyValidator extends PropertiesKeyConventionValidator implements L10nValidator<Property> {

  private L10nValidator<Property> htmlValidator;

  private L10nValidator<Property> jsValidator;

  private L10nValidator<Property> urlValidator;

  private L10nValidator<Property> plainTextValidator;

  private L10nValidator<Property>[] patternValidators;

  private L10nValidator<Property> defaultValidator;

  private L10nValidator<Property> parametricMessageValidator;

  private L10nValidator<Property> trailingWhitespaceValidator;

  /**
   * Better use setter injection for L10nValidator as they share same type
   */
  public PropertyValidator(L10nValidatorLogger logger, String[] excludedKeys) {
    super(logger, excludedKeys);
  }

  /**
   * Validate a single property of a Properties file
   * 
   * @param key
   *          properties key
   * @param message
   *          properties value
   * @param propertiesName
   *          the name of the .properties file, for error logging
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String key = property.getKey();

    // Nothing to validate if message is empty.
    if (property.getMessage().length() > 0) {

      logger.debug(property.getPropertiesFile().toString(), key, "Starting validation...", null, null);

      nbErrors += parametricMessageValidator.validate(property, reportItems);
      nbErrors += trailingWhitespaceValidator.validate(property, reportItems);

      boolean bMatched = false;
      if (htmlValidator.shouldValidate(property)) {
        bMatched = true;
        nbErrors += htmlValidator.validate(property, reportItems);

      } else if (jsValidator.shouldValidate(property)) {
        bMatched = true;
        nbErrors += jsValidator.validate(property, reportItems);

      } else if (urlValidator.shouldValidate(property)) {
        bMatched = true;
        nbErrors += urlValidator.validate(property, reportItems);

      } else if (plainTextValidator.shouldValidate(property)) {
        bMatched = true;
        nbErrors += plainTextValidator.validate(property, reportItems);

      } else {
        for (int i = 0; i < patternValidators.length; i++) {
          if (patternValidators[i].shouldValidate(property)) {
            bMatched = true;
            nbErrors += patternValidators[i].validate(property, reportItems);
            break;
          }
        }
      }

      if (!bMatched) {
        // Nothing matched, apply defaultValidator
        nbErrors += defaultValidator.validate(property, reportItems);
      }
    }

    return nbErrors;
  }

  /**
   * Nothing to validate if message is empty.
   * 
   */
  public boolean shouldValidate(Property property) {
    return !matches(property.getKey());
  }

  public void setHtmlValidator(L10nValidator<Property> htmlValidator) {
    this.htmlValidator = htmlValidator;
  }

  public void setJsValidator(L10nValidator<Property> jsValidator) {
    this.jsValidator = jsValidator;
  }

  public void setUrlValidator(L10nValidator<Property> urlValidator) {
    this.urlValidator = urlValidator;
  }

  public void setPlainTextValidator(L10nValidator<Property> plainTextValidator) {
    this.plainTextValidator = plainTextValidator;
  }

  public void setPatternValidators(L10nValidator<Property>[] patternValidators) {
    this.patternValidators = patternValidators;
  }

  public void setDefaultValidator(L10nValidator<Property> defaultValidator) {
    this.defaultValidator = defaultValidator;
  }

  public void setParametricMessageValidator(L10nValidator<Property> parametricMessageValidator) {
    this.parametricMessageValidator = parametricMessageValidator;
  }

  public void setTrailingWhitespaceValidator(L10nValidator<Property> trailingWhitespaceValidator) {
    this.trailingWhitespaceValidator = trailingWhitespaceValidator;
  }

}
