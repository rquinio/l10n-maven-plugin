/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package report;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

import com.googlecode.l10nmavenplugin.validators.L10nReportItem;
import com.googlecode.l10nmavenplugin.validators.L10nReportItem.Type;

/**
 * Renderer for l10n reports
 * 
 * @author romain.quinio
 */
public class L10nReportRenderer extends AbstractMavenReportRenderer {

  private List<L10nReportItem> reportItems;
  private int nbErrors;

  private ResourceBundle bundle;

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
    // Need to order by Type
    Collections.sort(reportItems);

    paragraph(MessageFormat.format(bundle.getString("report.dashboard.text"), nbErrors));

    int index = 1;
    Type previousType = null;
    for (L10nReportItem reportItem : reportItems) {
      if (reportItem.getItemType() != previousType) {
        if (previousType != null) { // Specific case of 1st row
          endTable();
          endSection();
        }
        startSection("[" + reportItem.getItemSeverity().toString() + "] "
            + bundle.getString(reportItem.getItemType().getTitleLocKey()));
        paragraph(bundle.getString(reportItem.getItemType().getDescriptionLocKey()));
        startTable();
        tableHeader(new String[] { "", "Property key", "Error", "Property value" });
      }
      tableRow(new String[] { String.valueOf(index), reportItem.getPropertiesKey() + "  " + reportItem.getPropertiesName(),
          reportItem.getItemMessage(), "[" + reportItem.getPropertiesValue() + "]" });
      previousType = reportItem.getItemType();
      index++;
    }
    // Close last section
    endTable();
    endSection();
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
