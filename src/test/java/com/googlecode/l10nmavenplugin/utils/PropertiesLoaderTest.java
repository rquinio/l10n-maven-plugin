package com.googlecode.l10nmavenplugin.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class PropertiesLoaderTest extends AbstractL10nValidatorTest<File> {

  private PropertiesLoader propertiesLoader;

  private Properties properties;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    propertiesLoader = new PropertiesLoader(logger);
    properties = new Properties();
  }

  @Test(expected = IllegalArgumentException.class)
  public void malfomedPropertiesShouldFailExecution() throws MojoExecutionException {
    File file = getFile("malformed/malformed.properties");

    propertiesLoader.loadPropertiesFile(file, getFile("malformed"), new Properties());
  }

  @Test
  public void simpleBundle() {
    File file = getFile("bundle/Bundle.properties");

    PropertiesFile propertiesFile = propertiesLoader.loadPropertiesFile(file, getFile("bundle"), properties);

    assertEquals("Bundle", propertiesFile.getBundleName());
    assertEquals("Bundle.properties", propertiesFile.getFileName());
  }

  @Test
  public void bundleNameShouldBePrefixedWithPath() {
    File file = getFile("recursive/sub-folder/bundle.properties");

    PropertiesFile propertiesFile = propertiesLoader.loadPropertiesFile(file, getFile("recursive"), properties);

    assertEquals("sub-folder.bundle", propertiesFile.getBundleName());
    assertEquals("sub-folder" + File.separator + "bundle.properties", propertiesFile.getFileName());
  }
}
