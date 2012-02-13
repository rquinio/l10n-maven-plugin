package com.googlecode.l10nmavenplugin.validators;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Performs validation of plain text properties (non HTML, non URL)
 * @author romain.quinio
 *
 */
public class TextValidator implements L10nValidator {

  /**
   * Basic detection of HTML tags
   */
  private static final String HTML_REGEXP = "^.*\\<[^>]+>.*$";

  /**
   * Basic detection of URL
   */
  private static final String URL_REGEXP = "^.*(?://|mailto).*$";

  protected static final Pattern HTML_PATTERN = Pattern.compile(HTML_REGEXP);
  protected static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

  private L10nValidatorLogger logger;

  public TextValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Check resource does not contain HTML/URL
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    if (isHtml(message)) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.TEXT_VALIDATION_NO_HTML, 
          "Text resource must not contain HTML.", propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);

    } else if (isUrl(message)) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.TEXT_VALIDATION_NO_URL, 
          "Text resource must not contain URL.", propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }
  
  public static boolean isHtml(String message){
    Matcher htmlMatcher = HTML_PATTERN.matcher(message);
    return htmlMatcher.matches();
  }
  
  public static boolean isUrl(String message){
    Matcher urlMatcher= URL_PATTERN.matcher(message);
    return urlMatcher.matches();
  }

  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    return 0;
  } 
}