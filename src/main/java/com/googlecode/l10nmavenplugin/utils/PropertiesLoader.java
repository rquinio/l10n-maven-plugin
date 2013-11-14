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
  public PropertiesFile loadPropertiesFile(File file, Properties propertiesToFill) {
    PropertiesFile propertiesFile = null;

    String fileName = file.getName();
    logger.getLogger().debug("Loading " + fileName + "...");

    try {
      InputStream inStream = new FileInputStream(file);
      try {
        propertiesToFill.load(inStream);
        propertiesFile = new BundlePropertiesFile(fileName, propertiesToFill);
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

}
