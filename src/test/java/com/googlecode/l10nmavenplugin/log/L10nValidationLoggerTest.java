package com.googlecode.l10nmavenplugin.log;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;

public class L10nValidationLoggerTest {
  private L10nValidatorLogger logger;
  private Log log;

  @Before
  public void setUp() {
    log = mock(Log.class);
    logger = new L10nValidatorLogger(log);
  }

  @Test
  public void testErrorLogging() {
    L10nReportItem item = new L10nReportItem(Severity.ERROR, Type.EXCLUDED, "", "", "", "", null);

    logger.log(item);

    verify(log).error(any(CharSequence.class));
  }

  @Test
  public void testWarnLogging() {
    L10nReportItem item = new L10nReportItem(Severity.WARN, Type.EXCLUDED, "", "", "", "", null);

    logger.log(item);

    verify(log).warn(any(CharSequence.class));
  }

  @Test
  public void testInfoLogging() {
    L10nReportItem item = new L10nReportItem(Severity.INFO, Type.EXCLUDED, "", "", "", "", null);

    logger.log(item);

    verify(log).info(any(CharSequence.class));
  }

  @Test
  public void loggingThresholdShouldNotBeExceeded() {
    L10nReportItem item = new L10nReportItem(Severity.INFO, Type.EXCLUDED, "", "", "", "", null);

    for (int i = 0; i <= L10nValidatorLogger.THRESOLD + 5; i++) {
      logger.log(item);
    }

    verify(log, times(L10nValidatorLogger.THRESOLD)).info(any(CharSequence.class));
  }

  @Test
  public void testErrorMessageLogging() {
    logger.log(Severity.ERROR, "");

    verify(log).error(any(CharSequence.class));
  }

  @Test
  public void testWarnMessageLogging() {
    logger.log(Severity.WARN, "");

    verify(log).warn(any(CharSequence.class));
  }

  @Test
  public void testInfoMessageLogging() {
    logger.log(Severity.INFO, "");

    verify(log).info(any(CharSequence.class));
  }

}
