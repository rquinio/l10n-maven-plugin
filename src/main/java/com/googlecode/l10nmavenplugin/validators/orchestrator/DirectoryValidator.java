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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.MojoExecutionException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFamily;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.utils.PropertiesLoader;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Orchestrates the load and validation of Properties from a directory, handling the case of multiple bundles.
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class DirectoryValidator extends AbstractL10nValidator implements L10nValidator<File> {

  private final L10nValidator<PropertiesFamily> propertiesFamilyValidator;

  private final L10nValidator<File> duplicateKeysValidator;

  private final PropertiesLoader propertiesLoader;

  public DirectoryValidator(L10nValidatorLogger logger, L10nValidator<PropertiesFamily> propertiesFamilyValidator,
      L10nValidator<File> duplicateKeysValidator) {
    super(logger);
    this.propertiesFamilyValidator = propertiesFamilyValidator;
    this.duplicateKeysValidator = duplicateKeysValidator;

    this.propertiesLoader = new PropertiesLoader(logger);
  }

  /**
   * Validate .properties files in a directory, grouped by bundle (aka PropertiesFamily).
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
    List<PropertiesFamily> propertiesFamilies = new ArrayList<PropertiesFamily>();
    nbError += loadPropertiesFamily(directory, reportItems, propertiesFamilies);
    for (PropertiesFamily propertiesFamily : propertiesFamilies) {
      if (propertiesFamily != null && propertiesFamily.getNbPropertiesFiles() > 0) {
        nbError += propertiesFamilyValidator.validate(propertiesFamily, reportItems);
      }
    }

    return nbError;
  }

  public boolean shouldValidate(File toValidate) {
    // Always validate
    return true;
  }

  /**
   * Load a group of Properties file from a directory
   */
  protected int loadPropertiesFamily(File directory, List<L10nReportItem> reportItems,
      List<PropertiesFamily> propertiesFamilies) {
    int nbErrors = 0;

    logger.getLogger().info("Looking for .properties files in: " + directory.getAbsolutePath());
    List<PropertiesFile> propertiesFilesInDir = new ArrayList<PropertiesFile>();
    Collection<File> files = Collections.emptyList();

    if (directory.exists()) {
      files = FileUtils.listFiles(directory, new SuffixFileFilter(".properties"), TrueFileFilter.INSTANCE);
    }
    if (files == null || files.size() == 0) {
      logger.getLogger().warn(
          "No properties file under folder " + directory.getAbsolutePath() + ". Skipping l10n validation.");

    } else {
      for (File file : files) {
        // Validate File
        nbErrors += duplicateKeysValidator.validate(file, reportItems);

        // Load it normally
        propertiesFilesInDir.add(loadPropertiesFile(file, directory, reportItems));
      }
    }
    propertiesFamilies.addAll(loadPropertiesFamily(propertiesFilesInDir));
    return nbErrors;
  }

  private List<PropertiesFamily> loadPropertiesFamily(List<PropertiesFile> propertiesFilesInDir) {
    List<PropertiesFamily> families = new ArrayList<PropertiesFamily>();
    Collection<List<PropertiesFile>> groupedPropertiesFiles = groupPropertiesFileByBundleName(propertiesFilesInDir);
    for (List<PropertiesFile> bundle : groupedPropertiesFiles) {
      families.add(new BundlePropertiesFamily(bundle));
    }
    return families;
  }

  private Collection<List<PropertiesFile>> groupPropertiesFileByBundleName(List<PropertiesFile> propertiesFiles) {
    Map<String, List<PropertiesFile>> bundleFiles = new HashMap<String, List<PropertiesFile>>();

    for (PropertiesFile propertiesFile : propertiesFiles) {
      String bundleName = propertiesFile.getBundleName();

      List<PropertiesFile> propertiesWithSameBundleName = bundleFiles.get(bundleName);

      if (propertiesWithSameBundleName == null) {
        logger.getLogger().debug(String.format("Detected bundle with name %s", bundleName));
        propertiesWithSameBundleName = new ArrayList<PropertiesFile>();
        bundleFiles.put(bundleName, propertiesWithSameBundleName);
      }

      propertiesWithSameBundleName.add(propertiesFile);
    }
    return bundleFiles.values();
  }

  /**
   * Load a single Properties file
   */
  protected PropertiesFile loadPropertiesFile(File file, File rootDir, List<L10nReportItem> reportItems) {
    Properties propertiesToFill = new Properties();
    return propertiesLoader.loadPropertiesFile(file, rootDir, propertiesToFill);
  }
}
