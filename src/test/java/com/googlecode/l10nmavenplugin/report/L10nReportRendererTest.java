/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.report;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

public class L10nReportRendererTest {

  private L10nReportRenderer renderer;

  private List<L10nReportItem> reportItems;

  @Before
  public void setUp() {
    ResourceBundle bundle = ResourceBundle.getBundle("l10n-report");
    Sink sink = new SiteRendererSink(null);
    renderer = new L10nReportRenderer(sink, bundle);
    reportItems = new ArrayList<L10nReportItem>();
    renderer.setReportItems(reportItems);
  }

  @Test
  public void testRenderNoReportItems() {
    List<L10nReportItem> reportItems = new ArrayList<L10nReportItem>();
    renderer.setReportItems(reportItems);
    renderer.render();
  }

  @Test
  public void testRenderOneSectionOneItem() {
    L10nReportItem item = new L10nReportItem(Severity.ERROR, Type.HTML_VALIDATION, "Some text", "test.properties",
        "ALLP.text.invalid", "<a>", "");

    reportItems.add(item);
    renderer.setReportItems(reportItems);
    renderer.render();
  }

  @Test
  public void testRenderOneSectionMultipleItems() {
    renderer.setNbErrors(4);
    L10nReportItem item = new L10nReportItem(Severity.ERROR, Type.HTML_VALIDATION, "Some text", "test.properties",
        "ALLP.text.invalid", "<a>", "");

    reportItems.add(item);
    reportItems.add(item);
    reportItems.add(item);
    reportItems.add(item);
    renderer.setReportItems(reportItems);
    renderer.render();
  }

  @Test
  public void testRenderMultipleSections() {
    renderer.setNbErrors(3);
    L10nReportItem item1 = new L10nReportItem(Severity.ERROR, Type.HTML_VALIDATION, "Some text", "test.properties",
        "ALLP.text.invalid", "<a>", "");
    L10nReportItem item2 = new L10nReportItem(Severity.ERROR, Type.JS_DOUBLE_QUOTED_VALIDATION, "Some text", "test.properties",
        "ALLP.js.invalid", "<a>", "");
    L10nReportItem item3 = new L10nReportItem(Severity.ERROR, Type.URL_VALIDATION, "Some text", "test.properties",
        "ALLP.url.invalid", "<a>", "");
    reportItems.add(item1);
    reportItems.add(item2);
    reportItems.add(item3);

    renderer.setReportItems(reportItems);
    renderer.render();
  }
}
