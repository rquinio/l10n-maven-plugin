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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Validator to check for identical and almost identical translations.
 * 
 * Identical translation increases the maintenance in case of modifications, and would better be moved to a root bundle if property is non-language dependent.
 * The property can still be overridden later for a specific language.
 * 
 * Almost identical translation can indicate a copy/paste mistake on property that is not actually language dependent. Or if it is language dependent, it means
 * it is not translated in some languages.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class IdenticalTranslationValidator extends AbstractL10nValidator implements L10nValidator<PropertyFamily> {

  public IdenticalTranslationValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  /**
   * INFO for property that have same translation in all languages (if > 1 and excluding language for which value is missing or empty)
   * 
   * WARNING for property that have same translation in most languages except a few.
   * 
   */
  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    int nbProperties = propertyFamily.getExistingPropertyFiles().size();

    // Ignore properties only defined in 1 language
    if (nbProperties > 1) {
      // Group properties per different values
      Collection<Property> values = propertyFamily.getValues();

      int nbDifferentValues = values.size();
      Property mostIdenticalProp = getMostIdenticalProp(values);

      Collection<PropertiesFile> mostIdenticalPropertiesFiles = mostIdenticalProp.getContainingPropertiesFiles();

      if (nbDifferentValues == 1) {
        // All properties have same value
        L10nReportItem reportItem = new L10nReportItem(Type.IDENTICAL_TRANSLATION,
            "Resource has identical translation in all languages, it could be defined in a root bundle", mostIdenticalPropertiesFiles.toString(),
            propertyFamily.getKey(), mostIdenticalProp.getMessage(), null);
        reportItems.add(reportItem);
        logger.log(reportItem);

      } else if (nbDifferentValues > 0 && mostIdenticalPropertiesFiles.size() > nbProperties / 2) {
        // Properties have few different values
        Collection<PropertiesFile> notIdenticalPropertiesFiles = new ArrayList<PropertiesFile>(propertyFamily.getExistingPropertyFiles());
        notIdenticalPropertiesFiles.removeAll(mostIdenticalPropertiesFiles);

        L10nReportItem reportItem = new L10nReportItem(Type.ALMOST_IDENTICAL_TRANSLATION, "Resource has identical translation in "
            + mostIdenticalPropertiesFiles.size() + " languages, but " + (nbDifferentValues - 1) + " different value(s) in the other languages "
            + notIdenticalPropertiesFiles, mostIdenticalPropertiesFiles.toString(), propertyFamily.getKey(), mostIdenticalProp.getMessage(), null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }
    }
    return 0;
  }

  /**
   * Get the property whose value is most identical between languages
   * 
   * @param properties
   * @return
   */
  private Property getMostIdenticalProp(Collection<Property> properties) {
    int maxContainingFiles = 0;
    Property mostIdenticalProp = null;
    for (Property property : properties) {
      int nbContainingFiles = property.getContainingPropertiesFiles().size();
      if (nbContainingFiles > maxContainingFiles) {
        maxContainingFiles = nbContainingFiles;
        mostIdenticalProp = property;
      }
    }
    return mostIdenticalProp;
  }

  public boolean shouldValidate(PropertyFamily propertyFamily) {
    // Always validate
    return true;
  }
}
