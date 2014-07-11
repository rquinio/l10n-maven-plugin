/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.family;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.googlecode.l10nmavenplugin.format.Formatter;
import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFileUtils;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Validator to check the coherence of parametric properties, i.e. properties containing formatting parameters ({0},{1},...).
 * 
 * Even if the order of the parameters can vary in translations, the number of parameters and the index should usually remain identical, or some info from the
 * original language would be lost.
 * 
 * @since 1.2
 * @author romain.quinio
 * @see {@link com.googlecode.l10nmavenplugin.validators.family.HtmlTagCoherenceValidator}
 * @see {@link com.googlecode.l10nmavenplugin.validators.property.format.MessageFormatFormattingValidator}
 * 
 */
public class ParametricCoherenceValidator extends AbstractL10nValidator implements L10nValidator<PropertyFamily> {

  private final Formatter formatter;

  public ParametricCoherenceValidator(L10nValidatorLogger logger, Formatter formatter) {
    super(logger);

    this.formatter = formatter;
  }

  /**
   * Extract and re-order the parameters by index, then WARN if order is not the same.
   */
  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    String key = propertyFamily.getKey();

    Map<PropertiesFile, List<Integer>> resourceParameters = new HashMap<PropertiesFile, List<Integer>>();

    Collection<PropertiesFile> propertiesFiles = propertyFamily.getExistingPropertyFiles();

    for (PropertiesFile propertiesFile : propertiesFiles) {
      String message = propertiesFile.getProperties().getProperty(key);
      List<Integer> parameters = formatter.captureParameters(message);
      if (parameters.size() > 0) {
        resourceParameters.put(propertiesFile, parameters);
      }
    }

    // Ignore messages only present in a single properties (i.e global / not translated)
    if (resourceParameters.size() >= 2) {
      Map<List<Integer>, Collection<PropertiesFile>> reverseMap = PropertiesFileUtils.reverseMap(resourceParameters);
      if (reverseMap.size() > 1) {
        List<Integer> majorityKey = PropertiesFileUtils.getMajorityKey(reverseMap);
        Collection<PropertiesFile> majorityPropertiesNames = reverseMap.get(majorityKey);
        for (Entry<List<Integer>, Collection<PropertiesFile>> entry : reverseMap.entrySet()) {
          if (!entry.getKey().equals(majorityKey)) {
            Collection<PropertiesFile> faultyPropertiesFiles = entry.getValue();
            // Only warn for now, need more feedback before moving to error.
            L10nReportItem reportItem = new L10nReportItem(Type.INCOHERENT_PARAMETERS, "Incoherent usage of parameters: " + displayParameters(entry.getKey())
                + " versus " + displayParameters(majorityKey) + " in <" + majorityPropertiesNames + ">", faultyPropertiesFiles.toString(), key, null, null);
            reportItems.add(reportItem);
            logger.log(reportItem);
          }
        }
      }
    }
    return 0;
  }

  /**
   * Display parameters
   * 
   * @param parameters
   * @return
   */
  private String displayParameters(List<Integer> parameters) {
    StringBuffer sb = new StringBuffer("[");
    for (int i = 0; i < parameters.size(); i++) {
      if (i != 0) {
        sb.append(",");
      }
      sb.append(formatter.displayIndexedParameter(parameters.get(i)));
    }
    sb.append("]");
    return sb.toString();
  }

  public boolean shouldValidate(PropertyFamily propertyFamily) {
    // Always validate
    return true;
  }
}
