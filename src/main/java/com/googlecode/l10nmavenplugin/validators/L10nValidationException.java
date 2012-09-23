package com.googlecode.l10nmavenplugin.validators;

/**
 * Encapsulates any unexpected exception occurring during validation
 * 
 * @since 1.5
 * @author romain.quinio
 * 
 */
public class L10nValidationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public L10nValidationException(String message, Throwable e) {
    super(message, e);
  }

  public L10nValidationException(String message) {
    super(message);
  }

  public L10nValidationException(Throwable e) {
    super(e);
  }
}
