package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.util.List;

public interface SpellDictionary {

  boolean isCorrect(String word);

  List<String> getSuggestions(String word);
}
