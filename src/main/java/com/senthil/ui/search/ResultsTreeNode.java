package com.senthil.ui.search;

import com.senthil.messages.Messages;
import com.senthil.codesearch.model.CodeSearchResult;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * UI Represent of a search result.
 */
@SuppressWarnings("serial")
class ResultsTreeNode extends DefaultMutableTreeNode {
  /**
   * If its the last node in the results tree - the 'Load more..' button.
   */
  private boolean lastNode;

  CodeSearchResult searchResult;

  public ResultsTreeNode(CodeSearchResult searchResult) {
    super(searchResult);
    this.searchResult = searchResult;
  }

  ResultsTreeNode() {
    super(Messages.message("ui.codesearch.results.load.more"));
    lastNode = true;
  }

  public boolean isLastNode() {
    return lastNode;
  }

  public CodeSearchResult getSearchResult() {
    return searchResult;
  }

  @Override
  public String toString() {
    return searchResult == null ? "" : searchResult.getFileName();
  }
}

