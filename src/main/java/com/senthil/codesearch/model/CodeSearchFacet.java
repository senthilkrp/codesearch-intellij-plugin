package com.senthil.codesearch.model;

import com.senthil.messages.Messages;


/**
 * Representation of a multiproduct facet.
 */
public class CodeSearchFacet {

  private final String id;

  /**
   * name of the multi product
   */
  private final String name;
  /**
   * no of search results in this multi product.
   */
  private final int matchCount;
  /**
   * The searched query string
   */
  private final String query;

  public CodeSearchFacet(String id, String name, int matchCount, String query) {
    this.id = id;
    this.name = name;
    this.matchCount = matchCount;
    this.query = query;
  }

  public CodeSearchFacet(String name, String matchCount, String query) {
    this(name, name, Integer.parseInt(matchCount), query);
  }

  public CodeSearchFacet(String id, String name, String matchCount, String query) {
    this(id, name, Integer.parseInt(matchCount), query);
  }

  public String getName() {
    return name;
  }

  public int getMatchCount() {
    return matchCount;
  }

  public String getQuery() {
    return query;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return Messages.message("ui.codesearch.facets.display.format", name, matchCount);
  }
}
