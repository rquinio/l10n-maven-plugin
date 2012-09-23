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
