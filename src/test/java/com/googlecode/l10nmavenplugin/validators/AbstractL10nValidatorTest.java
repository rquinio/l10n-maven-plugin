/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFamily;
import com.googlecode.l10nmavenplugin.model.BundlePropertiesFile;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;

/**
 * Abstract class for {@link L10nValidator} unit tests, to share common initializations and pre-defined constants.
 * 
 * @author romain.quinio
 * 
 * @param <T>
 *          the type to validate
 */
public abstract class AbstractL10nValidatorTest<T> {

  protected static final String BUNDLE = "Junit.properties";

  protected static final String KEY_OK = "key.ok";
  protected static final String KEY_KO = "key.ko";

  protected static final PropertiesFile FILE = new BundlePropertiesFile(BUNDLE, null);

  protected List<L10nReportItem> items;

  protected L10nValidator<T> validator;

  protected L10nValidatorLogger logger;

  protected Log log;

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
    log = spy(new SystemStreamLog());
    logger = new L10nValidatorLogger(log);

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

  public class ItemTypeMatcher extends BaseMatcher<L10nReportItem> {

    private final Type expectedType;

    public ItemTypeMatcher(Type expectedType) {
      this.expectedType = expectedType;
    }

    public boolean matches(Object obj) {
      return expectedType.equals(((L10nReportItem) obj).getItemType());
    }

    public void describeTo(Description description) {
      description.appendText("an L10nReportItem with type ").appendText(expectedType.name());
    }

  }

  /**
   * Used to inject a "neutral" nested validator for tests
   * 
   */
  protected static class AlwaysSucceedingValidator<T> implements L10nValidator<T> {

    public AlwaysSucceedingValidator() {
    }

    public int validate(T toValidate, List<L10nReportItem> reportItems) throws L10nValidationException {
      return 0;
    }

    public boolean shouldValidate(T toValidate) {
      return true;
    }
  }

  /**
   * Used to test propagation of errors, when nesting validators
   * 
   */
  protected static class AlwaysFailingValidator<T> implements L10nValidator<T> {

    public AlwaysFailingValidator() {
    }

    public int validate(T toValidate, List<L10nReportItem> reportItems) throws L10nValidationException {
      return 1;
    }

    public boolean shouldValidate(T toValidate) {
      return true;
    }

  }

  /**
   * Used to test handling of ignored properties
   * 
   */
  protected static class AlwaysRefusingValidator<T> implements L10nValidator<T> {

    public AlwaysRefusingValidator() {
    }

    public int validate(T toValidate, List<L10nReportItem> reportItems) throws L10nValidationException {
      throw new L10nValidationException("Should not have called validate");
    }

    public boolean shouldValidate(T toValidate) {
      return false;
    }

  }
}
