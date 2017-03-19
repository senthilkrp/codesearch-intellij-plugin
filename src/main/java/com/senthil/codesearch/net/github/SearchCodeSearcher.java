package com.senthil.codesearch.net.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.model.CodeSearchHighlightData;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.Searcher;
import com.senthil.utils.JsonParserUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;


/**
 * Created by spanneer on 3/18/17.
 */
public class SearchCodeSearcher implements Searcher {

  private static final String GITHUB_HOST = "https://searchcode.com/api/codesearch_I/";
  private static final Logger LOG = Logger.getInstance(SearchCodeSearcher.class);
  //search time out in milliseconds
  private static final int SEARCH_TIME_OUT = 10000;
  private final RequestConfig httpRequestConfig;

  private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

  private CloseableHttpClient httpClient = HttpClients.createDefault();

  private ExecutorService executor = Executors.newFixedThreadPool(5);

  public SearchCodeSearcher() {
    RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    requestConfigBuilder.setConnectionRequestTimeout(SEARCH_TIME_OUT);
    httpRequestConfig = requestConfigBuilder.build();
  }

  @Override
  public CompletableFuture<List<CodeSearchFacet>> getFacets(@NotNull CodeSearchRequest request) {
    try {
      String query = request.toString();
      URIBuilder uriBuilder = new URIBuilder(GITHUB_HOST);
      uriBuilder.setParameter("q", query);
      System.out.println("Retrieving facets for query " + uriBuilder.build());

      HttpGet httpRequest = new HttpGet(uriBuilder.build());
      httpRequest.setConfig(httpRequestConfig);

      return CompletableFuture.supplyAsync(() -> {
        CloseableHttpResponse response;
        try {
          response = httpClient.execute(httpRequest);
          JsonObject results =
              JsonParserUtil.getRootElement(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();

          if (results.getAsJsonPrimitive("total").getAsLong() == 0L) {
            return Collections.emptyList();
          }

          //we are interested only in the facets data.
          JsonArray facetsArray = results.getAsJsonArray("source_filters");
          //create a list of facets from result.
          List<CodeSearchFacet> searchFacets = new ArrayList<CodeSearchFacet>(facetsArray.size());
          for (JsonElement element : facetsArray) {
            JsonObject facet = element.getAsJsonObject();
            searchFacets.add(new CodeSearchFacet(facet.getAsJsonPrimitive("id").getAsString(),
                facet.getAsJsonPrimitive("source").getAsString(), facet.getAsJsonPrimitive("count").getAsString(),
                query));
          }
          return searchFacets;
        } catch (IOException e) {
          LOG.warn("Exception retrieving facets", e);
          return null;
        } finally {
          httpRequest.releaseConnection();
        }
      }, executor);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public CompletableFuture<List<CodeSearchResult>> getResults(@NotNull CodeSearchRequest request) {
    try {
      HttpGet httpRequest = new HttpGet(GITHUB_HOST + "?" + request.build());
      System.out.println("Retrieving facets for query " + (GITHUB_HOST + "?" + request.build()));
      httpRequest.setConfig(httpRequestConfig);

      return CompletableFuture.supplyAsync(() -> {
        CloseableHttpResponse response;
        try {
          response = httpClient.execute(httpRequest);
          JsonObject results =
              JsonParserUtil.getRootElement(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();

          String matchedTerm = results.getAsJsonPrimitive("matchterm").getAsString();

          //we are interested only in the results data.
          JsonArray facetsArray = results.getAsJsonArray("results");
          //create a list of facets from result.
          List<CodeSearchResult> searchResults = new ArrayList<>(facetsArray.size());
          for (JsonElement element : facetsArray) {
            JsonObject result = element.getAsJsonObject();
            CodeSearchResult codeSearchResult = new SearchCodeSearchResult();
            codeSearchResult.setFileName(result.getAsJsonPrimitive("filename").getAsString())
                .setFileExtension(result.getAsJsonPrimitive("language").getAsString())
                .setFilePath(result.getAsJsonPrimitive("filename").getAsString() + " in " + result.getAsJsonPrimitive("name").getAsString())
                .setProductName(result.getAsJsonPrimitive("name").getAsString())
                .setExternalLink(result.getAsJsonPrimitive("url").getAsString())
                .setContent(result.getAsJsonPrimitive("url").getAsString())
                .setHighlightData(parseHighlightInfo(matchedTerm, result.getAsJsonObject("lines")));
            searchResults.add(codeSearchResult);
          }
          return searchResults;
        } catch (IOException e) {
          LOG.warn("Exception retrieving facets", e);
          return null;
        } finally {
          httpRequest.releaseConnection();
        }
      }, executor);
    } catch (Exception e) {
      return null;
    }
  }

  private Collection<CodeSearchHighlightData> parseHighlightInfo(String query, JsonObject lines) {
    List<CodeSearchHighlightData> highlightDatas = new ArrayList<>();
    for (Map.Entry<String, JsonElement> lineNo : lines.entrySet()) {
      highlightDatas.add(new CodeSearchHighlightData(Integer.parseInt(lineNo.getKey()) -1 , query));
    }
    return highlightDatas;
  }

  @Override
  public ScheduledFuture<List<CodeSearchResult>> getResults(@NotNull CodeSearchRequest request,
      long delayInMilliseconds) {
    return scheduledExecutorService.schedule(() -> getResults(request).get(), delayInMilliseconds, TimeUnit.MILLISECONDS);
  }

  @Override
  public CompletableFuture<String> getContents(@NotNull String productName, @NotNull String filePath) {
    return null;
  }

  @Override
  public CompletableFuture<List<CodeSearchHighlightData>> getHighlightData(@NotNull String productName,
      @NotNull String filePath, @NotNull String query) {
    return null;
  }

  public String getContent(String url) {
    HttpGet httpRequest = new HttpGet(url);
    httpRequest.setConfig(httpRequestConfig);
    try {
      CloseableHttpResponse response = httpClient.execute(httpRequest);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      IOUtils.copy(response.getEntity().getContent(), outputStream);
      return outputStream.toString();
    } catch (IOException e) {
      return null;
    } finally {
      httpRequest.releaseConnection();
    }
  }
}
