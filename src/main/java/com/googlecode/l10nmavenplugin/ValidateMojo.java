/* Copyright (c) 2012 Romain Quinio

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.googlecode.l10nmavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.xml.sax.SAXException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.DefaultValidator;
import com.googlecode.l10nmavenplugin.validators.JsValidator;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.MissingTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.ParametricMessageValidator;
import com.googlecode.l10nmavenplugin.validators.TextValidator;
import com.googlecode.l10nmavenplugin.validators.UrlValidator;
import com.googlecode.l10nmavenplugin.validators.HtmlValidator;

/**
 * Validate a set of l10n {@link Properties} files against:
 * 
 * <ul>
 * <li>Missing javascript escaping for resources evaluated client side</li>
 * <li>Bad escaping for {@link MessageFormat} in case of parametric replacement</li>
 * <li>Invalid XHTML 1.0 transitional</li>
 * <li>Malformed absolute URLs</li>
 * <li>Plain text resources containing HTML/URL</li>
 * </ul>
 * 
 * In case multiple checks are performed on a resource (ex: client side resource with parameters), the order above applies.
 * 
 * The syntax of properties file is not checked but it relies on loading them successfully as Properties.
 * 
 * 
 * @note References for escape sequences and special characters:
 *       <ul>
 *       <li>Java Properties: {@link Properties#load}</li>
 *       <li>Java MessageFormat: {@link MessageFormat}</li>
 *       <li>Javascript: {@link http://www.w3schools.com/js/js_special_characters.asp}</li>
 *       <li>XHTML: {@link http://www.w3schools.com/tags/ref_entities.asp}</li>
 *       <li>URL: {@link http://www.w3schools.com/tags/ref_urlencode.asp}</li>
 *       </ul>
 *       Extra references for development:
 *       <ul>
 *       <li>Java Pattern: {@link Pattern}</li>
 *       <li>Java String: {@link http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#101089}</li>
 *       </ul>
 *       
 * @goal validate
 * @phase test
 * @since 1.0
 * @author romain.quinio
 */
public class ValidateMojo extends AbstractMojo {

  /**
   * Directory containing properties file to check
   * 
   * @parameter default-value="src\\main\\resources"
   * @since 1.0
   */
  private File propertyDir;

  /**
   * Keys excluded from validation. Default is none.
   * 
   * @parameter
   * @since 1.0
   */
  private String[] excludedKeys = new String[] {};

  /**
   * Make validation failure not blocking the build
   * 
   * @parameter default-value="false"
   * @since 1.0
   */
  private boolean ignoreFailure = false;

  /**
   * List of keys to match as text resources used from js. Default is ".js.".
   * 
   * @parameter
   * @since 1.0
   */
  private String[] jsKeys = new String[] { ".js." };

  /**
   * List of keys to match as url resources. Default is ".url.".
   * 
   * @parameter
   * @since 1.0
   */
  private String[] urlKeys = new String[] { ".url." };

  /**
   * List of keys to match as html text resources. Default is ".text.".
   * 
   * @parameter
   * @since 1.0
   */
  private String[] htmlKeys = new String[] { ".text." };

  /**
   * List of keys to match as non-html text resources. Default is ".title.".
   * 
   * @parameter
   * @since 1.1
   */
  private String[] textKeys = new String[] { ".title." };

  private L10nValidator htmlValidator;
  private L10nValidator jsValidator;
  private L10nValidator urlValidator;
  private L10nValidator textValidator;
  private L10nValidator defaultValidator;
  private L10nValidator parametricMessageValidator;
  private L10nValidator missingTranslationValidator;

  private L10nValidatorLogger logger;

  /**
   * Initialize the validator only once in constructor, for performance reason.
   * 
   * @throws URISyntaxException
   * @throws SAXException
   */
  public ValidateMojo() throws URISyntaxException, SAXException {
    logger = new L10nValidatorLogger(getLog());
    
    getLog().info("Initializing l10n validators...");
    htmlValidator = new HtmlValidator(logger);
    jsValidator = new JsValidator(htmlValidator, logger);
    urlValidator = new UrlValidator(logger);
    textValidator = new TextValidator(logger);
    defaultValidator = new DefaultValidator(logger);
    parametricMessageValidator = new ParametricMessageValidator(logger);
    missingTranslationValidator = new MissingTranslationValidator(logger);
  }

  /**
   * Entry point for the plugin validate goal
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    List<L10nReportItem> reportItems = new ArrayList<L10nReportItem>();
    int nbErrors = validateProperties(propertyDir, reportItems);

    if (nbErrors > 0) {
      if (ignoreFailure) {
        getLog().error("Validation has failed with " + nbErrors + " errors.");
        getLog().info("Ignoring failure as ignoreFailure is true.");
      } else {
        throw new MojoFailureException("Validation has failed with " + nbErrors + " errors.");
      }
    } else {
      getLog().info("Validation was successful.");
    }
  }
  
  /**
   * Maven Log is passed after constructor
   */
  @Override
  public void setLog(Log log){
    super.setLog(log);
    logger.setLogger(log);
  }
  
