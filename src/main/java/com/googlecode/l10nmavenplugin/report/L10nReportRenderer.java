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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.util.HtmlTools;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;

/**
 * Renderer for l10n reports, using a simple layout.
 * 
 * The items should be displayed per type/severity (errors, warning, info), cf {@link L10nReportItem#compareTo(L10nReportItem)}.
 * 
 * @since 1.2
 * @author romain.quinio
 */
public class L10nReportRenderer extends AbstractMavenReportRenderer {

  /**
   * List of blocking validations items (type error)
   */
  private int nbErrors;

  /**
   * Localized resources for report content
   */
  private final ResourceBundle bundle;

  /**
   * Map by types of items to be displayed
   */
  private Map<Type, List<L10nReportItem>> reportItemsByType;

  /**
   * index of the item
   */
  private int index = 1;

  /**
   * 
   * @param sink
   *          report document being rendered
   * @param bundle
   *          the localized resources for report content
   */
  public L10nReportRenderer(Sink sink, ResourceBundle bundle) {
    super(sink);
    this.bundle = bundle;
  }

  @Override
  public String getTitle() {
    return "L10n properties validation report";
  }

  @Override
  protected void renderBody() {
    if (reportItemsByType.size() > 0) {

      renderReportSummary();

      renderReportContent();

    } else { // Nothing to report
      paragraph(bundle.getString("report.dashboard.text.empty"));
    }
  }

  private void renderReportSummary() {
    sink.anchor("summary");
    sink.anchor_();

    // Build a summary, with links to sections
    paragraph(MessageFormat.format(bundle.getString("report.dashboard.text.intro"), nbErrors));

    sink.list();
    for (Entry<Type, List<L10nReportItem>> entry : reportItemsByType.entrySet()) {
      renderReportSummaryEntry(entry.getKey(), entry.getValue().size());
    }
    sink.list_();
  }

  private void renderReportSummaryEntry(Type type, int number) {
    sink.listItem();
    font(type.getSeverity());
    link('#' + HtmlTools.encodeId(type.toString()), bundle.getString(type.getTitleLocKey()));
    sink.text(" " + String.valueOf(number));
    font_(type.getSeverity());
    sink.listItem_();
  }

  private void font(Severity severity) {
    switch (severity) {
    case ERROR:
      sink.bold();
      break;
    case WARN:
      break;
    case INFO:
      sink.italic();
      break;
    }
  }

  private void font_(Severity severity) {
    switch (severity) {
    case ERROR:
      sink.bold_();
      break;
    case WARN:
      break;
    case INFO:
      sink.italic_();
      break;
    }
  }

  private void renderReportContent() {
    for (Entry<Type, List<L10nReportItem>> entry : reportItemsByType.entrySet()) {
      Type itemType = entry.getKey();
      List<L10nReportItem> reportItems = entry.getValue();

      // Display 1 section per type
      renderL10nReportItemType(itemType, reportItems);
    }
  }

  private void renderL10nReportItemType(Type itemType, List<L10nReportItem> reportItems) {
    sink.anchor(HtmlTools.encodeId(itemType.toString()));
    sink.anchor_();

    startSection("[" + itemType.getSeverity().toString() + "] " + bundle.getString(itemType.getTitleLocKey()));
    paragraph(bundle.getString(itemType.getDescriptionLocKey()));
    startTable();
    tableHeader(new String[] { "", bundle.getString("report.dashboard.messages.title.propertyKey"),
        bundle.getString("report.dashboard.messages.title.errorMessage"), bundle.getString("report.dashboard.messages.title.propertyValue") });

    for (L10nReportItem reportItem : reportItems) {
      renderL10nReportItem(reportItem);
    }

    endTable();
    endSection();
    sink.horizontalRule();
    link("#summary", bundle.getString("report.dashboard.title.up"));
  }

  private void renderL10nReportItem(L10nReportItem reportItem) {
    // Can't use super.tableRow, as it consumes some {}
    sink.tableRow();
    rendreCell(String.valueOf(index));
    rendreCell(reportItem.getPropertiesKey() + "  " + reportItem.getPropertiesName());
    rendreCell(reportItem.getItemMessage());
    rendreCell(("[" + reportItem.getPropertiesValue() + "]"));
    sink.tableRow_();

    index++;
  }

  private void rendreCell(String text) {
    sink.tableCell();
    if (text != null) {
      sink.text(text);
    }
    sink.tableCell_();
  }

  public void setReportItems(List<L10nReportItem> reportItems) {
    this.reportItemsByType = L10nReportItem.byType(reportItems);
  }

  public int getNbErrors() {
    return nbErrors;
  }

  public void setNbErrors(int nbErrors) {
    this.nbErrors = nbErrors;
  }
}
