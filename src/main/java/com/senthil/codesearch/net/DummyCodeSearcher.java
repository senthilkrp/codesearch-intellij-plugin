package com.senthil.codesearch.net;

import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.model.CodeSearchHighlightData;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;


/**
 * Dummy searcher that returns a dummy result 'immediately'
 * This is required for testing codesearch results window.
 */
public class DummyCodeSearcher implements Searcher {

  private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

  @Override
  public CompletableFuture<List<CodeSearchFacet>> getFacets(CodeSearchRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      CodeSearchFacet facet = new CodeSearchFacet("dummy-mp", "dummy-mp", 100, request.getQuery());
      return Collections.singletonList(facet);
    });
  }

  @Override
  public CompletableFuture<List<CodeSearchResult>> getResults(CodeSearchRequest request) {
    return CompletableFuture.supplyAsync(() -> {
      CodeSearchResult result = new CodeSearchResult().setFileName("DummyFile.java")
          .setFileExtension("java")
          .setFilePath("dummy-mp/src/java/DummyFile.java")
          .setProductName(request.getFacet() == null ? "Dummy" : request.getFacet())
          .setHighlightData(null)
          .setContent("dummy content");
      return Collections.singletonList(result);
    });
  }

  @Override
  public ScheduledFuture<List<CodeSearchResult>> getResults(@NotNull CodeSearchRequest request, long delayInMilliseconds) {
    CodeSearchResult result = new CodeSearchResult().setFileName(request.getFileName() == null ? "DummyFile.java" : request.getFileName())
        .setFileExtension("java")
        .setFilePath("dummy-mp/src/java/DummyFile.java")
        .setProductName(request.getFacet() == null ? "Dummy" : request.getFacet())
        .setHighlightData(null)
        .setContent("dummy content");
    return scheduledExecutorService.schedule(() -> Collections.singletonList(result), delayInMilliseconds, TimeUnit.MILLISECONDS);

  }

  @Override
  public CompletableFuture<String> getContents(String productName, String filePath) {
    return CompletableFuture.supplyAsync(() -> "dummyContent");
  }

  @Override
  public CompletableFuture<List<CodeSearchHighlightData>> getHighlightData(String productName, String filePath, String query) {
    return CompletableFuture.supplyAsync(Collections::emptyList);
  }
}
