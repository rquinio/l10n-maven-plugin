package com.googlecode.l10nmavenplugin.validators;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;

public class MissingTranslationValidatorTest {
  private L10nValidator missingValidator;
  
  private List<L10nReportItem> reportItems;
  
  @Before
  public void setUp(){
    missingValidator = new MissingTranslationValidator(new L10nValidatorLogger());
    reportItems = new ArrayList<L10nReportItem>();
  }
  
  @Test
  public void testMissingTranslations(){
    Set<String> propertiesNames = new HashSet<String>();
    Collections.addAll(propertiesNames, "Bundle_en","Bundle_fr","Bundle_es","Bundle_de");

    missingValidator.validate("key1", "", "Bundle", reportItems);
    missingValidator.validate("key1", "blabla", "Bundle_en", reportItems);
    missingValidator.validate("key1", "blabla", "Bundle_fr", reportItems);
    missingValidator.validate("key1", "", "Bundle_es", reportItems);

    assertEquals(0, missingValidator.report(propertiesNames, reportItems));
  }
}
