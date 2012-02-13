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

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.xml.sax.SAXException;

import report.L10nReportRenderer;

import com.googlecode.l10nmavenplugin.validators.L10nReportItem;

/**
 * Creates a report on l10n Properties files validation
 * @goal report
 * @phase site
 * @author romain.quinio
 */
public class ReportMojo extends AbstractMavenReport {
  
  /**
   * Directory containing properties file to check
   * 
   * @parameter default-value="src\\main\\resources"
   * @since 1.2
   */
  private File propertyDir;
  
  /**
   * List of keys to match as text resources used from js. Default is ".js.".
   * 
   * @parameter
   * @since 1.2
   */
  private String[] jsKeys = new String[] { ".js." };

  /**
   * List of keys to match as url resources. Default is ".url.".
   * 
   * @parameter
   * @since 1.2
   */
  private String[] urlKeys = new String[] { ".url." };

  /**
   * List of keys to match as html text resources. Default is ".text.".
   * 
   * @parameter
   * @since 1.2
   */
  private String[] htmlKeys = new String[] { ".text." };

  /**
   * List of keys to match as non-html text resources. Default is ".title.".
   * 
   * @parameter
   * @since 1.2
   */
  private String[] textKeys = new String[] { ".title." };
  
  /**
   * 
   */
  private Renderer siteRenderer;
  
  /**
   * 
   */
  private L10nReportRenderer reportRenderer;
  
  /**
   * @parameter default-value="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Directory where reports will go.
   *
   * @parameter expression="${project.reporting.outputDirectory}"
   * @required
   * @readonly
   */
  private String outputDirectory;
  
  public String getDescription(Locale locale) {
    return getBundle(locale).getString("report.dashboard.description");
  }

  public String getName(Locale locale) {
    return getBundle(locale).getString("report.dashboard.name");
  }

  public String getOutputName() {
    return "l10n-report";
  }

  /**
   * Entry point for the plugin report goal
   */
  @Override
  protected void executeReport(Locale locale) throws MavenReportException {
    List<L10nReportItem> reportItems = new ArrayList<L10nReportItem>();
    int nbErrors = 0;
    
    try{
      ValidateMojo validateMojo = new ValidateMojo();
      validateMojo.setLog(getLog());
      //Exclusions should not be used in reporting
      validateMojo.setExcludedKeys(new String[] {});

      //Propagate configuration
      validateMojo.setHtmlKeys(htmlKeys);
      validateMojo.setJsKeys(jsKeys);
      validateMojo.setTextKeys(textKeys);
      validateMojo.setUrlKeys(urlKeys);

      nbErrors = validateMojo.validateProperties(propertyDir, reportItems);

    } catch(SAXException e){
      throw new MavenReportException("Could not initialize ValidateMojo", e);
    } catch(URISyntaxException e){
      throw new MavenReportException("Could not initialize ValidateMojo", e);
    } catch (MojoExecutionException e) {
      throw new MavenReportException("Could not exceute ValidateMojo", e);
    }
    
    reportRenderer = new L10nReportRenderer(getSink(),getBundle(locale));
    reportRenderer.setReportItems(reportItems);
    reportRenderer.setNbErrors(nbErrors);
    reportRenderer.render();
  }

  @Override
  protected String getOutputDirectory() {
    return outputDirectory;
  }

  @Override
  protected MavenProject getProject() {
    return project;
  }

  @Override
  protected Renderer getSiteRenderer() {
    return siteRenderer;
  }
  
  private ResourceBundle getBundle(Locale locale){
      return ResourceBundle.getBundle("l10n-report", locale, this.getClass().getClassLoader());
  }
}
