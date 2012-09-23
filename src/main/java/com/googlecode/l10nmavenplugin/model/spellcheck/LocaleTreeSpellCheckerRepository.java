/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.PropertiesFileUtils;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.event.SpellChecker;

/**
 * Handles the hierarchy of locales and dictionaries.
 * 
 * Non-locale dependent dictionaries (typically containing proper nouns or international acronyms) are stored with the null key.
 * 
 * @author romain.quinio
 * 
 */
public class LocaleTreeSpellCheckerRepository implements LocaleSpellCheckerRepository {

  private final Map<Locale, Collection<SpellDictionary>> spellDictionaries = new HashMap<Locale, Collection<SpellDictionary>>();

  private L10nValidatorLogger logger;

  public LocaleTreeSpellCheckerRepository(L10nValidatorLogger logger) {
    this.logger = logger;
  }

  /**
   * Cache of SpellChecker
   */
  private final Map<Locale, SpellChecker> spellCheckers = new HashMap<Locale, SpellChecker>();

  private final Map<Locale, Boolean> resolvedLocales = new HashMap<Locale, Boolean>();

  public void addDictionary(Locale locale, SpellDictionary dictionary) {
    Collection<SpellDictionary> dictionaries = spellDictionaries.get(locale);
    if (dictionaries == null) {
      dictionaries = new ArrayList<SpellDictionary>();
      spellDictionaries.put(locale, dictionaries);
    }
    dictionaries.add(dictionary);
  }

  /**
   * 
   * @param locale
   * @return never null
   */
  private Collection<SpellDictionary> getDictionaries(Locale locale) {
    Collection<SpellDictionary> dictionaries = spellDictionaries.get(locale);
    if (dictionaries == null) {
      dictionaries = new ArrayList<SpellDictionary>();
    }
    return dictionaries;
  }

  /**
   * Returns the dictionaries of the local hierarchy.
   * 
   * @param locale
   * @return
   */
  private Collection<SpellDictionary> getHierarchyDictionaries(Locale locale) {
    Collection<SpellDictionary> dictionaries = new ArrayList<SpellDictionary>();

    Locale currentLocale = locale;
    while (currentLocale != null) {
      dictionaries.addAll(getDictionaries(currentLocale));
      currentLocale = PropertiesFileUtils.getParentLocale(currentLocale);
    }

    // Add root dictionaries, if there is at least another locale dependent dictionary
    if (dictionaries.size() > 0) {
      dictionaries.addAll(getDictionaries(null));
    }
    return dictionaries;
  }

  /**
   * Builds a SpellChecker to match the locale and any parent locale.
   * 
   * @param locale
   * @return hierarchical spellChecker, or null if no matching dictionary
   */
  public SpellChecker getSpellChecker(Locale locale) {
    SpellChecker spellChecker = null;
    if (resolvedLocales.get(locale) == null) {
      Collection<SpellDictionary> dictionaries = getHierarchyDictionaries(locale);
      this.logger.getLogger().info(
          "Building SpellChecker for locale <" + locale + "> : found " + dictionaries.size() + " dictionaries");
      if (dictionaries.size() > 0) {
        spellChecker = new SpellChecker();
        for (SpellDictionary dictionary : dictionaries) {
          spellChecker.addDictionary(dictionary);
        }
        spellCheckers.put(locale, spellChecker);
      }
      resolvedLocales.put(locale, true);

    } else {
      spellChecker = spellCheckers.get(locale);
    }
    return spellChecker;
  }
}
