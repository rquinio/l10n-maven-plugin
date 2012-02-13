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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Root bundle should be excluded, as it usually contains non language dependent resources.
 * @author romain.quinio
 *
 */
public class MissingTranslationValidator implements L10nValidator {

  private L10nValidatorLogger logger;
  
  private Map<String,Set<String>> translatedResources = new HashMap<String,Set<String>>();
  
  public MissingTranslationValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }
  
  /**
   * Keep track of translated resources
   */
  public int validate(String key, String message, String propertiesName, List<L10nReportItem> reportItems) {
    if(message.length() > 0 && propertiesName.contains("_")){ //Ignore root bundle
      Set<String> propertiesNames = translatedResources.get(key);
      if(propertiesNames == null){
        propertiesNames = new HashSet<String>();
        translatedResources.put(key, propertiesNames);
      }
      propertiesNames.add(propertiesName);
    }
    return 0;
  }
  
  public int report(final Set<String> propertiesNames, List<L10nReportItem> reportItems){
    int nbErrors = 0;
    
    for(Map.Entry<String, Set<String>> entry : translatedResources.entrySet()){
      Set<String> translatedPropertyNames = entry.getValue();
      
      //Ignore resource existing only in 1 language (reference language)
      int nbTranslations = translatedPropertyNames.size();
      if(nbTranslations > 1 && nbTranslations < propertiesNames.size()){
        Set<String> missingPropertyNames = new HashSet<String>();
        for(String propertyName : propertiesNames){
          if(!translatedPropertyNames.contains(propertyName)){
            missingPropertyNames.add(propertyName);
          }
        }
        L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.MISSING_TRANSLATION, 
            "Resource is not translated, although there are translations in "+nbTranslations+" other languages",
            missingPropertyNames.toString(), entry.getKey(), null, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    }
    return nbErrors;
  }
}
