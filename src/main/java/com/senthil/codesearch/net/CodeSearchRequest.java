package com.senthil.codesearch.net;

/**
 * A search request interface that can be passed on to the searcher.
 */
public interface CodeSearchRequest {

  /**
   *
   * @param numToReturn the number of search results to return
   * @return this object
   */
  CodeSearchRequest setNumToReturn(int numToReturn);

  /**
   *
   * @return the number of search results to return
   */
  int getNumToReturn();


  /**
   * Sets the start index. The results from 0 to start-1 will be ignored.
   * @param start The offset to begin the search
   * @return this object
   */
  CodeSearchRequest setStart(int start);

  /**
   * Sets the time out for the CodeSearch's brokers. This is the time out for the codesearch server and not the http request.
   * @param requestTimeOut request time out in milliseconds.
   * @return this object
   */
  CodeSearchRequest setRequestTimeOut(long requestTimeOut);

  /**
   *
   * @return The multiproduct name
   */
  String getFacet();

  /**
   * Sets the multipriduct name
   * @param facet multiproduct name
   * @return this object
   */
  CodeSearchRequest setFacet(String facet);

  /**
   *
   * @param fileName The search hit's file name
   * @return this object
   */
  CodeSearchRequest setFileName(String fileName);

  /**
   * @return the file name paramter
   */

  String getFileName();

  /**
   *
   * @param filePath The search hit's file path
   * @return this object
   */
  CodeSearchRequest setFilePath(String filePath);

  /**
   *
   * @param fileType The search hit's file type (extension)
   * @return this object
   */
  CodeSearchRequest setFileType(String fileType);

  String getFileType();

  /**
   *
   * @return the raw search query
   */
  String getQuery();

  /**
   * sets the search string
   * @param query the raw search query
   * @return this object
   */
  CodeSearchRequest setQuery(String query);

  /**
   *
   * @param numToScore Number of files to be considered for scoring.
   * @return this oject
   */
  CodeSearchRequest setNumToScore(int numToScore);

  /**
   * sets user friendly description for this search. This description may be displayed to the user during the search operation.
   * @param description
   * @return
   */
  CodeSearchRequest setDescription(String description);

  /**
   * returns user friendly description for this search.
   */
  String getDescription();

  /**
   * Builds this search request and returns an object that is compatible with its searcher.
   * @return search request representation in a format understood by its searcher.
   */
  <T> T build();

  /**
   * copies the current object
   * @return copied object.
   */
  CodeSearchRequest copy();

  CodeSearchRequest setContext(SearchContext context);



  enum SearchContext {
    CLASS_DECLARATION, CLASS_USAGE, SUPER_CLASS, IMPORTS, METHOD_USAGE, METHOD_DECLARATION, PACKAGE;
  }

}
