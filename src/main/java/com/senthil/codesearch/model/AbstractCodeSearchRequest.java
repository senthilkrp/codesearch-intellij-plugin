package com.senthil.codesearch.model;

import com.senthil.messages.Messages;
import com.senthil.codesearch.net.CodeSearchRequest;


/**
 * abstract search request implemenation.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractCodeSearchRequest implements CodeSearchRequest {

  protected int numToReturn = 20;
  protected int start;
  protected int numToScore = 100;
  protected String query = null;
  protected long requestTimeOut = 20000L;
  protected String facet = null;
  protected String fileName = null;
  protected String filePath = null;
  protected String fileType = null;
  protected static final String EXACT_MATCH_PREFIX = "^";
  protected static final String EXACT_MATCH_SUFFIX = "$";

  /**
   * User friendly description
   */
  private String description;

  @Override
  public CodeSearchRequest setNumToReturn(int numToReturn) {
    this.numToReturn = numToReturn;
    return this;
  }

  @Override
  public int getNumToReturn() {
    return numToReturn;
  }

  @Override
  public CodeSearchRequest setStart(int start) {
    this.start = start;
    return this;
  }

  @Override
  public CodeSearchRequest setRequestTimeOut(long requestTimeOut) {
    this.requestTimeOut = requestTimeOut;
    return this;
  }

  @Override
  public String getFacet() {
    return facet;
  }

  @Override
  public CodeSearchRequest setFacet(String facet) {
    if (facet != null && !facet.isEmpty()) {
      this.facet = facet;
    }
    return this;
  }

  @Override
  public CodeSearchRequest setFileName(String fileName) {
    if (fileName != null && !fileName.isEmpty()) {
      this.fileName = fileName;
    }
    return this;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public CodeSearchRequest setFilePath(String filePath) {
    if (filePath != null && !filePath.isEmpty()) {
      this.filePath = filePath;
    }
    return this;
  }

  @Override
  public CodeSearchRequest setFileType(String fileType) {
    if (fileType != null && !fileType.isEmpty() && !fileType.equalsIgnoreCase("all")) {
      this.fileType = fileType;
    }

    return this;
  }

  @Override
  public String getFileType() {
    return fileType;
  }

  @Override
  public String getQuery() {
    return query;
  }

  @Override
  public CodeSearchRequest setQuery(String query) {
    if (query != null && !query.isEmpty()) {
      this.query = query;
    }
    return this;
  }

  @Override
  public CodeSearchRequest setNumToScore(int numToScore) {
    this.numToScore = numToScore;
    return this;
  }

  @Override
  public CodeSearchRequest setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public String getDescription() {
    if (description != null) {
      return description;
    }
    return Messages.message("ui.codesearch.status.message", query,
        facet != null ? facet : Messages.message("ui.codesearch.status.message.all.multiproducts"));
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractCodeSearchRequest request = (AbstractCodeSearchRequest) o;

    if (numToReturn != request.numToReturn) {
      return false;
    }
    if (start != request.start) {
      return false;
    }
    if (numToScore != request.numToScore) {
      return false;
    }
    if (requestTimeOut != request.requestTimeOut) {
      return false;
    }
    if (query != null ? !query.equals(request.query) : request.query != null) {
      return false;
    }
    if (facet != null ? !facet.equals(request.facet) : request.facet != null) {
      return false;
    }
    if (fileName != null ? !fileName.equals(request.fileName) : request.fileName != null) {
      return false;
    }
    if (filePath != null ? !filePath.equals(request.filePath) : request.filePath != null) {
      return false;
    }
    return fileType != null ? fileType.equals(request.fileType) : request.fileType == null;
  }

  @Override
  public int hashCode() {
    int result = numToReturn;
    result = 31 * result + start;
    result = 31 * result + numToScore;
    result = 31 * result + (query != null ? query.hashCode() : 0);
    result = 31 * result + (int) (requestTimeOut ^ (requestTimeOut >>> 32));
    result = 31 * result + (facet != null ? facet.hashCode() : 0);
    result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
    result = 31 * result + (filePath != null ? filePath.hashCode() : 0);
    result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
    return result;
  }
}
