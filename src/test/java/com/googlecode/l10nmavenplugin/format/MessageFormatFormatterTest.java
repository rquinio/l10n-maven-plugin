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
}
