package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.util.List;

import com.swabunga.spell.event.WordTokenizer;

public interface SpellChecker {

  void addDictionary(SpellDictionary dictionary);

  List<SpellCheckError> checkSpelling(WordTokenizer tokenizer);
}
