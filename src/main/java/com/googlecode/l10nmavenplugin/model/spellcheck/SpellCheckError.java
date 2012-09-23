package com.googlecode.l10nmavenplugin.model.spellcheck;

import java.util.List;

import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellChecker;

public class SpellCheckError {
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
