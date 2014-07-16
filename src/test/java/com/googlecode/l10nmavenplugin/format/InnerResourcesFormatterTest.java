package com.googlecode.l10nmavenplugin.format;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.PropertiesFile;

public class InnerResourcesFormatterTest {

  private static final String SQUARED_BRACKETS_REGEX = "\\[\\[(([A-Za-z0-9\\._])+)\\]\\]";

  private InnerResourcesFormatter innerResourcesFormatter = new InnerResourcesFormatter(SQUARED_BRACKETS_REGEX);

  private Properties properties = new Properties();

  private PropertiesFile file = new PropertiesFile() {

    public Properties getProperties() {
      return properties;
    }

    public Locale getLocale() {
      return null;
    }

    public String getFileName() {
      return null;
    }

    public Set<String> getDuplicatedResourceKeys() {
      return null;
    }

    public String getBundleName() {
      return null;
    }
  };

  @Test
  public void testGetInnerResourceRegex() {
    assertEquals(SQUARED_BRACKETS_REGEX, innerResourcesFormatter.getInnerResourceRegex());
  }

  @Test
  public void testFormatSquaredBracketsValid() {
    properties.setProperty("some.res", "some.value");

    assertEquals("an inner some.value with stuff behind", innerResourcesFormatter.format(
        "an inner [[some.res]] with stuff behind", file));
  }

  @Test
  public void testFormatSquaredBracketsExceptionNonExistingKey() {
    boolean illegalArgumentExceptionRaised = false;
    try {
      innerResourcesFormatter.format("an inner [[non.existing.res]] with stuff behind", file);
    }
    catch (IllegalArgumentException e) {
      illegalArgumentExceptionRaised = true;
    }
    assertTrue("An empty inner key did not raise an illegalArgumentException", illegalArgumentExceptionRaised);
  }

  @Test
  public void testDefaultFormat() {
    String message = innerResourcesFormatter.defaultFormat("Res with [[multiple]] refs to [[external.res]]");
    assertFalse("default format should remove inner references to keys",
        innerResourcesFormatter.hasInnerResources(message));
  }

  @Test
  public void testCaptureInnerKeys() {
    assertEquals(Collections.emptyList(), innerResourcesFormatter.captureInnerResources(null));
    assertEquals(Collections.emptyList(), innerResourcesFormatter.captureInnerResources(" some text"));
    assertEquals(Collections.emptyList(), innerResourcesFormatter.captureInnerResources(" [[some.res text"));
    assertEquals(Arrays.asList("some.res", "another.res"),
        innerResourcesFormatter.captureInnerResources(" [[some.res]] text [[another.res]]"));
  }

  @Test
  public void testHasInnerRes() {
    String valueWithoutInnerKeys = "some value withouth innerKeys";
    assertFalse("hasInnerResources returned true even if <" + valueWithoutInnerKeys + "> does not contains any",
        innerResourcesFormatter.hasInnerResources(valueWithoutInnerKeys));
    String valueWithInnerKeys = "some value with [[inner.key]]";
    assertTrue("hasInnerResources returned false even if <" + valueWithInnerKeys + "> has one inner key",
        innerResourcesFormatter.hasInnerResources(valueWithInnerKeys));
  }

}
