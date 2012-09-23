/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
    L10nReportItem item = new L10nReportItem(Type.JS_DOUBLE_QUOTED_VALIDATION, "", "", "", "", null);

    logger.log(item);

    verify(log).error(any(CharSequence.class));
  }

  @Test
  public void testWarnLogging() {
    L10nReportItem item = new L10nReportItem(Type.ESCAPED_QUOTE_WITHOUT_PARAMETER, "", "", "", "", null);

    logger.log(item);

    verify(log).warn(any(CharSequence.class));
  }

  @Test
  public void testInfoLogging() {
    L10nReportItem item = new L10nReportItem(Type.EXCLUDED, "", "", "", "", null);

    logger.log(item);

    verify(log).info(any(CharSequence.class));
  }

  @Test
  public void loggingThresholdShouldNotBeExceeded() {
    L10nReportItem item = new L10nReportItem(Type.EXCLUDED, "", "", "", "", null);

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
