/*******************************************************************************
 * Copyright (c) 2012 Romain Quinio (http://code.google.com/p/l10n-maven-plugin)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.googlecode.l10nmavenplugin.validators.property;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;

import com.googlecode.l10nmavenplugin.log.L10nValidatorLogger;
import com.googlecode.l10nmavenplugin.model.L10nReportItem;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Severity;
import com.googlecode.l10nmavenplugin.model.L10nReportItem.Type;
import com.googlecode.l10nmavenplugin.model.PropertiesFileUtils;
import com.googlecode.l10nmavenplugin.model.Property;
import com.googlecode.l10nmavenplugin.validators.AbstractL10nValidator;
import com.googlecode.l10nmavenplugin.validators.L10nValidator;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

/**
 * Validator to check for spelling mistakes based on a dictionary of words.
 * 
 * Implementation is based on Jazzy {@link SpellChecker} based on one or multiple {@link SpellDictionary}. Unfortunately
 * dictionaries are not uploaded into maven, so they have to provided via file system...
 * 
 * For a given {@link java.util.Locale} from a {@link com.googlecode.l10nmavenplugin.model.PropertiesFile}, all the dictionary
 * associated to the locale or any "parent" locale are used, including any non-locale dependent dictionary at root.
 * 
 * @since 1.4
 * @author romain.quinio
 * 
 */
public class SpellCheckValidator extends AbstractL10nValidator implements L10nValidator<Property> {

  private SpellCheckerLocaleRepository spellCheckerLocaleRepository;

  /**
   * Initialize by loading dictionaries following {@link Locale} naming convention
   * 
   * @param logger
   * @param directory
   *          dictionaries location
   */
  public SpellCheckValidator(L10nValidatorLogger logger, File directory) {
    super(logger);
    spellCheckerLocaleRepository = new SpellCheckerLocaleRepository(logger);

    if (directory != null) {
      logger.getLogger().info("Looking for .dic files in: " + directory.getAbsolutePath());
      File[] files = directory.listFiles((FilenameFilter) new SuffixFileFilter(".dic"));
      if (files == null || files.length == 0) {
        logger.getLogger().warn(
            "No dictionary file under folder " + directory.getAbsolutePath() + ". Skipping spellcheck validation.");

      } else {
        // Load each dictionary, using file name to detect associated locale
        for (File file : files) {
          try {
            String fileName = FilenameUtils.getBaseName(file.getName());
            String localePart = null;
            String[] parts = fileName.split("_", 2);
            if (parts[0].length() == 2) {
              localePart = fileName;
            } else if (parts.length == 2) {
              localePart = parts[1];
            }
            Locale locale = PropertiesFileUtils.getLocale(localePart);
            logger.getLogger().info("Loading file <" + file.getName() + "> associated to locale <" + locale + ">");

            SpellDictionary dictionary = new SpellDictionaryHashMap(file);
            spellCheckerLocaleRepository.addDictionary(locale, dictionary);

          } catch (IOException e) {
            logger.getLogger().error(e);
          }
        }
      }
    } else {
      logger.getLogger().warn("No dictionary folder provided, skipping spellcheck validation.");
    }
  }

  /**
   * WARN in case of spellcheck error using property locale.
   */
  public int validate(Property property, List<L10nReportItem> reportItems) {
    Locale locale = property.getLocale();
    if (locale == null) {
      // Case of root bundle
      locale = Locale.ENGLISH;
    }
    SpellChecker spellChecker = spellCheckerLocaleRepository.getSpellChecker(locale);

    if (spellChecker != null) {
      ListSpellCheckErrorListener listener = new ListSpellCheckErrorListener(spellChecker);
      spellChecker.addSpellCheckListener(listener);

      String message = property.getMessage();
      spellChecker.checkSpelling(new StringWordTokenizer(message));

      Collection<SpellCheckError> errors = listener.getSpellCheckErrors();

      // The message with errors replaced by suggestions
      String correction = message;

      // Start from last errors, so that error position remains valid
      SpellCheckError[] errs = errors.toArray(new SpellCheckError[errors.size()]);
      for (int i = errs.length - 1; i >= 0; i--) {
        SpellCheckError error = errs[i];
        if (error.getSuggestion() != null) {
          int pos = error.getPosition();
          correction = StringUtils.overlay(correction, error.getSuggestion(), pos, pos + error.getError().length());
        }
      }

      if (errors.size() > 0) {
        StringBuffer sb = new StringBuffer();
        sb.append("Spellcheck error on word(s): ").append(errors.toString()).append(" and locale <").append(locale).append(">.");
        if (correction != null) {
          sb.append(" Suggested correction: [").append(correction).append("]");
        }

        L10nReportItem reportItem = new L10nReportItem(Severity.WARN, Type.SPELLCHECK, sb.toString(), property, null);
        reportItems.add(reportItem);
        logger.log(reportItem);
      }

      spellChecker.removeSpellCheckListener(listener);
    }
    return 0;
  }

  /**
   * Listen to keep track of all spellCheck errors
   * 
   */
  private static class ListSpellCheckErrorListener implements SpellCheckListener {

    private Collection<SpellCheckError> spellCheckErrors = new ArrayList<SpellCheckError>();

    private SpellChecker spellChecker;

    public ListSpellCheckErrorListener(SpellChecker spellChecker) {
      this.spellChecker = spellChecker;
    }

    public void spellingError(SpellCheckEvent event) {
      spellCheckErrors.add(new SpellCheckError(event, spellChecker));
    }

    public Collection<SpellCheckError> getSpellCheckErrors() {
      return spellCheckErrors;
    };
  }

  /**
   * Wrapper around SpellCheckEvent
   * 
   */
  private static class SpellCheckError {
    private String error;
    private String suggestion;
    private int position;

    public SpellCheckError(SpellCheckEvent event, SpellChecker spellChecker) {
      this.error = event.getInvalidWord();
      this.position = event.getWordContextPosition();

      // List<String> suggestions = event.getSuggestions();
      List<Word> suggestions = spellChecker.getSuggestions(error, 1);

      if (suggestions != null && suggestions.size() > 0) {
        String firstSuggestion = suggestions.get(0).getWord();
        if (!firstSuggestion.equals(this.error)) {
          this.suggestion = firstSuggestion;
        }
      }
    }

    /**
     * @return the error
     */
    public String getError() {
      return error;
    }

    /**
     * @return the suggestion
     */
    public String getSuggestion() {
      return suggestion;
    }

    /**
     * @return the position
     */
    public int getPosition() {
      return position;
    }

    @Override
    public String toString() {
      return error;
      // StringBuffer sb = new StringBuffer();
      // sb.append(error);
      // if (suggestion != null) {
      // sb.append(" (suggestion: ").append(suggestion).append(")");
      // }
      // return sb.toString();
    }
  }

  /**
   * Handles the hierarchy of loacles and dictionaries.
   * 
   * Non-locale dependent dictionaries (typically containing proper nouns or international acronyms) are stored with the null key.
   * 
   * @author romain.quinio
   * 
   */
  private static class SpellCheckerLocaleRepository {

    private final Map<Locale, Collection<SpellDictionary>> spellDictionaries = new HashMap<Locale, Collection<SpellDictionary>>();

    private L10nValidatorLogger logger;

    public SpellCheckerLocaleRepository(L10nValidatorLogger logger) {
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

}
