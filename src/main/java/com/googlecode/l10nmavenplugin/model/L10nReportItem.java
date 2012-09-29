/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Single unit of reporting during validation.
 * 
 * @author romain.quinio
 * 
 */
public class L10nReportItem implements Comparable<L10nReportItem> {

  /**
   * Ordered severity
   */
  public enum Severity {
    /**
     * Very likely to cause a bug in the application
     */
    ERROR,
    /**
     * Potential bug or bad practices
     */
    WARN,
    /**
     * Informative or suggestion
     */
    INFO,
  }

  /**
   * Ordered error types by severity
   */
  public enum Type {
    // Errors
    MALFORMED_PARAMETER("message.malformedParameters.title", "message.malformedParameters.description", Severity.ERROR), //
    JS_DOUBLE_QUOTED_VALIDATION("message.jsDoubleQuotedValidation.title", "message.jsDoubleQuotedValidation.description", Severity.ERROR), //
    JS_SINGLE_QUOTED_VALIDATION("message.jsSingleQuotedValidation.title", "message.jsSingleQuotedValidation.description", Severity.ERROR), //
    JS_NEWLINE_VALIDATION("message.jsNewlineValidation.title", "message.jsNewlineValidation.description", Severity.ERROR), //
    UNESCAPED_QUOTE_WITH_PARAMETERS("message.UnescapedQuotesParams.title", "message.UnescapedQuotesParams.description", Severity.ERROR), //
    HTML_VALIDATION("message.htmlValidation.title", "message.htmlValidation.description", Severity.ERROR), //
    TEXT_VALIDATION_NO_HTML("message.plainTextWithHtml.title", "message.plainTextWithHtml.description", Severity.ERROR), //
    URL_VALIDATION("message.urlValidation.title", "message.urlValidation.description", Severity.ERROR), //
    TEXT_VALIDATION_NO_URL("message.plainTextWithUrl.title", "message.plainTextWithUrl.description", Severity.ERROR), //
    CUSTOM_PATTERN("message.customPattern.title", "message.customPattern.description", Severity.ERROR),

    // Warnings
    ESCAPED_QUOTE_WITHOUT_PARAMETER("message.escapedQuoteWithoutParam.title", "message.escapedQuoteWithoutParam.description", Severity.WARN), //
    INCOHERENT_PARAMETERS("message.incoherentParams.title", "message.incoherentParams.description", Severity.WARN), //
    MISSING_TRANSLATION("message.missingTranslation.title", "message.missingTranslation.description", Severity.WARN), //
    UNDECLARED_HTML_RESOURCE("message.undeclaredHtml.title", "message.undeclaredHtml.description", Severity.WARN), //
    UNDECLARED_URL_RESOURCE("message.undeclaredUrl.title", "message.undeclaredUrl.description", Severity.WARN), //
    TRAILING_WHITESPACE("message.trailingWhitespace.title", "message.trailingWhitespace.description", Severity.WARN), //
    ALMOST_DUPLICATED_RESOURCE("message.almostDuplicatedResource.title", "message.almostDuplicatedResource.description", Severity.WARN), //
    ALMOST_IDENTICAL_TRANSLATION("message.almostIdenticalTranslation.title", "message.almostIdenticalTranslation.description", Severity.WARN), //
    SPELLCHECK("message.spellcheck.title", "message.spellcheck.description", Severity.WARN), //
    INCOHERENT_TAGS("message.incoherentTags.title", "message.incoherentTags.description", Severity.WARN),

    // Infos
    EXCLUDED("message.excluded.title", "message.excluded.description", Severity.INFO), //
    DUPLICATED_RESOURCE("message.duplicatedResource.title", "message.duplicatedResource.description", Severity.INFO), //
    IDENTICAL_TRANSLATION("message.identicalTranslation.title", "message.identicalTranslation.description", Severity.INFO);

    private final String titleLocKey;
    private final String descriptionLocKey;
    private Severity severity;

    private Type(String titleLocKey, String descriptionLocKey, Severity defaultSeverity) {
      this.titleLocKey = titleLocKey;
      this.descriptionLocKey = descriptionLocKey;
      this.severity = defaultSeverity;
    }

