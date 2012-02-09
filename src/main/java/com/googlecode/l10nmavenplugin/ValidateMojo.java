/* Copyright (c) 2012 Romain Quinio

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.googlecode.l10nmavenplugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.xml.sax.SAXException;

/**
 * Maven plugin to validate a set of l10n properties file against:
 * <ul>
 * <li>Invalid XHTML text</li>
 * <li>Invalid javascript characters</li>
 * <li>Malformed URLs</li>
 * </ul>
 * 
 * @goal validate
 * @phase test
 * @author romain.quinio
 * 
 */
public class ValidateMojo extends AbstractMojo {

  /**
   * TODO Could allow only a subset of xhtml1-transitional, conforming to WCAG
   */
  private static final String XHTML_XSD = "xhtml1-transitional.xsd";

  /**
   * Template for inserting text resource content before XHTML validation. Need to declare HTML entities that are non
   * default XML ones. Also the text has to be inside a div, as plain text is not allowed directly in body.
   */
  private static final String XHTML_TEMPLATE = "<!DOCTYPE html [ " 
	  + "<!ENTITY nbsp \"&#160;\"> "
      + "<!ENTITY copy \"&#169;\"> " 
      + "<!ENTITY cent \"&#162;\"> " 
      + "<!ENTITY pound \"&#163;\"> "
      + "<!ENTITY yen \"&#165;\"> " 
      + "<!ENTITY euro \"&#8364;\"> " 
      + "<!ENTITY sect \"&#167;\"> "
      + "<!ENTITY reg \"&#174;\"> " 
      + "<!ENTITY trade \"&#8482;\"> " 
      + "<!ENTITY ndash \"&#8211;\"> " 
      + "]> "
      + "<html xmlns=\"http://www.w3.org/1999/xhtml\">" 
      + "<head><title /></head><body><div>{0}</div></body></html>";

  /**
   * Protocol must be included in URL (http(s), mailto, or protocol relative)
   */
  private static final String URL_VALIDATION_REGEXP = "^((http[s]?:)?//[-a-zA-Z0-9_.:]+[-a-zA-Z0-9_:@&?=+,.!/~*'%$#]*)|(mailto:).*$";

  /**
   * " \n \r \t are not allowed in js resources, as it would cause a script error.
   */
  private static final String JS_VALIDATION_REGEXP = "^([^\"|\n|\t|\r])*$";

  /**
   * Detection of html tags
   */
  private static final String HTML_REGEXP = ".*\\<[^>]+>.*";
  
  /**
   * Detection of URL
   */
  private static final String URL_REGEXP = ".*//.*";

  /**
   * Directory containing properties file to check
   * 
   * @parameter default-value="src\\main\\resources"
   */
  private File propertyDir;

  /**
   * Keys excluded from validation. Default is none.
   * 
   * @parameter
   */
  private String[] excludedKeys = new String[] {};

  /**
   * Make validation failure not blocking the build
   * 
   * @parameter default-value="false"
   */
  private boolean ignoreFailure = false;

  /**
   * List of keys to match as text resources used from js. Default is ".js.".
   * 
   * @parameter
   */
  private String[] jsKeys = new String[] { ".js." };

  /**
   * List of keys to match as url resources. Default is ".url.".
   * 
   * @parameter
   */
  private String[] urlKeys = new String[] { ".url." };

  /**
   * List of keys to match as html text resources. Default is ".text.".
   * 
   * @parameter
   */
  private String[] htmlKeys = new String[] { ".text." };
  
  /**
   * List of keys to match as non-html text resources. Default is ".title.".
   * 
   * @parameter
   */
  private String[] textKeys = new String[] { ".title." };

  private final Validator xhtmlValidator;
  private final Pattern urlValidationPattern = Pattern.compile(URL_VALIDATION_REGEXP);
  private final Pattern jsValidationPattern = Pattern.compile(JS_VALIDATION_REGEXP);
  private final Pattern htmlPattern = Pattern.compile(HTML_REGEXP);
  private final Pattern urlPattern = Pattern.compile(URL_REGEXP);

  public ValidateMojo() throws URISyntaxException, SAXException {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    URL schemaURL = this.getClass().getClassLoader().getResource(XHTML_XSD);
    xhtmlValidator = factory.newSchema(schemaURL).newValidator();
    // xhtmlValidator = null;
  }

