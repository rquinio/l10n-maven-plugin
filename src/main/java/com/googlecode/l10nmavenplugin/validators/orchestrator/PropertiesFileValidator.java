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
import java.util.Set;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class PropertiesFileValidator extends AbstractL10nValidator implements L10nValidator<PropertiesFile> {

  private final L10nValidator<Property> propertyValidator;

  public PropertiesFileValidator(L10nValidatorLogger logger, L10nValidator<Property> propertyValidator) {
    super(logger);
    this.propertyValidator = propertyValidator;
  }

  /**
   * Validate a Properties file in isolation from the other Properties file.
   * 
   * @param properties
   *          Properties to validate
   * @param propertiesName
   *          the name of the .properties file, for error logging
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   */
  public int validate(PropertiesFile propertiesFile, List<L10nReportItem> reportItems) {
    logger.info(propertiesFile.getFileName(), null, "Starting validation (locale: " + propertiesFile.getLocale() + ")...", null, null);
    int nbErrors = 0;

    Set<Object> keys = propertiesFile.getProperties().keySet();
    for (Object obj : keys) {
      String key = (String) obj;
      String message = propertiesFile.getProperties().getProperty(key);

      nbErrors += propertyValidator.validate(new PropertyImpl(key, message, propertiesFile), reportItems);
    }
    return nbErrors;
  }

  public boolean shouldValidate(PropertiesFile propertiesFile) {
    // Always validate
    return true;
  }
}
