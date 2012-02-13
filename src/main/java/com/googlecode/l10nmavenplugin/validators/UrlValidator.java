package com.googlecode.l10nmavenplugin.validators;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Performs validation of absolute URLs
 * 
 * @author romain.quinio
 *
 */
public class UrlValidator implements L10nValidator {

  /**
   * Validation of an absolute URL Protocol must be included in URL (http(s), mailto, or protocol relative)
   */
  private static final String URL_VALIDATION_REGEXP = "^((http[s]?:)?//[-a-zA-Z0-9_.:]+[-a-zA-Z0-9_:@&?=+,.!/~*'%$#]*)|(mailto:).*$";

  protected static final Pattern URL_VALIDATION_PATTERN = Pattern.compile(URL_VALIDATION_REGEXP);
  
  private L10nValidatorLogger logger;
  
  public UrlValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }
  
  /**
   * Validate URLs using regexp.
   * 
   * Performs a MessageFormat if needed.
   * 
   * @note the URLValidator from Apache does not seem to support scheme relative URLs.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String formattedMessage = message;
    
    try{
      if(ParametricMessageValidator.isParametric(key, formattedMessage, propertiesName)){
        formattedMessage = ParametricMessageValidator.defaultFormat(message);
      }
      // Unescape HTML in case URL is used in HTML context (ex: &amp; -> &)
      String url = StringEscapeUtils.unescapeHtml(formattedMessage);
      Matcher m = URL_VALIDATION_PATTERN.matcher(url);
  
      if (!m.matches()) {
        nbErrors++;
        L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.URL_VALIDATION, 
            "Invalid absolute URL.", propertiesName, key, message, formattedMessage);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    } catch(IllegalArgumentException e){
      //Catch MessageFormat errors in case of malformed message
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.MALFORMED_PARAMETER, 
          "URL contains malformed parameters: " + e.getMessage(), propertiesName, key, message, formattedMessage);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    return 0;
  }

}
