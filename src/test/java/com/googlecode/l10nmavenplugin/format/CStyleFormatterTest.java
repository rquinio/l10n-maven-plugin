package com.googlecode.l10nmavenplugin.format;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CStyleFormatterTest {

  private Formatter formatter;

  @Before
  public void setUp() {
    formatter = new CStyleFormatter();
  }

  @Test
  public void testIsParametric() {
    assertFalse(formatter.isParametric("Some text"));
    assertFalse(formatter.isParametric("10% of 20$"));
    assertFalse(formatter.isParametric("% $"));
    assertFalse(formatter.isParametric("%1 2$"));

    assertTrue(formatter.isParametric("Some text: %1$s %2$s"));
    assertTrue(formatter.isParametric("Some text: %1$2s"));
    assertTrue(formatter.isParametric("Some date: %1$t"));
    assertTrue(formatter.isParametric("Some date: %1$+d"));
  }

  @Test
  public void testIsParametricWithNewline() {
    assertTrue(formatter.isParametric("Some text: %1$s \n with newline"));
  }

  @Test
  public void testParametricCaptureWithExplicitIndexing() {
    assertArrayEquals(new Integer[] { 1, 2 }, formatter.captureParameters("Some text: %1$s %2$s").toArray(new Integer[] {}));

    assertArrayEquals(new Integer[] { 1, 1, 2 }, formatter.captureParameters("Some text: %1$s %2$s %1$s").toArray(new Integer[] {}));
  }

  @Test
  @Ignore("Not supported")
  public void testParametricCaptureWithOrdinaryIndexing() {
    assertArrayEquals(new Integer[] { 1, 2 }, formatter.captureParameters("Some text: %2s %+d").toArray(new Integer[] {}));

    assertArrayEquals(new Integer[] { 1, 2, 3 }, formatter.captureParameters("Some text: %s %s %s").toArray(new Integer[] {}));
  }

  @Test
  @Ignore("Not supported")
  public void testParametricCaptureWithRelatibeIndexing() {
    assertArrayEquals(new Integer[] { 2, 2, 1, 1 }, formatter.captureParameters("Some text: %2$s %<s %1s %<s").toArray(new Integer[] {}));
  }

  @Test
  @Ignore("Not supported")
  public void testParametricCaptureWithMixedIndexing() {
    assertArrayEquals(new Integer[] { 2, 1, 1, 2 }, formatter.captureParameters("Some text: %2$s %s %<s %s").toArray(new Integer[] {}));
  }

  @Test
  public void defaultFormatShouldSupportAllTypes() {
    // No exceptions
    formatter.defaultFormat("%1$s %2$d %3$c");
  }

  @Test
  @Ignore("Not supported")
  public void defaultFormatShouldSupportAllTypes2() {
    formatter.defaultFormat("%4$f %5$tHH");
  }

}
