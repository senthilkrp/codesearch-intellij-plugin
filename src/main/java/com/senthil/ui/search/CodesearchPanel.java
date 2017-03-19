package com.senthil.ui.search;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.KeyStrokeAdapter;
import com.intellij.ui.treeStructure.Tree;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.messages.Messages;
import com.senthil.codesearch.CodeSearchHistoryManager;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import com.senthil.utils.CodeSearchUtils;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;

import static java.awt.event.KeyEvent.*;


/**
 * The main window used to display search results to the user.
 * Codesearch Panel contains Facets/products panel on the left and results panel on the right.
 * Most of the logic related to UI will be performed by this class.
 *
 * Many method in this class will be packed protected for testing purposes.
 * For all other means, they should be treated as private methods.
 */
@SuppressWarnings("serial")
public class CodesearchPanel extends JBSplitter {
  private final Project project;
  private final FacetsPanel facetsPanel;
  private final ResultsPanel resultsPanel;
  private Tree facetsTree = new Tree();
  private Tree resultsTree;
  private static final Logger LOGGER = Logger.getInstance(CodesearchPanel.class);

  private static final float DEFAULT_SPLIT_RATIO = 0.25f;

  public CodesearchPanel(Project project) {
    super(false, DEFAULT_SPLIT_RATIO);
    this.project = project;
    facetsPanel = new FacetsPanel(facetsTree);

    resultsTree = new Tree() {
      @Override
      public String getToolTipText(MouseEvent event) {
        if (getRowForLocation(event.getX(), event.getY()) == -1) {
          return null;
        }
        TreePath curPath = getPathForLocation(event.getX(), event.getY());
        if (curPath == null || curPath.getLastPathComponent() == null
            || !(curPath.getLastPathComponent() instanceof ResultsTreeNode)) {
          return null;
        }

        CodeSearchResult result = ((ResultsTreeNode) curPath.getLastPathComponent()).getSearchResult();

        return result == null ? null : result.getFilePath();
      }
    };
    resultsPanel = new ResultsPanel(resultsTree);
    setFirstComponent(facetsPanel);
    setSecondComponent(resultsPanel);

    addTreeListeners();
  }

  /**
   * Adds listener to facet and result tree
   */
  private void addTreeListeners() {
    //Listen to selection changes in facet tree.
    facetsTree.addTreeSelectionListener(e -> {
      Object component = facetsTree.getLastSelectedPathComponent();
      if (!(component instanceof FacetTreeNode)) {
        return;
      }
      FacetTreeNode node = (FacetTreeNode) component;
      if (node == facetsTree.getModel().getRoot()) {
        return;
      }
      //load results for the selected facets.
      onFacetChanged((CodeSearchFacet) node.getUserObject());
    });

    //Listen to selection changes in the results tree.
    resultsTree.addKeyListener(new KeyStrokeAdapter() {
      @Override
      public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == VK_ENTER) {
          openCurrentResult();
        }
      }
    });

    resultsTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          openCurrentResult();
        }
      }
    });
  }

  public Project getProject() {
    return project;
  }

  public void updateFacets(List<CodeSearchFacet> facets, CodeSearchRequest searchRequest) {
    facetsPanel.update(facets, searchRequest.getQuery());
    CodeSearchHistoryManager.getInstance().add(searchRequest);
  }

  private void clearResults() {
    resultsPanel.clearView();
  }

  private void loadResults(CodeSearchFacet facet) {
    loadResults(facet, 0);
  }

  /**
   * Loads search results for the facet starting from the provided index.
   * @param facet
   * @param startIndex
   */
  private void loadResults(CodeSearchFacet facet, int startIndex) {
    resultsPanel.markBusy();
    CodeSearchRequest request = SearcherFactory.createRequest();
    int resultsToReturn = Math.min(request.getNumToReturn(), facet.getMatchCount());
    request.setQuery(facet.getQuery())
        .setNumToReturn(resultsToReturn + startIndex)
        .setFacet(facet.getId())
        .setStart(startIndex);
    new Task.Backgroundable(project, request.getDescription()) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        List<CodeSearchResult> results = null;
        try {
          results = SearcherFactory.getSearcher().getResults(request).get();
          if (indicator.isCanceled()) {
            return;
          }
          if (results != null) {
            updateResults(results, facet);
          } else {
            List<CodeSearchResult> searchResults = new ArrayList<>();
            updateResults(searchResults, facet);
            CodeSearchUtils.displayErrorMessage(project);
          }
        } catch (InterruptedException | ExecutionException e) {
          CodeSearchUtils.displayErrorMessage(project);
        }
      }
    }.queue();
  }

  private void updateResults(List<CodeSearchResult> results, CodeSearchFacet facet) {
    resultsPanel.update(results, facet);
  }

  public void displayError() {
    facetsPanel.displayError();
  }

  /*
    The methods below this line are made package protected for testing purposes.
    Treat them as private methods for any other purposes.
   */

  /**
   * Checks if the search results contains content and highlight info.
   * Tries to retrieve those, if not present.
   * @param result search result
   * @param facet CodeSearchFacet corresponding to the result.
   */
  void validateResult(CodeSearchResult result, CodeSearchFacet facet) {
    if (result.getContent() == null) {
      try {
        result.setContent(SearcherFactory.getSearcher().getContents(facet.getId(), result.getFilePath()).get());
      } catch (Exception e) {
        LOGGER.debug("Error getting contents of the file", e);
      }
    }

    //We call the searcher for highlight data only if the result doesnt contain highlight info.
    if (result.getHighlightData() == null) {
      try {
        result.setHighlightData(SearcherFactory.getSearcher()
            .getHighlightData(facet.getName(), result.getFilePath(), facet.getQuery())
            .get());
      } catch (Exception e) {
        LOGGER.debug("Error getting highlight information", e);
      }
    }
  }

  void onFacetChanged(CodeSearchFacet facet) {
    clearResults();
    loadResults(facet);
  }

  /**
   * Handler for the load more results button.
   * @param node the last node of the tree ('load more')
   * @param facet CodeSearchFacet currently selected.
   */
  void handleLoadMoreResults(ResultsTreeNode node, CodeSearchFacet facet) {
    int totalResultsShown = ((DefaultMutableTreeNode) resultsTree.getModel().getRoot()).getChildCount();
    node.setUserObject(Messages.message("ui.codesearch.results.loading.message"));
    ((DefaultTreeModel) ((FilteredTreeModel) resultsTree.getModel()).getTreeModel()).reload(node);
    loadResults(facet, totalResultsShown - 1);
  }

  Tree getFacetsTree() {
    return facetsTree;
  }

  void openCurrentResult() {
    Object component = resultsTree.getLastSelectedPathComponent();

    if (!(component instanceof ResultsTreeNode)) {
      return;
    }

    ResultsTreeNode node = (ResultsTreeNode) component;
    FacetTreeNode selectedFacet = (FacetTreeNode) facetsTree.getLastSelectedPathComponent();
    CodeSearchFacet facet = (CodeSearchFacet) selectedFacet.getUserObject();

    //If its a last node, then it is the 'show more' button.
    //Load more results
    if (node.isLastNode()) {
      handleLoadMoreResults(node, facet);
    } else {
      CodeSearchResult result = (CodeSearchResult) node.getUserObject();
      validateResult(result, facet);
      CodeSearchUtils.openAndHighlightFile(project, result);
    }
  }
}
