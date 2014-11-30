package com.googlecode.l10nmavenplugin.format;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

import com.googlecode.l10nmavenplugin.model.PropertiesFile;

/**
 * Handles the replacing of inner references to other entries of the property file by their corresponding values
 * 
 */
public class InnerResourcesFormatter {

  private final String innerResourceRegex;

  private final Pattern innerResourcePattern;

  private final Pattern innerResourcePresencePattern;

  /**
   * constructor
   * 
   * @param innerResourceRegex
   *          regex to use to detect inner resource references as well as extract the keys of those references to do the
   *          replace, ex: $\{([A-Za-z\._]+)\} for ${key_ref_1} and the () to allow the matcher to extract the key
   *          reference key_ref_1 from the match
   */
  public InnerResourcesFormatter(String innerResourceRegex) {
    this.innerResourceRegex = innerResourceRegex;
    innerResourcePattern = Pattern.compile(innerResourceRegex, Pattern.DOTALL);
    innerResourcePresencePattern = Pattern.compile("^.*" + innerResourceRegex + ".*$", Pattern.DOTALL);
  }

  /**
   * replaces inner references to other
   * 
   * @param message
   *          the message pattern
   * @param file
   *          the properties file model where to look for the references
   * @return the message with the inner resource references replaced by their corresponding values
   * @throws IllegalArgumentException
   *           when the references are not correct of referencing non existing keys in the properties file
   * @see Formatter#format(String, Object...)
   */
  public String format(String message, PropertiesFile file) throws IllegalArgumentException {
    StringBuffer resultBuffer = new StringBuffer();
    Matcher matcher = innerResourcePattern.matcher(message);
    while (matcher.find()) {
      String innerKey = matcher.group(1);
      if (StringUtils.isBlank(innerKey)) {
        throw new IllegalArgumentException(String.format(
            "Could not extract inner key with regex %s in %s", innerResourceRegex, matcher.group()));
      }
      String value = file.getProperties().getProperty(innerKey);
      if (value == null) {
        throw new IllegalArgumentException(String.format(
            "Inner key <%s> not found in property file <%s>", innerKey, file.getFileName()));
      }
      matcher.appendReplacement(resultBuffer, value);
    }
    matcher.appendTail(resultBuffer);
    return resultBuffer.toString();
  }

  public String defaultFormat(String message) throws IllegalArgumentException {
    KeyMirrorPropertiesFile mirrorPropertiesFile = new KeyMirrorPropertiesFile();
    return format(message, mirrorPropertiesFile);
  }

  public boolean hasInnerResources(String message) {
    boolean isParametric = false;
    if (StringUtils.isNotBlank(message)) {
      isParametric = innerResourcePresencePattern.matcher(message).matches();
    }
    return isParametric;
  }

  public List<String> captureInnerResources(String message) {
    List<String> innerKeys = new LinkedList<String>();
    if (StringUtils.isNotBlank(message)) {
      Matcher matcher = innerResourcePattern.matcher(message);
      while (matcher.find()) {
        String innerKey = matcher.group(1);
        if (StringUtils.isNotBlank(innerKey)) {
          innerKeys.add(innerKey);
        }
      }
    }
    return innerKeys;
  }

  public String getInnerResourceRegex() {
    return innerResourceRegex;
  }

  private static final class KeyMirrorPropertiesFile implements PropertiesFile {

    private final Properties properties = new KeyMirrorProperties();

    public String getFileName() {
      return null;
    }

    public String getBundleName() {
      return null;
    }

    public Locale getLocale() {
      return null;
    }

    public Properties getProperties() {
      return properties;
    }

    public Set<String> getDuplicatedResourceKeys() {
      return null;
    }

  }

  @SuppressWarnings("serial")
  private static final class KeyMirrorProperties extends Properties {

    @Override
    public String getProperty(String key, String defaultValue) {
      return key;
    }

    @Override
    public String getProperty(String key) {
      return key;
    }

  }

}
