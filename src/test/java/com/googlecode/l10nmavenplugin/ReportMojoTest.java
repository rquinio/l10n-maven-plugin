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

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.maven.doxia.module.xhtml.decoration.render.RenderingContext;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.reporting.MavenReportException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;

public class ReportMojoTest {

  private static final String REPORT_FILE = "sink.html";

  private ReportMojo plugin;

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  public File reportFile;

  @Before
  public void setUp() throws RendererException, IOException {
    reportFile = new File(tmpFolder.getRoot(), REPORT_FILE);

    final Sink sink = new SiteRendererSink(new RenderingContext(tmpFolder.getRoot(), REPORT_FILE));

    plugin = new ReportMojo() {
      @Override
      public Sink getSink() {
        return sink;
      };
    };

    // Use XHTML5 as it is much faster
    plugin.setXhtmlSchema(HtmlValidator.XHTML5);

    plugin.setLog(new SystemStreamLog());
  }

  @Test
  public void reportNoPropertiesResourcesShouldGenerateNoReport() throws MavenReportException {
    plugin.setPropertyDir(new File("non-existing"));
    plugin.executeReport(Locale.ENGLISH);

    // assertTrue(reportFile.exists());
    // assertTrue(reportFile.length() > 0);
  }

  @Test
  public void reportBundleShouldGenerateAReport() throws MavenReportException {
    plugin.setPropertyDir(getFile("bundle"));
    plugin.executeReport(Locale.ENGLISH);

    // assertTrue(reportFile.exists());
    // assertTrue(reportFile.length() > 0);
  }

  @Test
  public void testReport() {
    assertNotNull(plugin.getDescription(Locale.ENGLISH));
    assertNotNull(plugin.getName(Locale.ENGLISH));
    assertNotNull(plugin.getOutputName());
  }

  private File getFile(String path) {
    return new File(this.getClass().getClassLoader().getResource(path).getFile());
  }
}
