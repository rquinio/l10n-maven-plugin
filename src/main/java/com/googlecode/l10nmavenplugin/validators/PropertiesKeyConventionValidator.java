package com.googlecode.l10nmavenplugin.validators;

import org.apache.commons.lang.StringUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public abstract class PropertiesKeyConventionValidator extends AbstractL10nValidator {

  private final String[] keysPattern;

  public PropertiesKeyConventionValidator(L10nValidatorLogger logger, String[] keysPattern) {
    super(logger);
    this.keysPattern = keysPattern;
  }

  protected boolean matches(String key) {
    return (StringUtils.indexOfAny(key, keysPattern) != -1);
  }

}
