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
