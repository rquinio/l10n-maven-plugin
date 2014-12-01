/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidatorTest;

/**
 * Unit tests for {@link ValidateMojo}
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateMojoTest extends AbstractL10nValidatorTest<File> {

  /**
   * Mocked mojo to test failures/Exception handling and skip/ignore flags
   */
  private ValidateMojo failingMojo;

  private ValidateMojo plugin;

  @Mock
  private L10nValidationConfiguration configuration;

  @Override
  @Before
  public void setUp() {
    super.setUp();

    plugin = new ValidateMojo();
    plugin.setLog(log);

    failingMojo = new ValidateMojo() {
      @Override
      public int validate(File directory, List<L10nReportItem> reportItems) throws MojoExecutionException {
        return 1;
      }
    };
  }

  @Test
  public void allConfigurationGettersShouldBeCalled() {
    plugin = new ValidateMojo(configuration);

    List<Method> invokedMethods = new ArrayList<Method>();
    for (Invocation invocation : mockingDetails(configuration).getInvocations()) {
      invokedMethods.add(invocation.getMethod());
    }

    for (Method method : L10nValidationConfiguration.class.getDeclaredMethods()) {
      if (method.getName().startsWith("get")) {
        assertThat("A getter was not called", invokedMethods, hasItem(method));
      }
    }
  }

  @Test
  public void testSuccessfulExecution() throws MojoExecutionException, MojoFailureException {
    plugin.setDirectoryValidator(new AlwaysSucceedingValidator<File>());

    plugin.executeInternal();

    verify(log, times(1)).info(any(CharSequence.class));
  }

  @Test
  public void testSkipExecution() throws MojoExecutionException, MojoFailureException {
    failingMojo.setSkip(true);

    failingMojo.execute();

    assertTrue(true);
  }

  @Test(expected = MojoFailureException.class)
  public void errorsShouldTriggerMojoFailureException() throws MojoExecutionException, MojoFailureException {
    failingMojo.executeInternal();
  }

  @Test
  public void testIgnoreExecutionFailure() throws MojoExecutionException, MojoFailureException {
    failingMojo.setIgnoreFailure(true);

    failingMojo.executeInternal();

    assertTrue(true);
  }

  @Test(expected = MojoExecutionException.class)
  public void validatorExceptionShouldBeWrapped() throws MojoExecutionException, IOException {
    plugin.setDirectoryValidator(new AlwaysRefusingValidator<File>());
    plugin.setPropertyDir(new File("non-existing"));

    plugin.validate(null, items);
  }
}
