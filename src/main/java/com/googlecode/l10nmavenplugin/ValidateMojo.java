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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.family.HtmlTagCoherenceValidator;
import com.googlecode.l10nmavenplugin.validators.family.IdenticalTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.family.MissingTranslationValidator;
import com.googlecode.l10nmavenplugin.validators.family.ParametricCoherenceValidator;
import com.googlecode.l10nmavenplugin.validators.orchestrator.DirectoryValidator;
import com.googlecode.l10nmavenplugin.validators.orchestrator.PropertiesFamilyValidator;
import com.googlecode.l10nmavenplugin.validators.orchestrator.PropertyFamilyValidator;
import com.googlecode.l10nmavenplugin.validators.orchestrator.PropertyValidator;
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
 * @since 1.0
 * @author romain.quinio
 * 
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.TEST)
public class ValidateMojo extends AbstractMojo implements L10nValidationConfiguration {

  /**
   * Directory containing properties file to check
   * 
   * @since 1.0
   */
  @Parameter(defaultValue = "src/main/resources")
  private File propertyDir;

  /**
   * Keys excluded from validation. Default is none.
   * 
   * @since 1.0
   */
  @Parameter
  private String[] excludedKeys = new String[] {};

  /**
   * Make validation failure not blocking the build
   * 
   * @since 1.0
   */
  @Parameter(defaultValue = "false")
  private boolean ignoreFailure = false;

  /**
   * List of keys to match as text resources used from js. Default is ".js.".
   * 
   * @since 1.0
   */
  @Parameter
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
   * @since 1.3
   */
  @Parameter(defaultValue = "true")
  private boolean jsDoubleQuoted = true;

  /**
   * List of keys to match as url resources. Default is ".url.".
   * 
   * @since 1.0
   */
  @Parameter
  private String[] urlKeys = new String[] { ".url." };

  /**
   * List of keys to match as html text resources. Default is ".text.".
   * 
   * @since 1.0
   */
  @Parameter
  private String[] htmlKeys = new String[] { ".text." };

  /**
   * XML Schema to use for html resource validation. Default value is to use XHTML1 transitional.
   * 
   * @since 1.3
   */
  @Parameter(defaultValue = "xhtml1-transitional.xsd")
  private File xhtmlSchema;

  /**
   * List of keys to match as non-html text resources. Default is ".title.".
   * 
   * @since 1.1
   */
  @Parameter
  private String[] textKeys = new String[] { ".title." };

  /**
   * Custom validation patterns.
   * 
   * @since 1.3
   */
  @Parameter
  private CustomPattern[] customPatterns = new CustomPattern[] {};

  /**
   * Directory containing dictionaries for SpellCheck validation.
   * 
   * Defaults to the value of parameter propertyDir.
   * 
   * @since 1.4
   */
  @Parameter
  private File dictionaryDir;

  /**
   * Flag allowing to skip plugin exceution for a particular build.
   * 
   * This makes the plugin more controllable from profiles.
   * 
   * @since 1.4
   */
  @Parameter(defaultValue = "${l10n.skip}")
  private boolean skip;

  /**
   * Base directory where all reports are written to.
   * 
   * @since 1.5
   */
  @Parameter(defaultValue = "${project.build.directory}/l10n-reports")
  private File reportsDir;

