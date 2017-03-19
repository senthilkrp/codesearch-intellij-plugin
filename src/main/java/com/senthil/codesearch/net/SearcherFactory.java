package com.senthil.codesearch.net;

import com.senthil.codesearch.net.github.SearchCodeRequest;
import com.senthil.codesearch.net.github.SearchCodeSearcher;


/**
 * Factory class to create search request and to return the instance of a searcher
 *
 */
public class SearcherFactory {

  private SearcherFactory() {

  }

  /**
   *
   * @return a reference to the searcher
   */
  public static Searcher getSearcher() {
    return new SearchCodeSearcher();
  }

  /**
   * Creates and returns a search request that can be used with the searcher returned from getSearcher() method.
   * @return Search Request.
   */
  public static CodeSearchRequest createRequest() {
    return new SearchCodeRequest();
  }
}
