package com.googlecode.l10nmavenplugin;

import java.io.File;

/**
 * Interface for l10 validation/report Mojo configuration
 * 
 * @author romain.quinio
 * 
 */
public interface L10nValidationConfiguration {

  public void setPropertyDir(File propertyDir);

  public void setExcludedKeys(String[] excludedKeys);

  public void setIgnoreFailure(boolean ignoreFailure);

  public void setJsKeys(String[] jsKeys);

  public void setUrlKeys(String[] urlKeys);

  public void setHtmlKeys(String[] htmlKeys);

  public void setTextKeys(String[] textKeys);

  public void setCustomPatterns(CustomPattern[] customPatterns);
}
