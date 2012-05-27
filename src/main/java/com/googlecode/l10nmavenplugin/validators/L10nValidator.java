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

import java.util.List;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;

/**
 * Generic interface for validation on an element or group of {@link java.util.Properties} files. The scope can be:
 * <ul>
 * <li>The syntax of a single {@link com.googlecode.l10nmavenplugin.model.Property}</li>
 * <li>The coherence of translation of a property across languages ({@link com.googlecode.l10nmavenplugin.model.PropertyFamily})</li>
 * <li>The coherence of a ({@link com.googlecode.l10nmavenplugin.model.PropertiesFile})</li>
 * <li>The coherence of the whole bundle (a group of Properties files, (
 * {@link com.googlecode.l10nmavenplugin.model.PropertiesFamily}))</li>
 * <li>etc.</li>
 * </ul>
 * 
 * Note: the limitation of using Java templates is that a given {@link L10nValidator} won't be able to implement more than 1 scope
 * of validation.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 * @param <T>
 *          scope of the validation
 */
public interface L10nValidator<T> {

  /**
   * Validate the syntax/coherence of T
   * 
   * @param toValidate
   *          the property/group/bundle to validate
   * @param reportItems
   *          list of items to which validation issues should be added.
   * @return number of reportItems with a severity {@link L10nReportItem.Severity#ERROR}
   */
  int validate(T toValidate, List<L10nReportItem> reportItems);

}
