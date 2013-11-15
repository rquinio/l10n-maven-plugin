package com.googlecode.l10nmavenplugin.validators.property.format;

import com.googlecode.l10nmavenplugin.format.Formatter;
import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public abstract class FormattingValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  protected Formatter formatter;

  public FormattingValidator(L10nValidatorLogger logger) {
    super(logger);
  }

  public Formatter getFormatter() {
    return formatter;
  }

}
