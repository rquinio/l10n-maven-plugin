/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class BundlePropertyFamilyTest {

  private static final String KEY = "key";

  private PropertyFamily propertyFamily;

  private Collection<PropertiesFile> propertiesFiles;

  private Properties bundleA;
  private Properties bundleB;
  private Properties bundleC;
  private Properties bundleD;
  private Properties bundleE;

  @Before
  public void setUp() {
    propertiesFiles = new ArrayList<PropertiesFile>();
    PropertiesFamily propertiesFamily = new BundlePropertiesFamily(propertiesFiles);
    propertyFamily = new BundlePropertyFamily("key", propertiesFamily);

    bundleA = new Properties();
    bundleB = new Properties();
    bundleC = new Properties();
    bundleD = new Properties();
    bundleE = new Properties();

    propertiesFiles.add(new BundlePropertiesFile("BundleA", bundleA));
    propertiesFiles.add(new BundlePropertiesFile("BundleB", bundleB));
    propertiesFiles.add(new BundlePropertiesFile("BundleC", bundleC));
    propertiesFiles.add(new BundlePropertiesFile("BundleD", bundleD));
    propertiesFiles.add(new BundlePropertiesFile("BundleE", bundleE));
  }

  @Test
  public void identicalValuesShouldBeGroupedIntoSingleProperty() {
    bundleA.put(KEY, "value");
    bundleB.put(KEY, "value");
    bundleC.put(KEY, "value");
    bundleD.put(KEY, "value");

    assertEquals(4, propertyFamily.getExistingPropertyFiles().size());
    assertEquals(1, propertyFamily.getMissingPropertyFiles().size());

    Collection<Property> values = propertyFamily.getValues();

    assertEquals(1, values.size());
    assertEquals(4, values.iterator().next().getContainingPropertiesFiles().size());
  }

  @Test
  public void differentValuesShouldNotBeGrouped() {
    bundleA.put(KEY, "value1");
    bundleB.put(KEY, "value2");
    bundleC.put(KEY, "value3");
    bundleD.put(KEY, "value4");

    assertEquals(4, propertyFamily.getExistingPropertyFiles().size());
    assertEquals(1, propertyFamily.getMissingPropertyFiles().size());
    Collection<Property> values = propertyFamily.getValues();
    // 4 different values
    assertEquals(4, values.size());
    assertEquals(1, values.iterator().next().getContainingPropertiesFiles().size());
  }

  @Test
  public void testMix() {
    bundleA.put(KEY, "value1");
    bundleB.put(KEY, "value2");
    bundleC.put(KEY, "value2");
    bundleD.put(KEY, "value2");

    assertEquals(4, propertyFamily.getExistingPropertyFiles().size());
    assertEquals(1, propertyFamily.getMissingPropertyFiles().size());
    Collection<Property> values = propertyFamily.getValues();
    // 2 different values
    assertEquals(2, values.size());
    // Should be sorted by frequency
    // assertEquals(3, values.iterator().next().getContainingPropertiesFiles().size());
  }
}
