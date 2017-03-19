package com.senthil.codesearch.model;

import com.intellij.ide.util.gotoByName.ChooseByNameItem;
import java.util.Collection;
import java.util.Collections;


/**
 * Representation of a codesearch result.
 */
public class CodeSearchResult implements ChooseByNameItem {

  private String fileName;
  private String filePath;
  private String productName;
  private String fileExtension;
  private String externalLink;
  /**
   * search result file's content.
   */
  private String content;
  /**
   * List of highlight data, one for each match in the file.
   */
  private Collection<CodeSearchHighlightData> highlightData;

  public String getFileName() {
    return fileName;
  }

  public CodeSearchResult setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public String getFilePath() {
    return filePath;
  }

  public CodeSearchResult setFilePath(String filePath) {
    this.filePath = filePath;
    return this;
  }

  public String getProductName() {
    return productName;
  }

  public CodeSearchResult setProductName(String productName) {
    this.productName = productName;
    return this;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public CodeSearchResult setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
    return this;
  }

  public String getContent() {
    return content;
  }

  public CodeSearchResult setContent(String content) {
    this.content = content;
    return this;
  }

  public Collection<CodeSearchHighlightData> getHighlightData() {
    return highlightData == null ? null : Collections.unmodifiableCollection(highlightData);
  }

  public CodeSearchResult setHighlightData(Collection<CodeSearchHighlightData> highlightData) {
    this.highlightData = highlightData;
    return this;
  }

  @Override
  public String getName() {
    return fileName;
  }

  @Override
  public String getDescription() {
    return productName;
  }


  public String getExternalLink() {
    return externalLink;
  }

  public CodeSearchResult setExternalLink(String externalLink) {
    this.externalLink = externalLink;
    return this;
  }

  @Override
  public String toString() {
    return "CodeSearchResult{" + "fileName='" + fileName + '\'' + ", filePath='" + filePath + '\'' + ", productName='"
        + productName + '\'' + ", fileExtension='" + fileExtension + '\'' + ", content='" + content + '\''
        + ", highlightData=" + highlightData + '}';
  }
}
