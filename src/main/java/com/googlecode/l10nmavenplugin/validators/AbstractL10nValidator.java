package com.googlecode.l10nmavenplugin.validators;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class AbstractL10nValidator {

  protected final L10nValidatorLogger logger;

  public AbstractL10nValidator(L10nValidatorLogger logger) {
    this.logger = logger;
  }
}
