/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.family;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFile;
import com.googlecode.l10nmavenplugin.model.PropertiesFileUtils;
import com.googlecode.l10nmavenplugin.model.PropertyFamily;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.googlecode.l10nmavenplugin.validators.PropertiesKeyConventionValidator;
import com.googlecode.l10nmavenplugin.validators.property.HtmlValidator;

/**
 * Validator to check the coherence of HTML tag in translations of HTML resources.
 * 
 * Any tag present in the original language should usually also be present in the translations, or it would mean some meaningful info has been lost.
 * 
 * Nesting of tags is important (if styling is applied), but the order of sibling tags should not matter.
 * 
 * Values not conforming to (X)HTML will be ignored
 * 
 * @see {@link com.googlecode.l10nmavenplugin.validators.property.HtmlValidator}
 * @see {@link com.googlecode.l10nmavenplugin.validators.family.ParametricCoherenceValidator}
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class HtmlTagCoherenceValidator extends PropertiesKeyConventionValidator implements L10nValidator<PropertyFamily> {

  private DocumentBuilder dBuilder;

  public HtmlTagCoherenceValidator(L10nValidatorLogger logger, String[] htmlKeys) {
    super(logger, htmlKeys);

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    try {
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      logger.getLogger().error("Could not initialize HtmlTagCoherenceValidator", e);
    }
  }

  /**
   * Extract the XML tags, then WARN if order is not the same.
   */
  public int validate(PropertyFamily propertyFamily, List<L10nReportItem> reportItems) {
    if (dBuilder != null) {
      dBuilder.setErrorHandler(new SilentErrorHandler());
      String key = propertyFamily.getKey();

      Map<PropertiesFile, Collection<String>> tags = new HashMap<PropertiesFile, Collection<String>>();

      for (PropertiesFile propertiesFile : propertyFamily.getExistingPropertyFiles()) {
        String message = propertiesFile.getProperties().getProperty(key);

        // Wrap message into a root XHTML tag
        String xhtml = MessageFormat.format(HtmlValidator.XHTML_TEMPLATE, message);

        StringReader sr = new StringReader(xhtml);
        try {
          Document htmlDoc = dBuilder.parse(new InputSource(sr));
          // Extract the useful node from wrapping
          Collection<String> tagList = getAllTags(htmlDoc.getLastChild().getLastChild().getFirstChild().getChildNodes());
          tags.put(propertiesFile, tagList);

        } catch (SAXException e) {
          // Ignore those exceptions, will be handled by html validator
          // logger.getLogger().error("SAXException while parsing [" + message +
          // "]", e);
        } catch (IOException e) {
          logger.getLogger().error(e);
        }
      }

      Map<Collection<String>, Collection<PropertiesFile>> reverseMap = PropertiesFileUtils.reverseMap(tags);
      Collection<String> majorityKey = PropertiesFileUtils.getMajorityKey(reverseMap);
      Collection<PropertiesFile> majorityPropertiesNames = reverseMap.get(majorityKey);

      for (Entry<Collection<String>, Collection<PropertiesFile>> entry : reverseMap.entrySet()) {
        if (!entry.getKey().equals(majorityKey)) {
          Collection<PropertiesFile> faultyPropertiesFiles = entry.getValue();

          L10nReportItem reportItem = new L10nReportItem(Type.INCOHERENT_TAGS, "Incoherent usage of html tags: " + entry.getKey().toString() + " versus "
              + majorityKey.toString() + " in <" + majorityPropertiesNames + ">", faultyPropertiesFiles.toString(), key, null, null);
          reportItems.add(reportItem);
          logger.log(reportItem);
        }
      }

    }

    return 0;
  }

  /**
   * Get a list of tags ordered for a given level
   */
  private Collection<String> getAllTags(NodeList nodeList) {
    Collection<String> tags = new ArrayList<String>();

    // Get the nodes
    List<Node> elementNodes = new ArrayList<Node>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        elementNodes.add(node);
      }
    }

    // Sort the nodes by lexicographic order
    Collections.sort(elementNodes, new Comparator<Node>() {
      public int compare(Node node1, Node node2) {
        return node1.getNodeName().compareTo(node2.getNodeName());
      }
    });

    // Build the final tag list recursively
    for (Node node : elementNodes) {
      tags.add(node.getNodeName());
      if (node.hasChildNodes()) {
        tags.addAll(getAllTags(node.getChildNodes()));
      }
    }
    return tags;
  }

  /**
   * ErrorHandler that silently catch fatalError that would otherwise be logged.
   * 
   */
  private static class SilentErrorHandler implements ErrorHandler {

    public void warning(SAXParseException e) throws SAXException {
    }

    public void error(SAXParseException e) throws SAXException {
    }

    public void fatalError(SAXParseException e) throws SAXException {
    }
  }

  private static class OrderedByNameNodeList implements NodeList {

    public int getLength() {
      // TODO Auto-generated method stub
      return 0;
    }

    public Node item(int index) {
      // TODO Auto-generated method stub
      return null;
    }

  }

  public boolean shouldValidate(PropertyFamily propertyFamily) {
    return matches(propertyFamily.getKey());
  }

}
