/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.orchestrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.maven.plugin.MojoExecutionException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFamily;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Orchestrates the load and validation of Properties from a directory
 * 
 * All Properties files are assumed to be part of the same bundle
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class DirectoryValidator extends AbstractL10nValidator implements L10nValidator<File> {

  private final L10nValidator<PropertiesFamily> propertiesFamilyValidator;

  public DirectoryValidator(L10nValidatorLogger logger, L10nValidator<PropertiesFamily> propertiesFamilyValidator) {
    super(logger);
    this.propertiesFamilyValidator = propertiesFamilyValidator;
  }

  /**
   * 
   * 
   * @param directory
   *          the folder containing .properties files to validate
   * @param reportItems
   *          list to update with validation errors/warn/info items
   * @return number of validation errors
   * @throws MojoExecutionException
   */
  public int validate(File directory, List<L10nReportItem> reportItems) {
    int nbError = 0;

    // TODO split multiple bundles in same directory ?
    PropertiesFamily propertiesFamily = loadPropertiesFamily(directory);
    if (propertiesFamily != null && propertiesFamily.getNbPropertiesFiles() > 0) {
      nbError = propertiesFamilyValidator.validate(propertiesFamily, reportItems);
    }

    if (reportItems.size() > 0) {
      logSummary(reportItems);
    }

    return nbError;
  }

  public boolean shouldValidate(File toValidate) {
    // Always validate
    return true;
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
  protected PropertiesFamily loadPropertiesFamily(File directory) {
    logger.getLogger().info("Looking for .properties files in: " + directory.getAbsolutePath());
    List<PropertiesFile> propertiesFiles = new ArrayList<PropertiesFile>();

    File[] files = directory.listFiles((FilenameFilter) new SuffixFileFilter(".properties"));
    if (files == null || files.length == 0) {
      logger.getLogger().warn("No properties file under folder " + directory.getAbsolutePath() + ". Skipping l10n validation.");

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
  protected PropertiesFile loadPropertiesFile(File file) {
    PropertiesFile propertiesFile = null;

    String fileName = file.getName();
    logger.getLogger().debug("Loading " + fileName + "...");

    try {
      InputStream inStream = new FileInputStream(file);
      Properties properties = new Properties();
      try {
        properties.load(inStream);
        propertiesFile = new BundlePropertiesFile(fileName, properties);
      } catch (IllegalArgumentException e) {
        // Add file details to the exception
        throw new IllegalArgumentException("The file <" + fileName + "> could not be loaded. Check for a malformed Unicode escape sequence.", e);

      } finally {
        inStream.close();
      }
    } catch (IOException e) {
      throw new L10nValidationException("An unexpected exception has occured while loading properties.", e);
    }
    return propertiesFile;
  }

  /**
   * Log a summary of the validation
   * 
   * @param reportItems
   */
  protected void logSummary(List<L10nReportItem> reportItems) {
    logger.getLogger().info("--------------------");
    logger.getLogger().info("Validation summary: " + reportItems.size() + " issues.");

    Map<Type, List<L10nReportItem>> byType = L10nReportItem.byType(reportItems);

    for (Entry<Type, List<L10nReportItem>> entry : byType.entrySet()) {
      Type type = entry.getKey();
      int nbType = entry.getValue().size();
      Severity severity = entry.getValue().iterator().next().getItemSeverity();

      logger.log(severity, type + ": " + nbType);
    }
    logger.getLogger().info("--------------------\n");

  }

}
