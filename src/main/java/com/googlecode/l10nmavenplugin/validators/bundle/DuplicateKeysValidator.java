package com.googlecode.l10nmavenplugin.validators.bundle;

import java.io.File;
import java.util.List;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nValidationException;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;

/**
 * http://java.net/jira/browse/GLASSFISH-17742
 * 
 */
public class DuplicateKeysValidator implements L10nValidator<File> {

  public int validate(File propertiesFile, List<L10nReportItem> reportItems) throws L10nValidationException {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean shouldValidate(File toValidate) {
    return true;
  }

}
