package com.googlecode.l10nmavenplugin.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFamily;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;

/**
 * Abstract class for {@link L10nValidator} unit tests.
 * 
 * @author romain.quinio
 * 
 * @param <T>
 *          the type to validate
 */
public abstract class AbstractL10nValidatorTest<T> {

  protected static final String KEY_OK = "key.ok";
  protected static final String KEY_KO = "key.ko";

  protected static final PropertiesFile FILE = new BundlePropertiesFile("junit.properties", null);

  protected List<L10nReportItem> items;

  protected L10nValidator<T> validator;

  protected L10nValidatorLogger logger;

  protected PropertiesFamily propertiesFamily;

  protected Properties root;
  protected Properties bundleA;
  protected Properties bundleB;
  protected Properties bundleC;
  protected Properties bundleD;
  protected Properties bundleE;

  /**
   * Initializes some pre-defined {@link Properties}.
   */
  public void setUp() {
    logger = new L10nValidatorLogger();
    items = new ArrayList<L10nReportItem>();

    Collection<PropertiesFile> propertiesFiles = new ArrayList<PropertiesFile>();

    root = new Properties();
    bundleA = new Properties();
    bundleB = new Properties();
    bundleC = new Properties();
    bundleD = new Properties();
    bundleE = new Properties();

    propertiesFiles.add(new BundlePropertiesFile("Bundle", root));
    propertiesFiles.add(new BundlePropertiesFile("Bundle_A", bundleA));
    propertiesFiles.add(new BundlePropertiesFile("Bundle_B", bundleB));
    propertiesFiles.add(new BundlePropertiesFile("Bundle_C", bundleC));
    propertiesFiles.add(new BundlePropertiesFile("Bundle_D", bundleD));
    propertiesFiles.add(new BundlePropertiesFile("Bundle_E", bundleE));

    propertiesFamily = new BundlePropertiesFamily(propertiesFiles);
  }
}
