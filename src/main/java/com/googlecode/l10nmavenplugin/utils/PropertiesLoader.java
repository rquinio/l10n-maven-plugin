package com.googlecode.l10nmavenplugin.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;

/**
 * Utility service for loading .properties file into a {@link java.util.Properties} instance
 */
public class PropertiesLoader {

  private final L10nValidatorLogger logger;

  public PropertiesLoader(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Load a single Properties file
   */
  public PropertiesFile loadPropertiesFile(File file, File rootDir, Properties propertiesToFill) {
    PropertiesFile propertiesFile = null;

    logger.getLogger().debug("Loading " + file.getPath() + "...");

    try {
      InputStream inStream = new FileInputStream(file);
      try {
        propertiesToFill.load(inStream);
        propertiesFile = new BundlePropertiesFile(getRelativeFileName(file, rootDir), propertiesToFill);
      } catch (IllegalArgumentException e) {
        // Add file details to the exception
        throw new IllegalArgumentException("The file <" + file.getPath()
            + "> could not be loaded. Check for a malformed Unicode escape sequence.", e);

      } finally {
        inStream.close();
      }
    } catch (IOException e) {
      throw new L10nValidationException("An unexpected exception has occured while loading properties.", e);
    }
    return propertiesFile;
  }

  /**
   * Get the relative fileName of file inside rootDir, without prefix.
   */
  public String getRelativeFileName(File file, File rootDir) {
    return file.getPath().replace(rootDir.getPath() + File.separator, "");
  }
}
