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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

import com.googlecode.l10nmavenplugin.model.L10nReportItem;
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
   * Unsorted list of items to be displayed
   */
  private List<L10nReportItem> reportItems = null;

  /**
   * List of blocking validations items (type error)
   */
  private int nbErrors;

  /**
   * Localized resources for report content
   */
  private ResourceBundle bundle;

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
    if (reportItems.size() > 0) {

      // TODO put a breakdown per type summary (with links to sections ?)
      paragraph(MessageFormat.format(bundle.getString("report.dashboard.text.intro"), nbErrors));

      // Need to order report items by Type
      Collections.sort(reportItems);

      int index = 1;
      Type previousType = null;
      for (L10nReportItem reportItem : reportItems) {
        if (reportItem.getItemType() != previousType) {
          if (previousType != null) { // Specific case of 1st row
            endTable();
            endSection();
            sink.horizontalRule();
          }
          startSection("[" + reportItem.getItemSeverity().toString() + "] "
              + bundle.getString(reportItem.getItemType().getTitleLocKey()));
          paragraph(bundle.getString(reportItem.getItemType().getDescriptionLocKey()));
          startTable();
          tableHeader(new String[] { "", bundle.getString("report.dashboard.messages.title.propertyKey"),
              bundle.getString("report.dashboard.messages.title.errorMessage"),
              bundle.getString("report.dashboard.messages.title.propertyValue") });
        }
        // Can't use super.tableRow, as it consumes some {}
        sink.tableRow();
        sink.tableCell();
        sink.text(String.valueOf(index));
        sink.tableCell_();
        sink.tableCell();
        sink.text(reportItem.getPropertiesKey() + "  " + reportItem.getPropertiesName());
        sink.tableCell_();
        sink.tableCell();
        sink.text(reportItem.getItemMessage());
        sink.tableCell_();
        sink.tableCell();
        if (reportItem.getPropertiesValue() != null) {
          sink.text("[" + reportItem.getPropertiesValue() + "]");
        }
        sink.tableCell_();
        sink.tableRow_();

        previousType = reportItem.getItemType();
        index++;
      }

      // Close last section
      endTable();
      endSection();

    } else { // Nothing to report
      paragraph(bundle.getString("report.dashboard.text.empty"));
    }
  }

  public void setReportItems(List<L10nReportItem> reportItems) {
    this.reportItems = reportItems;
  }

  public int getNbErrors() {
    return nbErrors;
  }

  public void setNbErrors(int nbErrors) {
    this.nbErrors = nbErrors;
  }

  public List<L10nReportItem> getReportItems() {
    return reportItems;
  }
}
