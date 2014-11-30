package com.googlecode.l10nmavenplugin.format;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MessageFormatFormatterTest {

  private Formatter formatter;

  @Before
  public void setUp() {
    formatter = new MessageFormatFormatter();
  }

  @Test
  public void testIsParametric() {
    assertFalse(formatter.isParametric("Some text"));
    assertFalse(formatter.isParametric("Some quoted text: '{bla}' "));

    assertTrue(formatter.isParametric("Some text: {0} {1}"));
    assertTrue(formatter.isParametric("Some date: {0,date}"));
    assertTrue(formatter.isParametric("Some date: {0,number,integer}"));
  }

  @Test
  public void testIsParametricWithNewline() {
    assertTrue(formatter.isParametric("Some text: {0} \n with newline"));
  }

  @Test
  @Ignore("Case not supported yet")
  public void testIsParametricWithDatePattern() {
    assertTrue(formatter.isParametric("{0,date,MM/dd/yyyy HH':'mm}"));
  }

  @Test
  @Ignore("Case not supported yet")
  public void singleQuoteShouldBeAnEscapeSequence() {
    assertFalse(formatter.isParametric("'{0}'"));
  }

  @Test
  public void testParametricReplacePatternCapture() {
    Matcher m = MessageFormatFormatter.DETECT_PARAMETERS_PATTERN.matcher("Some {0} parametrized text {1,date}");
    assertTrue(m.matches());
    // Only last is saved
    assertEquals(1, m.groupCount());
    assertEquals("1", m.group(1));
  }

  @Test
  public void testParametricCapture() {
    assertArrayEquals(new Integer[] { 0, 1 }, formatter.captureParameters("Some text: {0} {1}").toArray(new Integer[] {}));

    assertArrayEquals(new Integer[] { 0, 0, 1 }, formatter.captureParameters("Some text: {0} {1} {0}").toArray(new Integer[] {}));

    assertArrayEquals(new Integer[] { 0, 1 }, formatter.captureParameters("Some text: {0,date} {1,number,integer}").toArray(new Integer[] {}));
  }

  @Test
  public void defaultFormatShouldSupoortAllTypes() {
    // No exceptions
    formatter.defaultFormat("{0} {1,date} {2,number,integer} {3,number,$'#',##} {4,time} {5,choice,0#value1|1#value2}");
  }
}