  private L10nValidator<File> directoryValidator;

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
    setReportsDir(configuration.getReportsDir());
  }

  /**
   * Initialize validators from plugin configuration
   * 
   * TODO Should use a DI container to do the wirering
   * 
   */
  protected void initialize() {
    logger = new L10nValidatorLogger(getLog());
    getLog().info("Initializing l10n validators...");

    if (dictionaryDir == null) {
      // Default to propertyDir
      dictionaryDir = propertyDir;
    }
    L10nValidator<Property> spellCheckValidator = new SpellCheckValidator(logger, dictionaryDir);

    L10nValidator<Property> htmlValidator;
    if (xhtmlSchema != null) {
      htmlValidator = new HtmlValidator(xhtmlSchema, logger, spellCheckValidator, htmlKeys);
    } else {
      htmlValidator = new HtmlValidator(logger, spellCheckValidator, htmlKeys);
    }

    L10nValidator<Property> jsValidator = new JsValidator(jsDoubleQuoted, htmlValidator, logger, jsKeys);
    L10nValidator<Property> urlValidator = new UrlValidator(logger, urlKeys);
    L10nValidator<Property> plainTextValidator = new PlainTextValidator(logger, spellCheckValidator, textKeys);
    L10nValidator<Property> defaultValidator = new DefaultValidator(logger, htmlKeys, urlKeys);
    L10nValidator<Property> parametricMessageValidator = new ParametricMessageValidator(logger);
    L10nValidator<Property> trailingWhitespaceValidator = new TrailingWhitespaceValidator(logger);
    L10nValidator<PropertyFamily> missingTranslationValidator = new MissingTranslationValidator(logger);
    L10nValidator<PropertyFamily> parametricCoherenceValidator = new ParametricCoherenceValidator(logger);
    L10nValidator<PropertyFamily> identicalTranslationValidator = new IdenticalTranslationValidator(logger);
    L10nValidator<PropertyFamily> htmlTagCoherenceValidator = new HtmlTagCoherenceValidator(logger, htmlKeys);

    // L10nValidator<PropertiesFamily> duplicationValidator = new DuplicationValidator(logger);

    L10nValidator<Property>[] patternValidators = null;
    if (customPatterns != null) { // Initialize custom pattern validators
      patternValidators = new PatternValidator[customPatterns.length];
      for (int i = 0; i < customPatterns.length; i++) {
        CustomPattern pattern = customPatterns[i];
        patternValidators[i] = new PatternValidator(logger, pattern);
      }
    }

    PropertyValidator propertyValidator = new PropertyValidator(logger, excludedKeys);
    propertyValidator.setPatternValidators(patternValidators);
    propertyValidator.setDefaultValidator(defaultValidator);
    propertyValidator.setHtmlValidator(htmlValidator);
    propertyValidator.setJsValidator(jsValidator);
    propertyValidator.setParametricMessageValidator(parametricMessageValidator);
    propertyValidator.setPlainTextValidator(plainTextValidator);
    propertyValidator.setTrailingWhitespaceValidator(trailingWhitespaceValidator);
    propertyValidator.setUrlValidator(urlValidator);

    PropertyFamilyValidator propertyFamilyValidator = new PropertyFamilyValidator(logger, excludedKeys);
    propertyFamilyValidator.setHtmlTagCoherenceValidator(htmlTagCoherenceValidator);
    propertyFamilyValidator.setIdenticalTranslationValidator(identicalTranslationValidator);
    propertyFamilyValidator.setMissingTranslationValidator(missingTranslationValidator);
    propertyFamilyValidator.setParametricCoherenceValidator(parametricCoherenceValidator);
    propertyFamilyValidator.setPropertyValidator(propertyValidator);

    PropertiesFamilyValidator propertiesFamilyValidator = new PropertiesFamilyValidator(logger, reportsDir, propertyFamilyValidator);
    directoryValidator = new DirectoryValidator(logger, propertiesFamilyValidator);
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

    int nbErrors = validate(propertyDir, reportItems);

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
  protected int validate(File directory, List<L10nReportItem> reportItems) throws MojoExecutionException {
    int nbErrors = 0;

    try {
      nbErrors = directoryValidator.validate(directory, reportItems);

    } catch (L10nValidationException e) {
      throw new MojoExecutionException("An unexpected exception has occurred while validating properties under directory " + propertyDir.getAbsolutePath(), e);
    }
    return nbErrors;
  }

  public void setDirectoryValidator(L10nValidator<File> directoryValidator) {
    this.directoryValidator = directoryValidator;
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

  public File getReportsDir() {
    return reportsDir;
  }

  public void setReportsDir(File reportsDir) {
    this.reportsDir = reportsDir;
  }
}
