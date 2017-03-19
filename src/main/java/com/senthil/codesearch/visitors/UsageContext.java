package com.senthil.codesearch.visitors;

import com.senthil.codesearch.net.CodeSearchRequest;


/**
 * Usage Context for the word /psi element under the cursor.
 */
public class UsageContext {
  /**
   * Description of the search usage.
   */
  private String description;
  /**
   * Search request constructed for this context.
   */
  private CodeSearchRequest searchRequest;

  public UsageContext(String description, CodeSearchRequest searchRequest) {
    this.description = description;
    this.searchRequest = searchRequest;
  }

  public String getDescription() {
    return description;
  }

  public CodeSearchRequest getSearchRequest() {
    return searchRequest;
  }
}
