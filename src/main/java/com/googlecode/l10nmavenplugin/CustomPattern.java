/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin;

import java.util.Arrays;

/**
 * Plugin configuration "complex" object for defining a custom validation pattern
 * 
 * @author romain.quinio
 * 
 */
public class CustomPattern {

  /**
   * The name of the pattern, used for reporting validation errors
   * 
   * @parameter
   * @required
   * @since 1.3
   */
  private String name;

  /**
   * Regex to use for validation
   * 
   * @parameter
   * @required
   * @since 1.3
   */
  private String regex;

  /**
   * List of keys to validate
   * 
   * @parameter
   * @required
   * @since 1.3
   */
  private String[] keys;

  /**
   * Default constructor for configuration instantiation
   */
  public CustomPattern() {
  }

  /**
   * Utility constructor for unit testing
   * 
   * @param name
   * @param regex
   * @param keys
   */
  public CustomPattern(String name, String regex, String[] keys) {
    this.name = name;
    this.regex = regex;
    this.keys = keys;
  }
  
  /**
   * Utility constructor for unit testing
   * 
   * @param name
   * @param regex
   * @param key
   */
  public CustomPattern(String name, String regex, String key) {
    this.name = name;
    this.regex = regex;
    this.keys = new String[]{key};
  }

  /**
   * Overridden for configuration debug
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("name: ").append(name).append(", regex: ").append(regex).append(", keys: ").append(Arrays.toString(keys));
    return sb.toString();
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  public void setKeys(String[] keys) {
    this.keys = keys;
  }

  public String getName() {
    return name;
  }

  public String getRegex() {
    return regex;
  }

  public String[] getKeys() {
    return keys;
  }
}
