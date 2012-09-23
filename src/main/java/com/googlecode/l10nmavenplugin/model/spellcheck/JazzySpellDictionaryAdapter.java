package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionaryHashMap;

public class JazzySpellDictionaryAdapter extends SpellDictionaryHashMap implements SpellDictionary {

  public JazzySpellDictionaryAdapter(File wordList) throws FileNotFoundException, IOException {
    super(wordList);
  }

  @Override
  public boolean isCorrect(String word) {
    return super.isCorrect(word);
  }

  public List<String> getSuggestions(String word) {
    return super.getSuggestions(word, 1);
  }

}
