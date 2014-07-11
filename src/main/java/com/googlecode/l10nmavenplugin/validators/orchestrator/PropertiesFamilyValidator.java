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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.MojoExecutionException;

import au.com.bytecode.opencsv.CSVWriter;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Orchestrates validation of properties files belonging to the same bundle
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class PropertiesFamilyValidator extends AbstractL10nValidator implements L10nValidator<PropertiesFamily> {

  private final L10nValidator<PropertyFamily> propertyFamilyValidator;

  private final File reportsDir;

  public PropertiesFamilyValidator(L10nValidatorLogger logger, File reportsDir, L10nValidator<PropertyFamily> propertyFamilyValidator) {
    super(logger);
    this.reportsDir = reportsDir;
    this.propertyFamilyValidator = propertyFamilyValidator;
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
  public int validate(PropertiesFamily propertiesFamily, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    // nbErrors += duplicationValidator.validate(propertiesFamily, reportItems);

    for (Iterator<PropertyFamily> it = propertiesFamily.iterator(); it.hasNext();) {
      PropertyFamily propertyFamily = it.next();
      if (propertyFamilyValidator.shouldValidate(propertyFamily)) {
        nbErrors += propertyFamilyValidator.validate(propertyFamily, reportItems);
      } else {
        // Property is excluded from validation
        L10nReportItem item = new L10nReportItem(Type.EXCLUDED, "Property was excluded from validation by plugin configuration.", propertyFamily
            .getExistingPropertyFiles().toString(), propertyFamily.getKey(), null, null);
        reportItems.add(item);
        logger.log(item);
      }
    }

    if (reportItems.size() > 0) {
      logBundleValidationSummary(reportItems, propertiesFamily.getBaseName());
      generateCsv(reportItems, propertiesFamily.getBaseName());
    }

    return nbErrors;
  }

  /**
   * Generate csv file (bundleBaseName.csv).
   * 
   * Expects reportsDir top be a valid directory
   * 
   */
  protected void generateCsv(List<L10nReportItem> reportItems, String bundleBaseName) {
    Collections.sort(reportItems);
    generateCsv(reportItems, bundleBaseName, Severity.ERROR);
    generateCsv(reportItems, bundleBaseName, Severity.WARN);
    generateCsv(reportItems, bundleBaseName, Severity.INFO);
  }

  protected File generateCsv(List<L10nReportItem> reportItems, String bundleBaseName, Severity severity) {
    File csvFile = null;

    // Might be null in case of report goal
    if (reportsDir != null) {
      // Create directory path
      reportsDir.mkdirs();

      csvFile = new File(reportsDir, bundleBaseName + "-" + severity.toString() + ".csv");

      List<String> entries = new ArrayList<String>();
      for (L10nReportItem reportItem : reportItems) {
        if (severity.equals(reportItem.getItemSeverity())) {
          entries.add(reportItem.getPropertiesKey());
        }
      }
      if (entries.size() > 0) {
        logger.getLogger().info("Generating csv " + csvFile.getAbsolutePath() + " with keys associated to severity " + severity.toString());
        writeCsv(entries, csvFile);
      } else {
        logger.getLogger().debug("Skipping generation of csv for severity " + severity.toString());
      }
    }

    return csvFile;
  }

  private void writeCsv(List<String> entries, File csvFile) {
    try {
      CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
      writer.writeNext(entries.toArray(new String[entries.size()]));
      writer.close();

    } catch (IOException e) {
      throw new L10nValidationException("Could not write csv file", e);
    }
  }

  public boolean shouldValidate(PropertiesFamily propertiesFamily) {
    // Always validate
    return true;
  }

  /**
   * Log a summary of the validation of a given bundle
   * 
   * @param reportItems
   */
  protected void logBundleValidationSummary(List<L10nReportItem> reportItems, String bundleName) {
    logger.getLogger().info("--------------------");
    logger.getLogger().info("Bundle <" + bundleName + "> validation summary: " + reportItems.size() + " issues.");

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
