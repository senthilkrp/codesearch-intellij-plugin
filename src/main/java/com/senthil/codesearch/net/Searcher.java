package com.senthil.codesearch.net;

import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.model.CodeSearchHighlightData;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;


public interface Searcher {
  /**
   * Returns the mp facets for the given search request.
   *
   * @param request search request.
   * @return multiproducts for the given search request.
   */
  CompletableFuture<List<CodeSearchFacet>> getFacets(@NotNull CodeSearchRequest request);

  /**
   * Returns the search resutls for the given search request.
   * @param request search requst.
   * @return a list of search results.
   */
  CompletableFuture<List<CodeSearchResult>> getResults(@NotNull CodeSearchRequest request);

  /**
   * Returns the search results for the given search request.
   * @param request search request.
   * @param delayInMilliseconds delay in milliseconds after which the task will be run.
   * @return a list of search results.
   */
  ScheduledFuture<List<CodeSearchResult>> getResults(@NotNull CodeSearchRequest request, long delayInMilliseconds);

  /**
   * Retrives the content of the given file. This Function may not be called if getResults() contains the contents as well.
   * @param productName the multi product name
   * @param filePath the path of the search result.
   * @return content of the given file as string.
   */
  CompletableFuture<String> getContents(@NotNull String productName, @NotNull String filePath);

  /**
   * Returns the highlight data for the given query for the file.
   * @param productName multiproduct name
   * @param filePath file path
   * @param query search query.
   * @return A list of highlight information, which will be used for highlighting in the editor.
   */
  CompletableFuture<List<CodeSearchHighlightData>> getHighlightData(@NotNull String productName, @NotNull String filePath, @NotNull String query);
}
