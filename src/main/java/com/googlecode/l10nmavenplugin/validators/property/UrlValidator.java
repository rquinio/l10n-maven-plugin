/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.property;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.PropertiesKeyConventionValidator;
import com.googlecode.l10nmavenplugin.validators.bundle.ParametricCoherenceValidator;

/**
 * Validator to check URL is well formed, either being:
 * <ul>
 * <li>An absolute URL (starting with http(s) or ftp)</li>
 * <li>A pseudo-url starting with mailto</li>
 * <li>A scheme relative URL, starting with //</li>
 * </ul>
 * 
 * Also checks for HTML import URLs, based on file extensions (.js, .css, .png, ...) that would not support HTTPS context, causing mixed content warnings in
 * browsers.
 * 
 * @since 1.0
 * @author romain.quinio
 * 
 */
public class UrlValidator extends PropertiesKeyConventionValidator implements L10nValidator<Property> {

  /**
   * URL regex validation (cf ESAPI.properties)
   */
  private static final String RELATIVE_URL_VALIDATION_REGEXP = "//[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(/?)([a-zA-Z0-9-\\.\\?,:'/\\\\+=&%\\$#_]*)?";

  /**
   * E-mail regex validation (cf ESAPI.properties)
   */
  private static final String EMAIL_VALIDATION_REGEXP = "[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,4}";

  private static final List<String> HTML_URL_INCLUDE_EXTESIONS = Arrays.asList(new String[] { "js", "css", "gif", "jpg", "png", "ico" });

  /**
   * Validation of an absolute URL.
   * 
   * Protocol must be included in URL (either http(s), mailto, or scheme relative)
   */
  private static final String URL_VALIDATION_REGEXP = "^(((ht|f)tp(s?):)?" + RELATIVE_URL_VALIDATION_REGEXP + ")|(mailto:)" + EMAIL_VALIDATION_REGEXP + ".*$";

  protected static final Pattern URL_VALIDATION_PATTERN = Pattern.compile(URL_VALIDATION_REGEXP);

  public UrlValidator(L10nValidatorLogger logger, String[] urlKeys) {
    super(logger, urlKeys);
  }

  /**
   * ERROR if URL does not match regexp.
   * 
   * ERROR if URL does not support https context and is an HTML import.
   * 
   * Performs a MessageFormat if needed.
   * 
   * @note the {@link org.apache.commons.validator.UrlValidator} from Apache does not seem to support scheme relative URLs.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    int nbErrors = 0;
    String formattedMessage = property.getMessage();

    try {
      if (ParametricCoherenceValidator.isParametric(formattedMessage)) {
        formattedMessage = ParametricMessageValidator.defaultFormat(property.getMessage());
      }
      // Unescape HTML in case URL is used in HTML context (ex: &amp; -> &)
      String url = StringEscapeUtils.unescapeHtml(formattedMessage);
      Matcher m = URL_VALIDATION_PATTERN.matcher(url);

      if (!m.matches()) {
        nbErrors++;
        L10nReportItem reportItem = new L10nReportItem(Type.URL_VALIDATION, "Invalid URL syntax.", property, formattedMessage);
        reportItems.add(reportItem);
        logger.log(reportItem);

      } else {
        // If URL path extension is an HTML include (.js, .css, .jpg, ...) check that URL inherits https protocol
        // Context is mandatory for scheme relative URLs
        URL context = new URL("https://");
        URL resultingURL = new URL(context, url);

        String extension = FilenameUtils.getExtension(resultingURL.getPath());
        if (HTML_URL_INCLUDE_EXTESIONS.contains(extension) && !"https".equals(resultingURL.getProtocol())) {
          nbErrors++;
          L10nReportItem reportItem = new L10nReportItem(Type.URL_VALIDATION, "URL for external HTML import [." + extension
              + "] must be scheme relative to avoid mixed content in HTTPS context.", property, formattedMessage);
          reportItems.add(reportItem);
          logger.log(reportItem);
        }
      }

    } catch (IllegalArgumentException e) {
      // Catch MessageFormat errors in case of malformed message
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Type.MALFORMED_PARAMETER, "URL contains malformed parameters: " + e.getMessage(), property,
          formattedMessage);
      reportItems.add(reportItem);
      logger.log(reportItem);
    } catch (MalformedURLException e) {
      nbErrors++;
      L10nReportItem reportItem = new L10nReportItem(Type.URL_VALIDATION, "Malformed URL: " + e.getMessage(), property, formattedMessage);
      reportItems.add(reportItem);
      logger.log(reportItem);
    }

    return nbErrors;
  }

  public boolean shouldValidate(Property property) {
    return matches(property.getKey());
  }
}
