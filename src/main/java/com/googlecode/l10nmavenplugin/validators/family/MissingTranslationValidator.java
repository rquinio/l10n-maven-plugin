/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
import java.util.List;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Validator to check for resources that are missing translations.
 * 
 * As the behavior is normal for new resources when developers only define a default translation in development language, it only applies if number of
 * translations is more than 1.
 * 
 * Root bundle should be excluded, as it usually contains non language dependent resources.
 * 
 * @author romain.quinio
 * @since 1.2
 * 
 */
public class MissingTranslationValidator extends AbstractL10nValidator implements L10nValidator<PropertyFamily> {

  public MissingTranslationValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  /**
   * Raise a WARN for any resource missing translation in at least 2 languages.
   * 
   * Resource translated in only 1 language are ignored.
   * 
   */
  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    // Ignore root bundle
    PropertyFamily propertyFamilyNoRoot = propertyFamily.getPropertyFamilyExcludingRoot();
    int nbProperties = propertyFamilyNoRoot.getNbPropertiesFiles();

    // Properties file names for which a resource is missing translation
    Collection<PropertiesFile> missingPropertyFiles = propertyFamilyNoRoot.getMissingPropertyFiles();

    int nbMissingTranslations = missingPropertyFiles.size();
    // Ignore resource existing only in 1 language (reference language)
    if (nbMissingTranslations < nbProperties - 1 && nbMissingTranslations > 0) {

      L10nReportItem reportItem = new L10nReportItem(Type.MISSING_TRANSLATION, "Resource is not translated, although there are translations in "
          + (nbProperties - nbMissingTranslations) + " other languages", missingPropertyFiles.toString(), propertyFamilyNoRoot.getKey(), null, null);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }

    return 0;
  }

  public boolean shouldValidate(PropertyFamily propertyFamily) {
    // Always validate
    return true;
  }
}
