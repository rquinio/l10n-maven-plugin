package com.googlecode.l10nmavenplugin.validators.property.format;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;

import com.googlecode.l10nmavenplugin.format.InnerResourcesFormatter;
import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

public class InnerResourcesFormattingValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  private final InnerResourcesFormatter formatter;

  public InnerResourcesFormattingValidator(L10nValidatorLogger logger, String innerResourceRegex) {
    super(logger);
    if (StringUtils.isBlank(innerResourceRegex)) {
      formatter = null;
    }
    else {
      formatter = new InnerResourcesFormatter(innerResourceRegex);
    }
  }

  public int validate(Property property, List<L10nReportItem> reportItems) throws L10nValidationException {
    int nbErrors = 0;

    if (formatter != null) {
      List<String> innerResourceKeys = formatter.captureInnerResources(property.getMessage());
      for (String innerResourceKey : innerResourceKeys) {
        if (property.getPropertiesFile().getProperties().getProperty(innerResourceKey) == null) {
          // inner resource points to a non existing property
          L10nReportItem reportItem = new L10nReportItem(Type.INNER_RESOURCE_DOES_NOT_EXIST,
              "Non-existing inner resource reference: " + innerResourceKey, property, null);
          reportItems.add(reportItem);
          logger.log(reportItem);
          nbErrors++;
        }
      }
    }
    return nbErrors;
  }

  public boolean shouldValidate(Property toValidate) {
    return formatter != null;
  }

  public InnerResourcesFormatter getFormatter() {
    return formatter;
  }

}