  /**
   * Entry point for the plugin
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    int nbErrors = 0;
    try {
      getLog().info("Looking for properties files in: " + propertyDir.getAbsolutePath());
      File[] files = propertyDir.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".properties");
        }
      });
      if (files == null || files.length == 0) {
        getLog().warn(
            "No properties file under folder " + propertyDir.getAbsolutePath() + ". Skipping l10n validation.");
        return;
      }
      for (File file : files) {
        InputStream inStream = new FileInputStream(file);
        String propertyName = file.getName();
        Properties properties = new Properties();
        properties.load(inStream);

        nbErrors += validateProperties(properties, propertyName);
      }

    } catch (IOException e) {
      throw new MojoExecutionException("An unexpected exception has occured", e);
    }

    if (nbErrors > 0) {
      if (ignoreFailure) {
        getLog().error("Validation of l10n properties has failed with " + nbErrors + " errors");
      } else {
        throw new MojoFailureException("Validation of l10n properties has failed with " + nbErrors + " errors");
      }
    } else {
      getLog().info("Validation of l10n properties was successful");
    }
  }

  /**
   * Validate a Properties file
   * 
   * @param properties
   * @param propertyName
   *          the name of the file, for error logging
   * @return Number of errors
   */
  protected int validateProperties(Properties properties, String propertyName) {
    int nbErrors = 0;
    getLog().info("Validating " + propertyName);
    Set<Object> keys = properties.keySet(); // .propertyNames
    for (Object obj : keys) {
      String key = (String) obj;
      String message = properties.getProperty(key);
      // Only validate if key is not excluded, and message is defined.
      if (StringUtils.indexOfAny(key, excludedKeys) == -1 && message.length() > 0) {
        if (StringUtils.indexOfAny(key, htmlKeys) != -1) {
          nbErrors += validateHtmlResource(key, message, propertyName);
        } else if (StringUtils.indexOfAny(key, jsKeys) != -1) {// key.contains("ALLP.listelem.Error")
          // Js should pass both js and html
          nbErrors += validateJsResource(key, message, propertyName);
          nbErrors += validateHtmlResource(key, message, propertyName);
        } else if (StringUtils.indexOfAny(key, urlKeys) != -1) {
          nbErrors += validateUrlResource(key, message, propertyName);
        } else if (StringUtils.indexOfAny(key, textKeys) != -1){
          nbErrors += validateTextResource(key, message, propertyName);
        } else {
          nbErrors += validateOtherResource(key, message, propertyName);
        }
      }
    }
    return nbErrors;
  }

  /**
   * Validate js text using regexp.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  protected int validateJsResource(String key, String message, String propertyName) {
    int nbErrors = 0;
    Matcher m = jsValidationPattern.matcher(message);
    if (!m.matches()) {
      nbErrors++;
      StringBuffer sb = new StringBuffer();
      sb.append("<").append(propertyName).append(">Js error for key <").append(key).append(">\n");
      sb.append("Message value was: [").append(message).append("]\n\n");
      getLog().error(sb);
    }
    return nbErrors;
  }

  /**
   * Validate URLs using regexp. The URLValidator from Apache does not seem to support scheme relative URLs.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  protected int validateUrlResource(String key, String message, String propertyName) {
    int nbErrors = 0;
    message = MessageFormat.format(message, "0", "1", "2");
    //Unescape HTML in case URL is used in HTML context (ex: &amp; -> &)
    String url = StringEscapeUtils.unescapeHtml(message);
    Matcher m = urlValidationPattern.matcher(url);

    if (!m.matches()) {
      nbErrors++;
      StringBuffer sb = new StringBuffer();
      sb.append("<").append(propertyName).append(">URL error for <").append(key).append(">\n");
      sb.append("Message value was: [").append(message).append("]\n\n");
      getLog().error(sb);
    }
    return nbErrors;
  }

  /**
   * Validate HTML text using XHTML validator.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  protected int validateHtmlResource(String key, String message, String propertyName) {
    int nbErrors = 0;
    if (xhtmlValidator != null) {
      String xhtml = MessageFormat.format(XHTML_TEMPLATE, message);

      try {
        Source source = new StreamSource(new ByteArrayInputStream(xhtml.getBytes("UTF-8")));
        xhtmlValidator.validate(source);

      } catch (SAXException e) {
        nbErrors++;
        StringBuffer sb = new StringBuffer();
        sb.append("<").append(propertyName).append(">HTML error for key <").append(key).append("> : ");
        sb.append(e.getMessage()).append("\n");
        sb.append("Message value was: [").append(message).append("]\n\n");
        getLog().error(sb);
      } catch (IOException e) {
        nbErrors++;
        StringBuffer sb = new StringBuffer();
        sb.append("<").append(propertyName).append(">HTML error for key <").append(key).append("> : ");
        sb.append(e.getMessage()).append("\n");
        sb.append("Message value was: [").append(message).append("]\n\n");
        getLog().error(sb);
      }
    }
    return nbErrors;
  }
  
  /**
   * Check resource does not contain HTML/URL
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  protected int validateTextResource(String key, String message, String propertyName) {
    int nbErrors = 0;
    Matcher htmlMatcher = htmlPattern.matcher(message);
    Matcher urlMatcher = urlPattern.matcher(message);
    if (htmlMatcher.matches() || urlMatcher.matches()) {
      nbErrors++;
      StringBuffer sb = new StringBuffer();
      sb.append("<").append(propertyName).append(">Text resource contains HTML or URL: <")
          .append(key).append(">\n");
      sb.append("Message value was: [").append(message).append("]\n\n");
      getLog().error(sb);
    }
    return nbErrors;
  }

  /**
   * Warn if other resources contain HTML/URL.
   * 
   * @param key
   * @param message
   * @param propertyName
   * @return Number of errors
   */
  protected int validateOtherResource(String key, String message, String propertyName) {
    Matcher htmlMatcher = htmlPattern.matcher(message);
    Matcher urlMatcher = urlPattern.matcher(message);
    
    if (htmlMatcher.matches() || urlMatcher.matches()) {
      StringBuffer sb = new StringBuffer();
      sb.append("<").append(propertyName).append(">Resource may contain HTML or URL, but is not listed as such. No validation was performed: <")
          .append(key).append(">\n");
      sb.append("Message value was: [").append(message).append("]\n\n");
      getLog().warn(sb);
    }
    return 0;
  }

  public void setPropertyDir(File propertyDir) {
    this.propertyDir = propertyDir;
  }

  public void setExcludedKeys(String[] excludedKeys) {
    this.excludedKeys = excludedKeys;
  }
}