    public String getTitleLocKey() {
      return titleLocKey;
    }

    public String getDescriptionLocKey() {
      return descriptionLocKey;
    }

    public Severity getSeverity() {
      return severity;
    }

    public void setSeverity(Severity severity) {
      this.severity = severity;
    }

    @Override
    public String toString() {
      String s;

      try {
        ResourceBundle bundle = getBundle(Locale.ENGLISH);
        s = bundle.getString(titleLocKey);
      } catch (MissingResourceException e) {
        // Cobertura execution doesn' have access to .properties ?
        s = name();
      }

      return s;
    }

    /**
     * The bundle containing localized description of item types.
     * 
     * @param locale
     * @return
     */
    private ResourceBundle getBundle(Locale locale) {
      return ResourceBundle.getBundle("l10n-report", locale, this.getClass().getClassLoader());
    }
  }

  /**
   * Type of error.
   */
  private final Type itemType;

  /**
   * The error text
   */
  private final String itemMessage;

  /**
   * Name of properties file
   */
  private final String propertiesName;

  /**
   * Key of the property
   */
  private final String propertiesKey;

  /**
   * Message of the property
   */
  private final String propertiesValue;

  /**
   * The value actually used for validation.
   */
  private final String formattedPropertiesValue;

  public L10nReportItem(Type itemType, String itemMessage, Property property, String formattedPropertiesValue) {
    this(itemType, itemMessage, property.getPropertiesFile().toString(), property.getKey(), property.getMessage(), formattedPropertiesValue);
  }

  public L10nReportItem(Type itemType, String itemMessage, String propertiesName, String propertiesKey, String propertiesValue, String formattedPropertiesValue) {
    this.itemType = itemType;
    this.itemMessage = itemMessage;
    this.propertiesName = propertiesName;
    this.propertiesKey = propertiesKey;
    this.propertiesValue = propertiesValue;
    this.formattedPropertiesValue = formattedPropertiesValue;
  }

  public Severity getItemSeverity() {
    return itemType.getSeverity();
  }

  public Type getItemType() {
    return itemType;
  }

  public String getItemMessage() {
    return itemMessage;
  }

  public String getPropertiesName() {
    return propertiesName;
  }

  public String getPropertiesKey() {
    return propertiesKey;
  }

  public String getPropertiesValue() {
    return propertiesValue;
  }

  public String getFormattedPropertiesValue() {
    return formattedPropertiesValue;
  }

  public int compareTo(L10nReportItem o) {
    int result = getItemSeverity().compareTo(o.getItemSeverity());
    if (result == 0) {
      result = itemType.compareTo(o.getItemType());
    }
    if (result == 0) {
      result = propertiesKey.compareTo(o.getPropertiesKey());
    }
    if (result == 0) {
      result = propertiesName.compareTo(o.getPropertiesName());
    }
    return result;
  }

  public static Map<Type, List<L10nReportItem>> byType(List<L10nReportItem> items) {
    // Order items by Type
    Collections.sort(items);

    // Use TreeMap to keep Type ordering
    Map<Type, List<L10nReportItem>> mapByType = new TreeMap<Type, List<L10nReportItem>>();

    for (L10nReportItem reportItem : items) {
      List<L10nReportItem> itemsByType = mapByType.get(reportItem.getItemType());
      if (itemsByType == null) {
        itemsByType = new ArrayList<L10nReportItem>();
        mapByType.put(reportItem.getItemType(), itemsByType);
      }
      itemsByType.add(reportItem);
    }

    return mapByType;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((itemType == null) ? 0 : itemType.hashCode());
    result = prime * result + ((propertiesKey == null) ? 0 : propertiesKey.hashCode());
    result = prime * result + ((propertiesName == null) ? 0 : propertiesName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof L10nReportItem)) {
      return false;
    } else {
      return this.compareTo((L10nReportItem) obj) == 0;
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("type", itemType.name()).append("severity", getItemSeverity()).toString();
  }
}
