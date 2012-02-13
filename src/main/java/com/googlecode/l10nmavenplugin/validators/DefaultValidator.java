package com.googlecode.l10nmavenplugin.validators;

import java.util.List;
import java.util.Set;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.*;

/**
 * Default validator, used in case no other specific validator was triggered.
 * 
 * @author romain.quinio
 *
 */
public class DefaultValidator implements L10nValidator {
  
  private L10nValidatorLogger logger;
  
  public DefaultValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Warn if other resources contain HTML/URL.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    if (TextValidator.isHtml(message)) {
      L10nReportItem reportItem = new L10nReportItem(Severity.WARN, 
          Type.UNDECLARED_HTML_RESOURCE, 
          "Resource may contain HTML, but is not declared as such. No validation was performed.",
          propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);

    } else if (TextValidator.isUrl(message)) {
      L10nReportItem reportItem = new L10nReportItem(Severity.WARN, 
          Type.UNDECLARED_URL_RESOURCE, 
          "Resource may contain URL, but is not declared as such. No validation was performed.",
          propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItem) {
    return 0;
  }
}
