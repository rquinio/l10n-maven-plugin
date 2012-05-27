/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFamily;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Validator to check for keys that are duplicated, i.e. identical values that exist twice or more for the same language.
 * 
 * INFO for keys that have duplication in all languages (excluding languages where value are missing or empty), meaning that they
 * can be merged into 1 unique key.
 * 
 * WARN if there is duplication in at least 2 or 3 languages but no all languages, suggesting a mistake.
 * 
 * @author romain.quinio
 * @since ??
 * 
 */
public class DuplicationValidator extends AbstractL10nValidator implements L10nValidator<PropertiesFamily> {

  public DuplicationValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  public int validate(PropertiesFamily propertiesFamily, List<L10nReportItem> reportItems) {

    int nbPropertiesFiles = propertiesFamily.getNbPropertiesFiles();
    List<String> allDuplicatedResourceKeys = new ArrayList<String>();
    Set<String> duplicatedResourceKeys = new HashSet<String>();

    for (PropertiesFile propertiesFile : propertiesFamily.getPropertiesFiles()) {
      Set<String> duplicates = propertiesFile.getDuplicatedResourceKeys();
      allDuplicatedResourceKeys.addAll(duplicates);
      duplicatedResourceKeys.addAll(duplicates);
    }

    // TODO need to have keys corresponding to duplicates + language list for duplicates...

    // TODO Simply warn for duplicates in same language ??

    for (String key : duplicatedResourceKeys) {
      int nbDuplicates = Collections.frequency(allDuplicatedResourceKeys, key);
      if (nbDuplicates == nbPropertiesFiles) {
        L10nReportItem reportItem = new L10nReportItem(Severity.INFO, Type.DUPLICATED_RESOURCE,
            "Resource is a duplicate from another resource in all languages", null, key, null, null);
        reportItems.add(reportItem);
        logger.log(reportItem);

      } else if (nbPropertiesFiles - nbDuplicates < 2) {
        // Probably a mistake
        L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.ALMOST_DUPLICATED_RESOURCE,
            "Resource is a duplicate from another resource in " + nbDuplicates + " languages, but not all languages.", null, key,
            null, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    }

    return 0;
  }
}
