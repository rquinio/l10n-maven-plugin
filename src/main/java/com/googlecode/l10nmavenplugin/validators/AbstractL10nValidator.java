package com.googlecode.l10nmavenplugin.validators;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

/**
 * Abstract class for {@link L10nValidator}
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public abstract class AbstractL10nValidator {

  protected final L10nValidatorLogger logger;

  public AbstractL10nValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }
}
