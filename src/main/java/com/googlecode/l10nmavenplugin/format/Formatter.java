package com.googlecode.l10nmavenplugin.format;

import java.util.List;

/**
 * Wrapper interface around formatter implementations.
 * 
 * Adds the ability to detect and extract formatting parameters from a message, based on the syntax used for parametric replacement.
 * 
 */
public interface Formatter {

  /**
   * Formats a message using args to perform parametric replacement
   */
  String format(String message, Object... args);

  /**
   * Applies a "generic" formatting, to consume the formatting syntax, and allow actual validation of the post-formatted message.
   * 
   * @throws IllegalArgumentException
   *           May throw an IllegalArgumentException if message format syntax is wrong.
   */
  String defaultFormat(String message) throws IllegalArgumentException;

  /**
   * Whether message contains at least 1 parameter
   */
  boolean isParametric(String message);

  /**
   * Unordered list of parameter indexes referenced inside message.
   * 
   * TODO Should also capture conversion type.
   * 
   */
  List<Integer> captureParameters(String message);

  /**
   * How to display an indexed parameter (ex: {0} or %1$), for logging purposes
   */
  String displayIndexedParameter(int index);
}
