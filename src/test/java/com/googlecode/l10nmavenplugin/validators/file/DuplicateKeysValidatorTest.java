package com.googlecode.l10nmavenplugin.validators.file;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

public class DuplicateKeysValidatorTest extends AbstractL10nValidatorTest<File> {

  private DuplicateKeysValidator validator;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    validator = new DuplicateKeysValidator(logger, getFile("duplicates"));
  }

  @Test
  public void duplicateKeysShouldRaiseError() {
    File file = getFile("duplicates/Bundle.properties");

    assertEquals(2, validator.validate(file, items));
    assertEquals(2, items.size());
  }

}
