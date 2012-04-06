/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import au.com.bytecode.opencsv.CSVWriter;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.DefaultValidator;
import com.googlecode.l10nmavenplugin.validators.HtmlValidator;
import com.googlecode.l10nmavenplugin.validators.JsValidator;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.MissingTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.ParametricMessageValidator;
import com.googlecode.l10nmavenplugin.validators.PatternValidator;
import com.googlecode.l10nmavenplugin.validators.TextValidator;
import com.googlecode.l10nmavenplugin.validators.UrlValidator;

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
 *       <li>JSON {@link http://json.org/}</li>
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
public class ValidateMojo extends AbstractMojo implements L10nValidationConfiguration {

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
   * Declares how the client side resources are loaded in javascript:
   * <ul>
   * <li>double quoted: var jsResource = "<fmt:message key='jsKey' />"</li>
   * <li>single quoted: var jsResource = '<fmt:message key="jsKey" />'</li>
   * <ul>
   * 
   * Default value is true (double quoted), which complies with JSON format.
   * 
   * @parameter default-value="true"
   * @since 1.3
   */
  private boolean jsDoubleQuoted = true;

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
   * XML Schema to use for html resource validation. Default value is to use XHTML1 transitional.
   * 
   * @parameter default-value="xhtml1-transitional.xsd"
   * @since 1.3
   */
  private File xhtmlSchema;

  /**
   * List of keys to match as non-html text resources. Default is ".title.".
   * 
   * @parameter
   * @since 1.1
   */
  private String[] textKeys = new String[] { ".title." };

  /**
   * Custom validation patterns.
   * 
   * @parameter
   * @since 1.3
   */
  private CustomPattern[] customPatterns = new CustomPattern[] {};

  private L10nValidator htmlValidator;
  private L10nValidator jsValidator;
  private L10nValidator urlValidator;
  private L10nValidator textValidator;
  private L10nValidator defaultValidator;
  private L10nValidator parametricMessageValidator;
  private L10nValidator missingTranslationValidator;
  private L10nValidator[] patternValidators = new L10nValidator[] {};

  private L10nValidatorLogger logger;

  /**
   * Default constructor for plugin execution
   * 
   * Can't initialize validators here, because Mojo configuration is injected after constructor has returned
   * 
   */
  public ValidateMojo() {
  }

  /**
   * Initialize from another configured Mojo
   * 
   */
  public ValidateMojo(L10nValidationConfiguration configuration) {
    // Propagate configuration
    setPropertyDir(configuration.getPropertyDir());
    setHtmlKeys(configuration.getHtmlKeys());
    setXhtmlSchema(configuration.getXhtmlSchema());
    setJsKeys(configuration.getJsKeys());
    setJsDoubleQuoted(configuration.getJsDoubleQuoted());
    setTextKeys(configuration.getTextKeys());
    setUrlKeys(configuration.getUrlKeys());
    setCustomPatterns(configuration.getCustomPatterns());
    setExcludedKeys(configuration.getExcludedKeys());
  }

  /**
   * Initialize validators from plugin configuration
   * 
   */
  protected void initialize() {
    logger = new L10nValidatorLogger(getLog());
    getLog().info("Initializing l10n validators...");

    if (xhtmlSchema != null) {
      htmlValidator = new HtmlValidator(xhtmlSchema, logger);
    } else {
      htmlValidator = new HtmlValidator(logger);
    }
    jsValidator = new JsValidator(jsDoubleQuoted, htmlValidator, logger);

    urlValidator = new UrlValidator(logger);
    textValidator = new TextValidator(logger);
    defaultValidator = new DefaultValidator(logger);
    parametricMessageValidator = new ParametricMessageValidator(logger);
    missingTranslationValidator = new MissingTranslationValidator(logger);

    if (customPatterns != null) {
      // Initialize custom pattern validators
      patternValidators = new L10nValidator[customPatterns.length];
      for (int i = 0; i < customPatterns.length; i++) {
        CustomPattern pattern = customPatterns[i];
        patternValidators[i] = new PatternValidator(logger, pattern.getName(), pattern.getRegex());
      }
    }
  }

  /**
   * Plugin entry point for validate goal.
   * 
   * @throws MojoExecutionException
   *           in case of unexpected exception during plugin execution
   * @throws MojoFailureException
   *           in case validation detected errors and ignoreFailure is false
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    initialize();
    executeInternal();
  }

  /**
   * Plugin entry point for unit testing to allow re-use of a single initialized Mojo instance, for perf reasons.
   * 
   * @throws MojoExecutionException
   *           in case of unexpected exception during plugin execution
   * @throws MojoFailureException
   *           in case validation detected errors and ignoreFailure is false
   */
  protected void executeInternal() throws MojoExecutionException, MojoFailureException {
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
   * Validation logic entry point used both by {@link ValidateMojo} and {@link ReportMojo}
   * 
   * @param directory
   *          the folder containing .properties files to validate
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   * @throws MojoExecutionException
   */
  public int validateProperties(File directory, List<L10nReportItem> reportItems) throws MojoExecutionException {
    Map<String, Properties> propertiesMap = loadProperties(directory);
    if (propertiesMap.size() > 0) {
      return validateProperties(propertiesMap, reportItems);
    } else {
      return 0;
    }
  }

  /**
   * Load Properties file
   * 
   * @param directory
   *          the folder containing .properties files to load
   * @return map of <file name, file content as Properties>, never null
   * @throws MojoExecutionException
   */
  private Map<String, Properties> loadProperties(File directory) throws MojoExecutionException {
    Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
    getLog().info("Looking for .properties files in: " + directory.getAbsolutePath());

    try {
      File[] files = directory.listFiles((FilenameFilter) new SuffixFileFilter(".properties"));
      if (files == null || files.length == 0) {
        getLog().warn("No properties file under folder " + directory.getAbsolutePath() + ". Skipping l10n validation.");

      } else {
        for (File file : files) {
          InputStream inStream = new FileInputStream(file);
          String propertiesName = file.getName();
          Properties properties = new Properties();
          getLog().debug("Loading " + propertiesName + "...");
          try {
            properties.load(inStream);
          } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("The file <" + propertiesName
                + "> could not be loaded. Check for a malformed Unicode escape sequence.", e);
          }
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
   *          Properties to validate, indexed by bundle name
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   * @throws MojoExecutionException
   */
  protected int validateProperties(Map<String, Properties> propertiesMap, List<L10nReportItem> reportItems)
      throws MojoExecutionException {
    int nbErrors = 0;

    // TODO need to split bundles and reset validators that are statefull

    // 1st step: validation in isolation
    for (Map.Entry<String, Properties> entry : propertiesMap.entrySet()) {
      nbErrors += validateProperties(entry.getValue(), entry.getKey(), reportItems);
    }

    // 2nd step: comparison
    nbErrors += parametricMessageValidator.report(propertiesMap.keySet(), reportItems);
    nbErrors += missingTranslationValidator.report(excludeRootBundle(propertiesMap.keySet()), reportItems);

    // Generate csv file
    /*
     * String bundleBaseName = getBundleBaseName(propertiesMap.keySet().iterator().next()); try { generateCsv(reportItems,
     * bundleBaseName); } catch (IOException e) { throw new
     * MojoExecutionException("IOException while generating csv of keys with warning/error", e); }
     */

    return nbErrors;
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
  protected int validateProperty(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    if (message.length() > 0) { // Nothing to validate if message is empty.
      // Only validate if key is not excluded
      if (StringUtils.indexOfAny(key, excludedKeys) == -1) {
        logger.debug(propertiesName, key, "Starting validation...", null, null);

        nbErrors += missingTranslationValidator.validate(key, message, propertiesName, reportItems);
        nbErrors += parametricMessageValidator.validate(key, message, propertiesName, reportItems);

        boolean bMatched = false;
        if (StringUtils.indexOfAny(key, htmlKeys) != -1) {
          bMatched = true;
          nbErrors += htmlValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, jsKeys) != -1) {
          bMatched = true;
          nbErrors += jsValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, urlKeys) != -1) {
          bMatched = true;
          nbErrors += urlValidator.validate(key, message, propertiesName, reportItems);

        } else if (StringUtils.indexOfAny(key, textKeys) != -1) {
          bMatched = true;
          nbErrors += textValidator.validate(key, message, propertiesName, reportItems);

        } else {
          for (int i = 0; i < customPatterns.length; i++) {
            CustomPattern pattern = customPatterns[i];
            if (StringUtils.indexOfAny(key, pattern.getKeys()) != -1) {
              bMatched = true;
              nbErrors += patternValidators[i].validate(key, message, propertiesName, reportItems);
              break;
            }
          }
        }

        if (!bMatched) { // Nothing matched, apply defaultValidator
          nbErrors += defaultValidator.validate(key, message, propertiesName, reportItems);
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

  /**
   * 
   * @param reportItems
   * @param bundleBaseName
   *          Base name of the bundle, optionally null
   * @throws IOException
   */
  private void generateCsv(List<L10nReportItem> reportItems, String bundleBaseName) throws IOException {
    File csvFile = new File(bundleBaseName + ".csv");
    CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
    getLog().info("Generating " + csvFile.getAbsolutePath() + " with keys having error/warning");

    List<String> entries = new ArrayList<String>();
    for (L10nReportItem reportItem : reportItems) {
      if (!Severity.INFO.equals(reportItem.getItemSeverity())) {
        entries.add(reportItem.getPropertiesKey());
      }
    }
    writer.writeNext(entries.toArray(new String[] {}));
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
   * Exclude the root bundle from a set of properties from the same bundle
   * 
   * @param propertiesNames
   * @return
   */
  private Set<String> excludeRootBundle(Set<String> propertiesNames) {
    String rootBundle = getRootBundle(propertiesNames);

    Set<String> localizedPropertiesNames = new HashSet<String>();
    for (String propertiesName : propertiesNames) {
      if (!propertiesName.equals(rootBundle)) {
        localizedPropertiesNames.add(propertiesName);
      }
    }
    return localizedPropertiesNames;
  }

  /**
   * Get the root bundle
   * 
   * @param propertiesNames
   * @return rootBundle, or null if not found
   */
  private String getRootBundle(Set<String> propertiesNames) {
    String rootBundle = null;
    for (String propertiesName : propertiesNames) {
      if (!propertiesName.contains("_")) {
        rootBundle = propertiesName;
        break;
      }
    }
    return rootBundle;
  }

  /**
   * Get the base name of bundle, based on default {@link java.util.ResourceBundle} convention
   * baseName[_language[_country[_variant]]].properties
   * 
   * @param propertiesName
   *          file name of 1 properties of the bundle
   * @return baseName of the bundle
   */
  protected String getBundleBaseName(String propertiesName) {
    int index = propertiesName.indexOf("_");
    if (index != -1) {
      return propertiesName.substring(0, index);
    } else {
      return FilenameUtils.getBaseName(propertiesName);
    }
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

  public void setCustomPatterns(CustomPattern[] customPatterns) {
    this.customPatterns = customPatterns;
  }

  public void setJsDoubleQuoted(boolean jsDoubleQuoted) {
    this.jsDoubleQuoted = jsDoubleQuoted;
  }

  public void setXhtmlSchema(File xhtmlSchema) {
    this.xhtmlSchema = xhtmlSchema;
  }

  public File getPropertyDir() {
    return propertyDir;
  }

  public String[] getExcludedKeys() {
    return excludedKeys;
  }

  public boolean getIgnoreFailure() {
    return ignoreFailure;
  }

  public String[] getJsKeys() {
    return jsKeys;
  }

  public boolean getJsDoubleQuoted() {
    return jsDoubleQuoted;
  }

  public String[] getUrlKeys() {
    return urlKeys;
  }

  public String[] getHtmlKeys() {
    return htmlKeys;
  }

  public File getXhtmlSchema() {
    return xhtmlSchema;
  }

  public String[] getTextKeys() {
    return textKeys;
  }

  public CustomPattern[] getCustomPatterns() {
    return customPatterns;
  }
}
