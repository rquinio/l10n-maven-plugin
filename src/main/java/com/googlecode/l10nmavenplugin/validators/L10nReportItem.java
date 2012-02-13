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

public class L10nReportItem implements Comparable<L10nReportItem> {
  
  /**
   * Ordered severity
   */
  public enum Severity {
    //Very likely to cause a bug in the appplication
    ERROR,
    //Potential bug or bad practices
    WARN,
    //Informative
    INFO,
  }
  
  private Severity itemSeverity;
  
  /**
   * Ordered error types by severity
   */
  public enum Type {
    //Errors
    MALFORMED_PARAMETER("message.malformedParameters.title", "message.malformedParameters.description"),   
    JS_VALIDATION("message.jdValidation.title","message.jdValidation.description"),
    UNESCAPED_QUOTE_WITH_PARAMETERS("message.UnescapedQuotesParams.title","message.UnescapedQuotesParams.description"), 
    HTML_VALIDATION("message.htmlValidation.title","message.htmlValidation.description"),
    TEXT_VALIDATION_NO_HTML("message.plainTextWithHtml.title","message.plainTextWithHtml.description"),
    URL_VALIDATION("message.urlValidation.title","message.urlValidation.description"), 
    TEXT_VALIDATION_NO_URL("message.plainTextWithUrl.title","message.plainTextWithUrl.description"),
        
    //Warnings
    ESCAPED_QUOTE_WITHOUT_PARAMETER("message.escapedQuoteWithoutParam.title","message.escapedQuoteWithoutParam.description"),
    INCOHERENT_PARAMETERS("message.incoherentParams.title","message.incoherentParams.description"),
    MISSING_TRANSLATION("message.missingTranslation.title","message.missingTranslation.description"),  
    UNDECLARED_HTML_RESOURCE("message.undeclaredHtml.title","message.undeclaredHtml.description"),
    UNDECLARED_URL_RESOURCE("message.undeclaredUrl.title","message.undeclaredUrl.description"),
        
    //Infos
    EXCLUDED("message.excluded.title","message.excluded.description");
    
    private final String titleLocKey;
    private final String descriptionLocKey;
    
    private Type(String titleLocKey, String descriptionLocKey){
      this.titleLocKey = titleLocKey;
      this.descriptionLocKey = descriptionLocKey;
    }
    
    public String getTitleLocKey(){
      return titleLocKey;
    }
    public String getDescriptionLocKey(){
      return descriptionLocKey;
    }
  }
  
  /**
   * Type of error.
   */
  private Type itemType;
  
  /**
   * The error text
   */
  private String itemMessage;
  
  /**
   * Name of properties file
   */
  private String propertiesName;
  
  /**
   * Key of the property
   */
  private String propertiesKey;
  
  /**
   * Message of the property
   */
  private String propertiesValue;
  
  /**
   * The value actually used for validation.
   */
  private String formattedPropertiesValue;
  
  public L10nReportItem(Severity itemSeverity, 
      Type itemType, 
      String itemMessage, 
      String propertiesName, 
      String propertiesKey, 
      String propertiesValue, 
      String formattedPropertiesValue){
    this.itemSeverity = itemSeverity;
    this.itemType = itemType;
    this.itemMessage = itemMessage;
    this.propertiesName = propertiesName;
    this.propertiesKey = propertiesKey;
    this.propertiesValue = propertiesValue;
    this.formattedPropertiesValue = formattedPropertiesValue;
  }

  public Severity getItemSeverity() {
    return itemSeverity;
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
    int result = itemSeverity.compareTo(o.getItemSeverity());
    if(result == 0){
      result = itemType.compareTo(o.getItemType());
    }
    if(result == 0){
      result = propertiesKey.compareTo(o.getPropertiesKey());
    }
    if(result == 0){
      result = propertiesName.compareTo(o.getPropertiesName());
    }
    return result;
  }
}
