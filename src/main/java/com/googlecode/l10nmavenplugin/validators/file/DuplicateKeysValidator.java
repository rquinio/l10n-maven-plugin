package com.googlecode.l10nmavenplugin.validators.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyImpl;
import com.googlecode.l10nmavenplugin.utils.PropertiesLoader;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Loads a file with an overridden version of {@link java.util.Properties} that detects duplicate keys.
 * 
 */
public class DuplicateKeysValidator extends AbstractL10nValidator implements L10nValidator<File> {

  private final PropertiesLoader propertiesLoader;

  private final File rootDir;

  public DuplicateKeysValidator(L10nValidatorLogger logger, File rootDir) {
    super(logger);

    this.rootDir = rootDir;
    this.propertiesLoader = new PropertiesLoader(logger);
  }

  public int validate(File file, List<L10nReportItem> reportItems) throws L10nValidationException {
    PropertiesFile propertiesFile = new BundlePropertiesFile(propertiesLoader.getRelativeFileName(file, rootDir), null);
    DuplicateKeysAwareProperties propertiesToFill = new DuplicateKeysAwareProperties(logger, propertiesFile);

    // Validation happens during loading
    this.propertiesLoader.loadPropertiesFile(file, rootDir, propertiesToFill);

    reportItems.addAll(propertiesToFill.getReportItems());
    return propertiesToFill.getNbErrors();
  }

  public boolean shouldValidate(File file) {
    // TODO Could check file extension ?
    return true;
  }

  private class DuplicateKeysAwareProperties extends Properties {

    private final L10nValidatorLogger logger;

    private final List<L10nReportItem> reportItems = new ArrayList<L10nReportItem>();

    private final PropertiesFile propertiesFile;

    private int nbErrors = 0;

    public DuplicateKeysAwareProperties(L10nValidatorLogger logger, PropertiesFile propertiesFile) {
      this.logger = logger;
      this.propertiesFile = propertiesFile;
    }

    @Override
    public synchronized Object put(Object key, Object value) {

      if (containsKey(key)) {
        String previousValue = (String) get(key);
        Property property = new PropertyImpl((String) key, (String) value, propertiesFile);
        L10nReportItem reportItem = new L10nReportItem(Type.DUPLICATE_KEY, "Duplicate key for existing value ["
            + previousValue + "]", property, null);
        reportItems.add(reportItem);

        logger.log(reportItem);
        nbErrors++;
      }

      // Do not alter default behavior
      return super.put(key, value);
    }

    public List<L10nReportItem> getReportItems() {
      return reportItems;
    }

    public int getNbErrors() {
      return nbErrors;
    }

  }

}
