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

import java.util.List;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Detects properties not following a configurable naming convention.
 * 
 * By default uses a "community standard" convention: <view or dialog>[.<sub-context>].<control-type>.<name>
 * 
 * Control type could be deducted from plugin configuration (htmlKeys, urlKeys, textKeys, jsKeys, ....)
 * 
 * [A-Za-z]+.[subContext].{text, title, url, ...}.[A-Za-z]+ *
 * 
 * http://docs.codehaus.org/display/SONAR/Internationalization#Internationalization-Namingconventionsforkeys
 * http://stackoverflow.com/questions/4366107/what-is-a-proper-way-for-naming-of-message-properties-in-i18n
 * https://confluence.sakaiproject.org/display/I18N/Best+Practices+for+Internationalized+Tools+in+Sakai
 * 
 * TODO
 * 
 * @since ??
 * @author romain.quinio
 */
public class KeyNamingConventionValidator extends AbstractL10nValidator implements L10nValidator<PropertyFamily> {

  public KeyNamingConventionValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    return 0;
  }

  public boolean shouldValidate(PropertyFamily propertyFamily) {
    // Always validate
    return true;
  }

}