  /**
   * Validation logic entry point used both by {@link ValidateMojo} and {@link ReportMojo}
   * @param directory
   * @param reportItems List to update with errors/warn/info
   * @return number of errors.
   * @throws MojoExecutionException
   */
  public int validateProperties(File directory, List<L10nReportItem> reportItems) throws MojoExecutionException {
    Map<String, Properties> propertiesMap = loadProperties(directory);
    return validateProperties(propertiesMap, reportItems);
  }

  /**
   * Load Properties file
   * 
   * @return never null
   * @throws MojoExecutionException
   */
  private Map<String, Properties> loadProperties(File directory) throws MojoExecutionException {
    Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
    getLog().info("Looking for .properties files in: " + directory.getAbsolutePath());

    try {
      File[] files = directory.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".properties");
        }
      });

      if (files == null || files.length == 0) {
        getLog().warn("No properties file under folder " + directory.getAbsolutePath() + ". Skipping l10n validation.");

      } else {
        for (File file : files) {
          InputStream inStream = new FileInputStream(file);
          String propertiesName = file.getName();
          Properties properties = new Properties();
          properties.load(inStream);
          propertiesMap.put(propertiesName, properties);
        }
      }
    } catch (IOException e) {
      throw new MojoExecutionException("An unexpected exception has occured while loading properties.", e);
    }

    return propertiesMap;
  }

  /**
   * Validate some Properties file belonging to the same bundle. There are 2 steps:
   * <ul>
   * <li>Validate properties in isolation, based on the context the property will be used (xHTML, URL, js, ...)</li>
   * <li>Compare properties between each Properties file.</li>
   * </ul>
   * 
   * @param propertiesMap
   * @return
   */
  protected int validateProperties(Map<String, Properties> propertiesMap, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    // 1st step: validation in isolation
    for (Map.Entry<String, Properties> entry : propertiesMap.entrySet()) {
      nbErrors += validateProperties(entry.getValue(), entry.getKey(),reportItems);
    }

    // 2nd step: comparison
    nbErrors += parametricMessageValidator.report(propertiesMap.keySet(),reportItems);
    nbErrors += missingTranslationValidator.report(excludeRootBundle(propertiesMap.keySet()),reportItems);

    return nbErrors;
  }

  /**
   * Validate a Properties file in isolation from the other Properties file.
   * 
   * @param properties
   * @param propertyName
   *          the name of the file, for error logging
   * @return Number of errors
   */
  protected int validateProperties(Properties properties, String propertiesName, List<L10nReportItem> reportItems) {
    logger.info(propertiesName, null, "Starting validation...", null, null);
    int nbErrors = 0;

    Set<Object> keys = properties.keySet();
    for (Object obj : keys) {
      String key = (String) obj;
      String message = properties.getProperty(key);
      nbErrors += validateProperty(key, message, propertiesName, reportItems);
    }
    return nbErrors;
  }

  /**
   * Validate a single property of a Properties file
   * 
   * @param properties
   * @param propertyName
   *          the name of the file, for error logging
   * @return Number of errors
   */
  protected int validateProperty(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    if (message.length() > 0) { // Nothing to validate if message is empty.
      // Only validate if key is not excluded
      if (StringUtils.indexOfAny(key, excludedKeys) == -1) {
        logger.debug(propertiesName, key, "Starting validation...", null, null);
        
        nbErrors+= missingTranslationValidator.validate(key, message, propertiesName, reportItems);
        nbErrors+= parametricMessageValidator.validate(key, message, propertiesName, reportItems);

        if (StringUtils.indexOfAny(key, htmlKeys) != -1) {
          nbErrors+= htmlValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, jsKeys) != -1) {
          nbErrors+= jsValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, urlKeys) != -1) {
          nbErrors+= urlValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, textKeys) != -1) {
          nbErrors+= textValidator.validate(key, message, propertiesName, reportItems);

        } else {
          nbErrors+= defaultValidator.validate(key, message, propertiesName, reportItems);
        }
      } else {
        L10nReportItem item = new L10nReportItem(Severity.INFO, Type.EXCLUDED, 
            "Property was excluded from validation by plugin configuration.", propertiesName, key, null, null);
        reportItems.add(item);
        logger.log(item);
      }
    }
    return nbErrors;
  }

  public void setPropertyDir(File propertyDir) {
    this.propertyDir = propertyDir;
  }

  public void setExcludedKeys(String[] excludedKeys) {
    this.excludedKeys = excludedKeys;
  }

  public void setIgnoreFailure(boolean ignoreFailure) {
    this.ignoreFailure = ignoreFailure;
  }

  /**
   *
   * @param propertiesNames
   * @return
   */
  private Set<String> excludeRootBundle(Set<String> propertiesNames){
    Set<String> localizedPropertiesNames = new HashSet<String>();
    for(String propertiesName : propertiesNames){
      if(propertiesName.contains("_")){
        localizedPropertiesNames.add(propertiesName);
      }
    }
    return localizedPropertiesNames;
  }

  public void setJsKeys(String[] jsKeys) {
    this.jsKeys = jsKeys;
  }

  public void setUrlKeys(String[] urlKeys) {
    this.urlKeys = urlKeys;
  }

  public void setHtmlKeys(String[] htmlKeys) {
    this.htmlKeys = htmlKeys;
  }

  public void setTextKeys(String[] textKeys) {
    this.textKeys = textKeys;
  }
}