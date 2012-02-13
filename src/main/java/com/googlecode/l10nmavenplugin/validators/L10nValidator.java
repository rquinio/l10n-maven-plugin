package com.googlecode.l10nmavenplugin.validators;

import java.util.List;
import java.util.Set;

/**
 * Interface for all l10n validation modules
 * 
 * @author romain.quinio
 */
public interface L10nValidator {
  
  /**
   * Validates a property
   * @param key key of the property
   * @param message value to validate, potentially different from the original property value in case of chaining validators
   * @param propertiesName identification of the Properties file, usually file name.
   * @return number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems);
  
  /**
   * Report on validation results
   * @param propertiesNames
   * @return
   */
  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems);
}
