package com.senthil.codesearch;

import com.senthil.codesearch.net.CodeSearchRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;


/**
 * Maintains a history of the code search requests.
 */
public class CodeSearchHistoryManager {

    /**
     * Singleton instance
     */
    private final static CodeSearchHistoryManager INSTANCE = new CodeSearchHistoryManager();

    /**
     * List of recent search requests
     */
    private LinkedHashSet<CodeSearchRequest> recentQueries = new LinkedHashSet<>();

    private CodeSearchHistoryManager() {

    }

    public static CodeSearchHistoryManager getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a search request to history.
     *
     * @param request search request.
     */
    public void add(CodeSearchRequest request) {
        recentQueries.remove(request);
        recentQueries.add(request);
    }

    /**
     * @return list of recent queries.
     */
    public Collection<CodeSearchRequest> getRecentQueries() {
        return Collections.unmodifiableCollection(recentQueries);
    }

    /**
     * clears the current history
     */
    public void clear() {
        recentQueries.clear();
    }
}

