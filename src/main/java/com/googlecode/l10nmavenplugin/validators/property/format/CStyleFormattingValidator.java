package com.googlecode.l10nmavenplugin.validators.property.format;

import java.util.List;

import com.googlecode.l10nmavenplugin.format.CStyleFormatter;
import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * Validator to check syntax of {@link java.util.Formatter} parametric resources
 * 
 */
public class CStyleFormattingValidator extends FormattingValidator implements L10nValidator<Property> {

  public CStyleFormattingValidator(L10nValidatorLogger logger) {
    super(logger);

    formatter = new CStyleFormatter();
  }

  public int validate(Property property, List<L10nReportItem> reportItems) throws L10nValidationException {
    // Nothing to validate so far
    return 0;
  }

  public boolean shouldValidate(Property property) {
    // Always validate
    return true;
  }

}
