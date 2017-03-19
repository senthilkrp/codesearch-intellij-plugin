package com.senthil.codesearch.net.github;

import com.senthil.codesearch.model.CodeSearchResult;


/**
 * Created by spanneer on 3/19/17.
 */
public class SearchCodeSearchResult extends CodeSearchResult {

  private static final SearchCodeSearcher GITHUB_SEARCHER= new SearchCodeSearcher();
  @Override
  public String getContent() {
    String content = super.getContent();
    content = content.replace("/view/", "/raw/");
    return GITHUB_SEARCHER.getContent(content);
  }
}
