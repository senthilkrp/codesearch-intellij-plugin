package com.senthil.codesearch.model;

/**
 * Highlight information for a search result.
 * Not all codesearch servers provide offset currently. So we will search for the matched string in the given line number.
 */
public class CodeSearchHighlightData {

  /**
   * line number in which a match occured.
   */
  private final int lineNumber;
  /**
   * the matched search string. This need not be the same as input search query.
   */
  private final String matchedString;

  public CodeSearchHighlightData(int lineNumber, String matchedString) {
    this.lineNumber = lineNumber;
    this.matchedString = matchedString;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getMatchedString() {
    return matchedString;
  }

}
