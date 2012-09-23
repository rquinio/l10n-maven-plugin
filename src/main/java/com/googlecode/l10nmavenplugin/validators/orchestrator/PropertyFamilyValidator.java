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
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.PropertiesKeyConventionValidator;

/**
 * Orchestrates validation of a multi-language property
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class PropertyFamilyValidator extends PropertiesKeyConventionValidator implements L10nValidator<PropertyFamily> {

  private L10nValidator<PropertyFamily> parametricCoherenceValidator;

  private L10nValidator<PropertyFamily> missingTranslationValidator;

  private L10nValidator<PropertyFamily> identicalTranslationValidator;

  private L10nValidator<PropertyFamily> htmlTagCoherenceValidator;

  private L10nValidator<Property> propertyValidator;

  public PropertyFamilyValidator(L10nValidatorLogger logger, String[] excludedKeys) {
    super(logger, excludedKeys);
  }

  /**
   * Validate translations of a property. There are 2 steps:
   * <ul>
   * <li>Validate property in isolation, based on the context the property will be used (xHTML, URL, js, ...)</li>
   * <li>Validate the coherence of translation of the property</li>
   * </ul>
   * 
   * @param properties
   *          Properties to validate
   * @param propertiesName
   *          the name of the .properties file, for error logging
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   */
  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String key = propertyFamily.getKey();

    for (PropertiesFile propertiesFile : propertyFamily.getExistingPropertyFiles()) {
      Property property = new PropertyImpl(key, propertiesFile.getProperties().getProperty(key), propertiesFile);
      if (propertyValidator.shouldValidate(property)) {
        nbErrors += propertyValidator.validate(property, reportItems);
      } else {
        // Property is excluded from validation
        L10nReportItem item = new L10nReportItem(Type.EXCLUDED, "Property was excluded from validation by plugin configuration.", propertyFamily
            .getExistingPropertyFiles().toString(), propertyFamily.getKey(), null, null);
        reportItems.add(item);
        logger.log(item);
      }
    }

    // Apply validation with PropertyFamily scope
    nbErrors += parametricCoherenceValidator.validate(propertyFamily, reportItems);
    nbErrors += missingTranslationValidator.validate(propertyFamily, reportItems);
    nbErrors += identicalTranslationValidator.validate(propertyFamily, reportItems);
    if (htmlTagCoherenceValidator.shouldValidate(propertyFamily)) {
      nbErrors += htmlTagCoherenceValidator.validate(propertyFamily, reportItems);
    }

    return nbErrors;
  }

  /**
   * Validate if key is not ignored for validation
   */
  public boolean shouldValidate(PropertyFamily propertyFamily) {
    return !matches(propertyFamily.getKey());
  }

  public void setParametricCoherenceValidator(L10nValidator<PropertyFamily> parametricCoherenceValidator) {
    this.parametricCoherenceValidator = parametricCoherenceValidator;
  }

  public void setMissingTranslationValidator(L10nValidator<PropertyFamily> missingTranslationValidator) {
    this.missingTranslationValidator = missingTranslationValidator;
  }

  public void setIdenticalTranslationValidator(L10nValidator<PropertyFamily> identicalTranslationValidator) {
    this.identicalTranslationValidator = identicalTranslationValidator;
  }

  public void setHtmlTagCoherenceValidator(L10nValidator<PropertyFamily> htmlTagCoherenceValidator) {
    this.htmlTagCoherenceValidator = htmlTagCoherenceValidator;
  }

  public void setPropertyValidator(L10nValidator<Property> propertyValidator) {
    this.propertyValidator = propertyValidator;
  }
}
