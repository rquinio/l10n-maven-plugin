package com.googlecode.l10nmavenplugin.format;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * {@link java.text.MessageFormat} wrapper.
 */
public class MessageFormatFormatter implements Formatter {

  /**
   * Detection of parameters ex: {0}, {0,date}, {0,number,integer}
   * 
   * Note: conversion type is not captured.
   */
  private static final String CAPTURE_PARAMETERS_REGEXP = "(?:\\{([0-9]+)(?:,[a-z]+){0,2}\\})";

  private static final String DETECT_PARAMETERS_REGEXP = "^.*" + CAPTURE_PARAMETERS_REGEXP + ".*$";

  protected static final Pattern CAPTURE_PARAMETERS_PATTERN = Pattern.compile(CAPTURE_PARAMETERS_REGEXP);

  /**
   * Use DOTALL flag so that . includes newline characters
   */
  protected static final Pattern DETECT_PARAMETERS_PATTERN = Pattern.compile(DETECT_PARAMETERS_REGEXP, Pattern.DOTALL);

  /**
   * Number of formatting parameters replaced in resources
   */
  private static final int NB_MAX_FORMAT_PARAM = 19;

  /**
   * Values to replace parameters {i} in properties.
   * 
   * Use Integers as they work with all parameter definitions {i,date} {i,number} etc.
   */
  private static final Object[] PARAMETRIC_REPLACE_VALUES = new Integer[NB_MAX_FORMAT_PARAM];

  static {
    for (int i = 0; i < NB_MAX_FORMAT_PARAM; i++) {
      PARAMETRIC_REPLACE_VALUES[i] = i;
    }
  }

  public boolean isParametric(String message) {
    Matcher m = DETECT_PARAMETERS_PATTERN.matcher(message);
    return m.matches();
  }

  public List<Integer> captureParameters(String message) {
    Matcher m = CAPTURE_PARAMETERS_PATTERN.matcher(message);

    List<Integer> parameters = new ArrayList<Integer>();
    while (m.find()) {
      String param = m.group(1);
      if (!StringUtils.isEmpty(param)) {
        parameters.add(Integer.valueOf(param));
      }
    }
    // Parameters may not appear in the same order depending of language
    Collections.sort(parameters);

    return parameters;
  }

  public String defaultFormat(String message) {
    String formattedMessage = format(message, PARAMETRIC_REPLACE_VALUES);

    // In any case replace '' by ' (resource without parameters but called with fmt:param)
    formattedMessage = formattedMessage.replaceAll("''", "'");
    return formattedMessage;
  }

  public String format(String message, Object... args) {
    return MessageFormat.format(message, args);
  }
}
