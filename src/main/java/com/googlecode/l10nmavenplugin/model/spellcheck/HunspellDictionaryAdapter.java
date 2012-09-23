package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import dk.dren.hunspell.Hunspell;

public class HunspellDictionaryAdapter implements SpellDictionary {

  private final Hunspell.Dictionary internalDictionary;

  HunspellDictionaryAdapter(String baseFileName) throws FileNotFoundException, UnsupportedEncodingException {
    internalDictionary = Hunspell.getInstance().getDictionary(baseFileName);
  }

  public boolean isCorrect(String word) {
    return !internalDictionary.misspelled(word);
  }

  public List<String> getSuggestions(String word) {
    return internalDictionary.suggest(word);
  }

}
