/* Copyright (c) 2012 Romain Quinio

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.googlecode.l10nmavenplugin;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.googlecode.l10nmavenplugin.ValidateMojo;

public class ValidateMojoTest {

	private static ValidateMojo plugin;

	/**
	 * Initialize only once as the XHTML schema loading can be slow
	 * 
	 * @throws URISyntaxException
	 * @throws SAXException
	 */
	@BeforeClass
	public static void setUp() throws URISyntaxException, SAXException {
		plugin = new ValidateMojo();
		plugin.setLog(new SystemStreamLog());
	}

	@Test
	public void testFromPorpertiesResources() throws MojoExecutionException,
			MojoFailureException {
		// Look for properties from src/test/resources added to test classpath
		String propertiesDir = this.getClass().getClassLoader().getResource("")
				.getFile();
		plugin.setPropertyDir(new File(propertiesDir));

		try {
			plugin.execute();
		} catch (MojoFailureException e) {
			e.printStackTrace();
			return;
		}
		fail();
	}

	@Test
	public void testValidText() {
		Properties properties = new Properties();
		properties.put("ALLP.text.valid.1", "Some text");
		properties.put("ALLP.text.valid.2",
				"<div>Some Text on<a href=\"www.google.fr\">Google</a></div>");
		properties.put("ALLP.text.valid.3",
				"<a href=\"www.google.fr\" target=\"_blank\">Google</a>");
		properties.put("ALLP.text.valid.4", "&nbsp;&copy;&ndash;");
		properties.put("ALLP.text.valid.5", "<a href='http://example.com'>link</a>");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}

	@Test
	public void testValidJs() {
		Properties properties = new Properties();
		properties.put("ALLP.js.valid.1", "Some 'text' ");
		// properties.put("ALLP.js.valid.2", "Some \\\"text\\\" ");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}

	@Test
	public void testValidUrl() {
		Properties properties = new Properties();
		properties.put("ALLP.url.valid.1", "//www.google.com");
		properties.put("ALLP.url.valid.2", "https://www.google.com/search");
		properties
				.put("ALLP.url.valid.3",
						"http://www.google.com.au/search/misc/help-search/detect-context");
		properties
				.put("ALLP.url.valid.4", "http://www.google.au?param1=value1&param2=value2");
		properties.put("ALLP.url.valid.5", "mailto:e-mail@example.com");
		//URL should allow HTML escaping
		properties.put("ALLP.url.valid.6", "http://www.google.au?param1=value1&amp;param2=value2");
		properties.put("ALLP.url.valid.7", "//www.google.com/file.js");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}

	@Test
	public void testValidOther() {
		Properties properties = new Properties();
		properties.put("ALLP.other.resource", "Some text");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}

	@Test
	public void testUnclosedHtmlTag() {
		Properties properties = new Properties();
		properties.put("ALLP.url.invalid.1", "<br>");
		properties.put("ALLP.url.invalid.2", "<div>Some Text");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(2, nbErrors);
	}
	
	@Test
	public void testUnescapedHtmlEntity() {
		Properties properties = new Properties();
		properties.put("ALLP.text.invalid.1", "<a href='http://example.com?param1=1&param2=2'>Text<a>");
		properties.put("ALLP.text.invalid.2", "A & B");
		properties.put("ALLP.text.invalid.3", "&nbsp");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(3, nbErrors);
	}

	@Test
	public void testJsSpecialCharacters() {
		Properties properties = new Properties();
		properties.put("ALLP.js.invalid.1", "Some \"Text");
		properties.put("ALLP.js.invalid.2", "Some Text\n");
		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(2, nbErrors);
	}

	@Test
	public void testInvalidHtmlInsideJs() {
		Properties properties = new Properties();
		properties.put("ALLP.js.invalid.1", "Some error <a href=''>Text<a>");
		properties.put("ALLP.js.invalid.2", "Some Text <br>");
		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(2, nbErrors);
	}

	@Test
	public void urlWithoutProtocolShouldBeInvalid() {
		Properties properties = new Properties();
		properties.put("ALLP.url.invalid.1", "www.google.com");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(1, nbErrors);
	}

	@Test
	public void urlTrailingSpaceShouldBeInvalid() {
		Properties properties = new Properties();
		properties.put("ALLP.url.invalid.1", "//www.google.com ");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(1, nbErrors);
	}

	@Test
	public void excludedKeysShouldBeIgnored() {
		String[] excludedKeys = new String[] { "ALLP.text.excluded" };
		plugin.setExcludedKeys(excludedKeys);

		Properties properties = new Properties();
		properties.put("ALLP.text.excluded", "<div>Some text");
		properties.put("ALLP.text.excluded.longer", "<div>Some text");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}

	@Test
	public void emptyMessagesShouldBeIgnored() {
		Properties properties = new Properties();
		properties.put("ALLP.text.empty", "");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(0, nbErrors);
	}
	
	@Test
	public void testInvalidTextResource() {
		Properties properties = new Properties();
		properties.put("ALLP.title.invalid.resource1", "<div>Some text</div>");
		properties.put("ALLP.title.invalid.resource2", "Some <br />text");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		assertEquals(2, nbErrors);
	}

	@Test
	public void testOtherResource() {
		Properties properties = new Properties();
		properties.put("ALLP.other.invalid.resource1", "<div>Some text</div>");
		properties.put("ALLP.other.invalid.resource2", "Some <br />text");
		properties.put("ALLP.other.invalid.resource3", "http://example.com");

		int nbErrors = plugin
				.validateProperties(properties, "Junit.properties");
		//Check it's only warning
		assertEquals(0, nbErrors);
	}
}
