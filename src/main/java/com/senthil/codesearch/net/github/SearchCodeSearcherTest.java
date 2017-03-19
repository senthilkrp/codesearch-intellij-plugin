package com.senthil.codesearch.net.github;

import org.junit.Test;


/**
 * Created by spanneer on 3/18/17.
 */
public class SearchCodeSearcherTest {

  @Test
  public void testGetFacets() throws Exception {
    System.out.println(new SearchCodeSearcher().getFacets(new SearchCodeRequest().setQuery("collection")).get());
  }
}
