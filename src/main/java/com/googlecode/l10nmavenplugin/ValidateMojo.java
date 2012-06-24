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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import au.com.bytecode.opencsv.CSVWriter;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFamily;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.DuplicationValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.HtmlTagCoherenceValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.IdenticalTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.MissingTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.ParametricCoherenceValidator;
import com.googlecode.l10nmavenplugin.validators.property.DefaultValidator;
import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;
import com.googlecode.l10nmavenplugin.validators.property.JsValidator;
import com.googlecode.l10nmavenplugin.validators.property.ParametricMessageValidator;
import com.googlecode.l10nmavenplugin.validators.property.PatternValidator;
import com.googlecode.l10nmavenplugin.validators.property.PlainTextValidator;
import com.googlecode.l10nmavenplugin.validators.property.SpellCheckValidator;
import com.googlecode.l10nmavenplugin.validators.property.TrailingWhitespaceValidator;
import com.googlecode.l10nmavenplugin.validators.property.UrlValidator;

/**
 * Validate a set of l10n {@link Properties} files against:
 * 
 * <ul>
 * <li>Missing javascript escaping for resources evaluated client side</li>
 * <li>Bad escaping for {@link java.text.MessageFormat} in case of parametric replacement</li>
 * <li>Invalid XHTML 1.0 transitional</li>
 * <li>Malformed absolute URLs</li>
 * <li>Plain text resources containing HTML/URL</li>
 * </ul>
 * 
 * In case multiple checks are performed on a resource (ex: client side resource with parameters), the order above applies.
 * 
 * The syntax of properties file itself is not checked, but it relies on loading them successfully as {@link Properties}.
 * 
 * 
 * @note References for escape sequences and special characters:
 *       <ul>
 *       <li>Java Properties: {@link Properties#load}</li>
 *       <li>Java MessageFormat: {@link java.text.MessageFormat}</li>
 *       <li>Javascript: {@link http://www.w3schools.com/js/js_special_characters.asp}</li>
 *       <li>JSON {@link http://json.org/}</li>
 *       <li>XHTML: {@link http://www.w3schools.com/tags/ref_entities.asp}</li>
 *       <li>URL: {@link http://www.w3schools.com/tags/ref_urlencode.asp}</li>
 *       </ul>
 *       Extra references for development:
 *       <ul>
 *       <li>Java Pattern: {@link java.util.regex.Pattern}</li>
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

  /**
   * Directory containing dictionaries for SpellCheck validation.
   * 
   * Defaults to the value of parameter propertyDir.
   * 
   * @parameter
   * @since 1.4
   */
  private File dictionaryDir;

  /**
   * Flag allowing to skip plugin exceution for a particular build.
   * 
   * This makes the plugin more controllable from profiles.
   * 
   * @parameter expression="${l10n.skip}"
   * @since 1.4
   */
  private boolean skip;

  private L10nValidator<Property> htmlValidator;
  private L10nValidator<Property> jsValidator;
  private L10nValidator<Property> urlValidator;
  private L10nValidator<Property> plainTextValidator;
  private L10nValidator<Property> spellCheckValidator;
  private L10nValidator<Property> defaultValidator;
  private L10nValidator<Property> parametricMessageValidator;
  private L10nValidator<Property> trailingWhitespaceValidator;
  private L10nValidator<Property>[] patternValidators = new PatternValidator[] {};

  private L10nValidator<PropertyFamily> missingTranslationValidator;
  private L10nValidator<PropertyFamily> identicalTranslationValidator;
  private L10nValidator<PropertyFamily> parametricCoherenceValidator;
  private L10nValidator<PropertyFamily> htmlTagCoherenceValidator;

  private L10nValidator<PropertiesFamily> duplicationValidator;

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
    setDictionaryDir(configuration.getDictionaryDir());
  }

  /**
   * Initialize validators from plugin configuration
   * 
   */
  protected void initialize() {
    logger = new L10nValidatorLogger(getLog());
    getLog().info("Initializing l10n validators...");

    if (dictionaryDir == null) {
      // Default to propertyDir
      dictionaryDir = propertyDir;
    }
    spellCheckValidator = new SpellCheckValidator(logger, dictionaryDir);

    if (xhtmlSchema != null) {
      htmlValidator = new HtmlValidator(xhtmlSchema, logger, spellCheckValidator);
    } else {
      htmlValidator = new HtmlValidator(logger, spellCheckValidator);
    }
    jsValidator = new JsValidator(jsDoubleQuoted, htmlValidator, logger);

    urlValidator = new UrlValidator(logger);
    plainTextValidator = new PlainTextValidator(logger, spellCheckValidator);
    defaultValidator = new DefaultValidator(logger);
    parametricMessageValidator = new ParametricMessageValidator(logger);
    trailingWhitespaceValidator = new TrailingWhitespaceValidator(logger);
    missingTranslationValidator = new MissingTranslationValidator(logger);
    parametricCoherenceValidator = new ParametricCoherenceValidator(logger);
    identicalTranslationValidator = new IdenticalTranslationValidator(logger);
    duplicationValidator = new DuplicationValidator(logger);
    htmlTagCoherenceValidator = new HtmlTagCoherenceValidator(logger);

    if (customPatterns != null) {
      // Initialize custom pattern validators
      patternValidators = new PatternValidator[customPatterns.length];
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
    if (!skip) {
      initialize();
      executeInternal();
    } else {
      getLog().info("Skipping plugin execution, as per configuration.");
    }
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

    if (reportItems.size() > 0) {
      logSummary(reportItems);
    }

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
   * Log a summary of the validation
   * 
   * @param reportItems
   */
  protected void logSummary(List<L10nReportItem> reportItems) {
    getLog().info("--------------------");
    getLog().info("Validation summary: " + reportItems.size() + " issues.");

    Map<Type, Collection<L10nReportItem>> byType = L10nReportItem.byType(reportItems);

    for (Entry<Type, Collection<L10nReportItem>> entry : byType.entrySet()) {
      Type type = entry.getKey();
      int nbType = entry.getValue().size();
      Severity severity = entry.getValue().iterator().next().getItemSeverity();

      logger.log(severity, type + ": " + nbType);
    }
    getLog().info("--------------------\n");

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
    // TODO split multiple bundles in same directory ?
    PropertiesFamily propertiesFamily = loadPropertiesFamily(directory);
    if (propertiesFamily != null && propertiesFamily.getNbPropertiesFiles() > 0) {
      return validatePropertiesFamily(propertiesFamily, reportItems);
    } else {
      return 0;
    }
  }

  /**
   * Load a group of Properties file
   * 
   * @param directory
   *          the folder containing .properties files to load
   * @param log
   * @return
   * @throws MojoExecutionException
   */
  protected PropertiesFamily loadPropertiesFamily(File directory) throws MojoExecutionException {
    getLog().info("Looking for .properties files in: " + directory.getAbsolutePath());
    List<PropertiesFile> propertiesFiles = new ArrayList<PropertiesFile>();

    File[] files = directory.listFiles((FilenameFilter) new SuffixFileFilter(".properties"));
    if (files == null || files.length == 0) {
      getLog().warn("No properties file under folder " + directory.getAbsolutePath() + ". Skipping l10n validation.");

    } else {
      for (File file : files) {
        propertiesFiles.add(loadPropertiesFile(file));
      }
    }

    return new BundlePropertiesFamily(propertiesFiles);
  }

  /**
   * Load a single Properties file
   * 
   * @param file
   * @param log
   * @return
   * @throws MojoExecutionException
   */
  protected PropertiesFile loadPropertiesFile(File file) throws MojoExecutionException {
    PropertiesFile propertiesFile = null;

    String fileName = file.getName();
    getLog().debug("Loading " + fileName + "...");

    try {
      InputStream inStream = new FileInputStream(file);
      Properties properties = new Properties();
      try {
        properties.load(inStream);
        propertiesFile = new BundlePropertiesFile(fileName, properties);
      } catch (IllegalArgumentException e) {
        // Add file details to the exception
        throw new IllegalArgumentException("The file <" + fileName
            + "> could not be loaded. Check for a malformed Unicode escape sequence.", e);

      } finally {
        inStream.close();
      }
    } catch (IOException e) {
      throw new MojoExecutionException("An unexpected exception has occured while loading properties.", e);
    }
    return propertiesFile;
  }

  /**
   * Validate some Properties file belonging to the same bundle.
   * 
   * @param propertiesFamily
   *          Properties to validate
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   * @throws MojoExecutionException
   */
  protected int validatePropertiesFamily(PropertiesFamily propertiesFamily, List<L10nReportItem> reportItems)
      throws MojoExecutionException {
    int nbErrors = 0;

    // nbErrors += duplicationValidator.validate(propertiesFamily, reportItems);

    for (Iterator<PropertyFamily> it = propertiesFamily.iterator(); it.hasNext();) {
      nbErrors += validatePropertyFamily(it.next(), reportItems);
    }

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
  protected int validatePropertiesFile(PropertiesFile propertiesFile, List<L10nReportItem> reportItems) {
    logger.info(propertiesFile.getFileName(), null, "Starting validation (locale: " + propertiesFile.getLocale() + ")...", null,
        null);
    int nbErrors = 0;

    Set<Object> keys = propertiesFile.getProperties().keySet();
    for (Object obj : keys) {
      String key = (String) obj;
      String message = propertiesFile.getProperties().getProperty(key);
      nbErrors += validateProperty(new PropertyImpl(key, message, propertiesFile), reportItems);
    }
    return nbErrors;
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
  protected int validatePropertyFamily(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String key = propertyFamily.getKey();

    // Only validate if key is not excluded
    if (StringUtils.indexOfAny(key, excludedKeys) == -1) {
      for (PropertiesFile propertiesFile : propertyFamily.getExistingPropertyFiles()) {
        nbErrors += validateProperty(new PropertyImpl(key, propertiesFile.getProperties().getProperty(key), propertiesFile),
            reportItems);
      }

      // Apply validation with PropertyFamily scope
      nbErrors += parametricCoherenceValidator.validate(propertyFamily, reportItems);
      nbErrors += missingTranslationValidator.validate(propertyFamily, reportItems);
      nbErrors += identicalTranslationValidator.validate(propertyFamily, reportItems);
      if (StringUtils.indexOfAny(key, htmlKeys) != -1) {
        nbErrors += htmlTagCoherenceValidator.validate(propertyFamily, reportItems);
      }

    } else {
      // Property is excluded from validation
      L10nReportItem item = new L10nReportItem(Severity.INFO, Type.EXCLUDED,
          "Property was excluded from validation by plugin configuration.", propertyFamily.getExistingPropertyFiles().toString(),
          key, null, null);
      reportItems.add(item);
      logger.log(item);
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
  protected int validateProperty(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String key = property.getKey();

    // Nothing to validate if message is empty.
    if (property.getMessage().length() > 0) {
      // Only validate if key is not excluded
      if (StringUtils.indexOfAny(key, excludedKeys) == -1) {
        logger.debug(property.getPropertiesFile().toString(), key, "Starting validation...", null, null);

        nbErrors += parametricMessageValidator.validate(property, reportItems);
        nbErrors += trailingWhitespaceValidator.validate(property, reportItems);

        boolean bMatched = false;
        if (StringUtils.indexOfAny(key, htmlKeys) != -1) {
          bMatched = true;
          nbErrors += htmlValidator.validate(property, reportItems);

        } else if (StringUtils.indexOfAny(key, jsKeys) != -1) {
          bMatched = true;
          nbErrors += jsValidator.validate(property, reportItems);

        } else if (StringUtils.indexOfAny(key, urlKeys) != -1) {
          bMatched = true;
          nbErrors += urlValidator.validate(property, reportItems);

        } else if (StringUtils.indexOfAny(key, textKeys) != -1) {
          bMatched = true;
          nbErrors += plainTextValidator.validate(property, reportItems);

        } else {
          for (int i = 0; i < customPatterns.length; i++) {
            CustomPattern pattern = customPatterns[i];
            if (StringUtils.indexOfAny(key, pattern.getKeys()) != -1) {
              bMatched = true;
              nbErrors += patternValidators[i].validate(property, reportItems);
              break;
            }
          }
        }

        if (!bMatched) {
          // Nothing matched, apply defaultValidator
          nbErrors += defaultValidator.validate(property, reportItems);
        }

      } else {
        // Property is excluded from validation
        L10nReportItem item = new L10nReportItem(Severity.INFO, Type.EXCLUDED,
            "Property was excluded from validation by plugin configuration.", property.getPropertiesFile().toString(), key, null,
            null);
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
    writer.writeNext(entries.toArray(new String[entries.size()]));
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

  public void setDictionaryDir(File dictionaryDir) {
    this.dictionaryDir = dictionaryDir;
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

  public File getDictionaryDir() {
    return dictionaryDir;
  }

  public void setSkip(boolean skip) {
    this.skip = skip;
  }

  public boolean getSkip() {
    return skip;
  }
}
