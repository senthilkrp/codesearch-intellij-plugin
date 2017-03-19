package com.senthil.codesearch;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.Processor;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;


/**
 * File provider for codesearch file search.
 * Files can be searched in the format {filename} in {multiproduct}
 */
public class CodesearchFileProvider implements ChooseByNameItemProvider {

  private static final Logger LOG = Logger.getInstance(CodesearchFileProvider.class);
  private static final String DELIMITER = " in ";
  //timeout for results future in ms.
  private static final long RESULTS_TIMEOUT = 100;
  //the duration of the delay in ms.
  private static final long REQUEST_DELAY = 500;

  @NotNull
  @Override
  public List<String> filterNames(@NotNull ChooseByNameBase base, @NotNull String[] names, @NotNull String pattern) {
    return Collections.emptyList();
  }

  @Override
  public boolean filterElements(@NotNull ChooseByNameBase base, @NotNull String pattern, boolean everywhere,
      @NotNull ProgressIndicator cancelled, @NotNull Processor<Object> consumer) {
    LOG.debug("Searching for ", pattern);

    CodeSearchRequest request = SearcherFactory.createRequest();

    //separate the filename and product name (eg. rewriter in seas-articles)
    String[] tokens = pattern.split(DELIMITER);
    if (tokens.length > 0) {
      pattern = tokens[0];
      if (tokens.length > 1 && tokens[1] != null && !tokens[1].trim().isEmpty()) {
        request.setFacet(tokens[1]);
      }
    }
    request.setFileName(pattern);

    if (!everywhere) {
      request.setFileType(JavaFileType.DEFAULT_EXTENSION);
    }

    if (cancelled.isCanceled()) {
      LOG.debug("Cancelled before http request: ", pattern);
      return false;
    }

//    delaying the search because the user may still be typing.
    ScheduledFuture<List<CodeSearchResult>> resultsFuture = SearcherFactory.getSearcher().getResults(request, REQUEST_DELAY);

    List<CodeSearchResult> results;
    while (true) {
      try {
//      for every 100 ms check if we got the results. Also check if the request is already cancelled by the user.
//      Cancel the task if the user cancelled the operation.
        results = resultsFuture.get(RESULTS_TIMEOUT, TimeUnit.MILLISECONDS);
        break;
      } catch (InterruptedException | ExecutionException | TimeoutException e) {
        LOG.debug("Interrupted: " + pattern);
        if (cancelled.isCanceled()) {
          resultsFuture.cancel(true);
          LOG.debug("Cancelled after interrupt: ", pattern);
          return false;
        }
      }
    }

    if (results == null) {
      return false;
    }

    results.forEach(consumer::process);
    return true;
  }

}
