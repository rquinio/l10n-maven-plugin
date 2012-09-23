package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.util.Locale;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.event.SpellChecker;

public interface LocaleSpellCheckerRepository {

  void addDictionary(Locale locale, SpellDictionary dictionary);

  SpellChecker getSpellChecker(Locale locale);
}
