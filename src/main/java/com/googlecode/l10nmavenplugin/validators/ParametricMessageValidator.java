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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Performs generic validation of properties containing parameters ({0},{1},...)
 * 
 * @author romain.quinio
 * 
 */
public class ParametricMessageValidator implements L10nValidator {

  /**
   * Detection of parameters in properties, ex: {0}, {0,date}, {0,number,integer}
   */
  private static final String CAPTURE_PARAMETERS_REGEXP = "(?:\\{([0-9]+)(?:,[a-z]+){0,2}\\})";
  private static final String DETECT_PARAMETERS_REGEXP = "^.*"+CAPTURE_PARAMETERS_REGEXP+".*$";

  /**
   * Values to replace parameters {i} in properties.
   * 
   * Use Integers as they work with all parameter definitions {i,date} {i,number} etc.
   */
  private static final Object[] PARAMETRIC_REPLACE_VALUES = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

  /**
   * Validation of single quotes escaping, consumed by MessageFormat.
   * 
   * "^([^']|'')*$" runs into StackOverflow for long messages.
   */
  private static final String UNESCAPED_QUOTE_REGEX = "[^']*'[^']*";
  
  protected static final Pattern UNESCAPED_QUOTE_PATTERN = Pattern.compile(UNESCAPED_QUOTE_REGEX);
  protected static final Pattern CAPTURE_PARAMETERS_PATTERN = Pattern.compile(CAPTURE_PARAMETERS_REGEXP);
  protected static final Pattern DETECT_PARAMETERS_PATTERN = Pattern.compile(DETECT_PARAMETERS_REGEXP);

  private Map<String, Map<String, List<Integer>>> resourceParameters = new HashMap<String, Map<String, List<Integer>>>();

  private L10nValidatorLogger logger;

  public ParametricMessageValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Validate single quotes are escaped and collect info on number of parameters.
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    boolean isParametric = this.captureParameters(key, message, propertiesName);
    if (isParametric) {
      Matcher unescapedQuotesMatcher = UNESCAPED_QUOTE_PATTERN.matcher(message);
      if (unescapedQuotesMatcher.matches()) {
        nbErrors++;
        L10nReportItem reportItem = new L10nReportItem(Severity.ERROR, Type.UNESCAPED_QUOTE_WITH_PARAMETERS, 
            "MessageFormat requires that ' be escaped with ''.", propertiesName, key, message, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    } else if(message.contains("''")){
      L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.ESCAPED_QUOTE_WITHOUT_PARAMETER, 
          "Resource contains escaped quote '' but no parameters. This may be correct if formatter is called with unused parameters.", 
          propertiesName, key, message, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }
    return nbErrors;
  }

  /**
   * Detect incoherences between properties sharing the same key.
   * 
   * @return
   */
  public int report(Set<String> propertiesNames, List<L10nReportItem> reportItems) {
    int nbErrors = 0;

    for (Map.Entry<String, Map<String, List<Integer>>> keyEntry : resourceParameters.entrySet()) {
      String key = keyEntry.getKey();
      Map<String, List<Integer>> propertiesMap = keyEntry.getValue();
      if (propertiesMap.size() >= 2) { // Ignore messages only present in a single properties (i.e global / not translated)

        Map<List<Integer>,Set<String>> reverseMap = buildReverseMap(propertiesMap);
        if(reverseMap.size() > 1){
          List<Integer> majorityKey = getMajorityKey(reverseMap);
          String majorityKeyMessage = displayParameters(majorityKey);
          Set<String> majorityPropertiesNames = reverseMap.get(majorityKey);
          for(List<Integer> entry : reverseMap.keySet()){
            if(!entry.equals(majorityKey)){
              Set<String> faultyPropertiesNames = reverseMap.get(entry);
              //Only warn for now, need more feedback before moving to error.
              //nbErrors++;
              L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.INCOHERENT_PARAMETERS, 
                  "Incoherent usage of parameters: " + displayParameters(entry) + " versus " + majorityKeyMessage + 
                  " in <" + majorityPropertiesNames + ">", faultyPropertiesNames.toString(), key, null, null);
              reportItems.add(reportItem);
              logger.log(reportItem);
            }
          }
        }
      }
    }
    return nbErrors;
  }
  
  private Map<List<Integer>,Set<String>> buildReverseMap(Map<String, List<Integer>> propertiesMap){
    Map<List<Integer>,Set<String>> reverseMap = new HashMap<List<Integer>,Set<String>>();
    for (Map.Entry<String, List<Integer>> propertiesNameEntry : propertiesMap.entrySet()) {
      String propertiesName = propertiesNameEntry.getKey();
      List<Integer> parameters = propertiesNameEntry.getValue();
      
      Set<String> matchedPropertiesName = reverseMap.get(parameters);
      if(matchedPropertiesName == null){
        matchedPropertiesName = new HashSet<String>();
        reverseMap.put(parameters, matchedPropertiesName);
      }
      matchedPropertiesName.add(propertiesName);
    }
    return reverseMap;
  }
  
  private List<Integer> getMajorityKey(Map<List<Integer>,Set<String>> reverseMap){
    List<Integer> majorityKey = null;
    Set<String> majority = null;
    for(Map.Entry<List<Integer>, Set<String>> entry : reverseMap.entrySet()){
      if(majority == null || entry.getValue().size() > majority.size()){
        majority = entry.getValue();
        majorityKey = entry.getKey();
      }
    }
    return majorityKey;
  }

  public static boolean isParametric(String key, String message, String propertiesName) {
    Matcher m = DETECT_PARAMETERS_PATTERN.matcher(message);
    return m.matches();
  }

  public static String defaultFormat(String message) {
    String formattedMessage = MessageFormat.format(message, PARAMETRIC_REPLACE_VALUES);
    return formattedMessage;
  }

  public boolean captureParameters(String key, String message, String propertiesName) {
    Matcher m = CAPTURE_PARAMETERS_PATTERN.matcher(message);
    boolean isParametric = false;
    List<Integer> storedParams = this.getParameters(key, propertiesName);
    if (storedParams == null) {
      List<Integer> parameters = new ArrayList<Integer>();
      // Save info for further analysis
      while(m.find()){
        String param = m.group(1);
        if (!param.isEmpty()) {
          parameters.add(Integer.valueOf(param));
        }
      }
      // Parameters may not appear in the same order depending of language
      Collections.sort(parameters);
      this.setParameters(key, propertiesName, parameters);
      isParametric = (parameters.size() != 0);
    } else {
      isParametric = (storedParams.size() != 0);
    }
    return isParametric;
  }

  private List<Integer> getParameters(String key, String propertiesName) {
    Map<String, List<Integer>> propertiesNameMap = resourceParameters.get(key);
    if (propertiesNameMap == null) {
      return null;
    } else {
      return propertiesNameMap.get(propertiesName);
    }
  }

  private void setParameters(String key, String propertiesName, List<Integer> parameters) {
    Map<String, List<Integer>> propertiesNameMap = resourceParameters.get(key);
    if (propertiesNameMap == null) {
      propertiesNameMap = new HashMap<String, List<Integer>>();
      resourceParameters.put(key, propertiesNameMap);
    }
    propertiesNameMap.put(propertiesName, parameters);
  }

  private String displayParameters(List<Integer> parameters) {
    StringBuffer sb = new StringBuffer("[");
    for (int i=0; i<parameters.size(); i++) {
      if (i != 0) {
        sb.append(",");
      }
      sb.append("{").append(parameters.get(i)).append("}");
    }
    sb.append("]");
    return sb.toString();
  }
}
