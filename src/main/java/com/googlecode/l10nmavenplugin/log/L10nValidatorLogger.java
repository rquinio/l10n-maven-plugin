package com.googlecode.l10nmavenplugin.log;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import com.googlecode.l10nmavenplugin.validators.L10nReportItem;

/**
 * Basic logger that ensures consistency in logging.
 * 
 * Standard maven options apply: --quiet for ERROR level, -X for DEBUG. 
 * 
 * @author romain.quinio
 *
 */
public class L10nValidatorLogger {
  
  private Log logger; 

  public L10nValidatorLogger() {
    this.logger = new SystemStreamLog();
  }
  
  public L10nValidatorLogger(Log logger) {
    this.logger = logger;
  }
  
  public void debug(String propertiesName, String key, String logMessage, String message, String formattedMessage){
    this.logger.debug(buildLogMessage(propertiesName, key, logMessage, message, formattedMessage));
  }
  
  public void info(String propertiesName, String key, String logMessage, String message, String formattedMessage){
    this.logger.info(buildLogMessage(propertiesName, key, logMessage, message, formattedMessage));
  }
  
  public void warn(String propertiesName, String key, String logMessage, String message, String formattedMessage){
    this.logger.warn(buildLogMessage(propertiesName, key, logMessage, message, formattedMessage));
  }
  
  public void error(String propertiesName, String key, String logMessage, String message, String formattedMessage){
    this.logger.error(buildLogMessage(propertiesName, key, logMessage, message, formattedMessage));
  }
  
  public void log(L10nReportItem reportItem){
    switch(reportItem.getItemSeverity()){
      case INFO:
         this.info(reportItem.getPropertiesName(), reportItem.getPropertiesKey(), reportItem.getItemMessage(), 
             reportItem.getPropertiesValue(), reportItem.getFormattedPropertiesValue());
         break;
      case WARN:
        this.warn(reportItem.getPropertiesName(), reportItem.getPropertiesKey(), reportItem.getItemMessage(), 
            reportItem.getPropertiesValue(), reportItem.getFormattedPropertiesValue());
        break;
      case ERROR:
        this.error(reportItem.getPropertiesName(), reportItem.getPropertiesKey(), reportItem.getItemMessage(), 
            reportItem.getPropertiesValue(), reportItem.getFormattedPropertiesValue());
        break;
    }
  }
  
  /**
   * Log in a consistent way for all validators.
   * 
   * @param logLevel
   * @param propertiesName
   * @param key
   *          optional
   * @param logMessage
   * @param message
   *          optional
   * @param formattedMessage
   *          optional
   */
  private String buildLogMessage(String propertiesName, String key, String logMessage, String message,
      String formattedMessage) {
    StringBuffer sb = new StringBuffer();
    sb.append("<").append(propertiesName).append(">");
    if (key != null) {
      sb.append("<").append(key).append(">");
    }
    sb.append(" ").append(logMessage).append("\n");
    if (message != null) {
      sb.append("Property value was:[").append(message).append("]").append("\n");
    }
    if (formattedMessage != null) {
      sb.append("Formatted value used for validation:[").append(formattedMessage).append("]").append("\n");
    }
    return sb.toString();
  }

  public Log getLogger() {
    return logger;
  }

  public void setLogger(Log logger) {
    this.logger = logger;
  }
}
