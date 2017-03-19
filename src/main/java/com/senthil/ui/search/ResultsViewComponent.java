package com.senthil.ui.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.Tree;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.messages.Messages;
import com.senthil.codesearch.model.CodeSearchFacet;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.NotNull;


/**
 * UI represenation of the search results
 */
@SuppressWarnings("serial")
class ResultsViewComponent extends JBPanel {
  /**
   * Tree representation of search results.
   */
  private Tree resultsTree;

  private DefaultMutableTreeNode root;

  private static final Logger LOGGER = Logger.getInstance(ResultsViewComponent.class);

  ResultsViewComponent(Tree tree) {
    setLayout(new BorderLayout());
    setBackground(JBColor.WHITE);
    resultsTree = tree;
    if (!(resultsTree.getModel().getRoot() instanceof DefaultMutableTreeNode)) {
      return;
    }
    this.root = ((DefaultMutableTreeNode) resultsTree.getModel().getRoot());
    this.root.removeAllChildren();
    this.root.setUserObject(Messages.message("ui.codesearch.results.ready.message"));

//    Tree model that supports filter based on file types.
    FilteredTreeModel filteredModel = new FilteredTreeModel(resultsTree.getModel());
    resultsTree.setModel(filteredModel);
    ColoredTreeCellRenderer renderer = new ColoredTreeCellRenderer() {

      @Override
      public void customizeCellRenderer(@NotNull JTree tree, Object node, boolean selected, boolean expanded,
          boolean leaf, int row, boolean hasFocus) {
        Object userObj = ((DefaultMutableTreeNode) node).getUserObject();
        //The userobject will be string in case of root.
        if (userObj instanceof String) {
          append((String) userObj);
        } else if (userObj instanceof CodeSearchResult) {
          CodeSearchResult result = (CodeSearchResult) userObj;
          append(result.getFilePath());
          setIcon(FileTypeManager.getInstance()
              .getFileTypeByExtension(((CodeSearchResult) userObj).getFileExtension())
              .getIcon());
        }
      }
    };
    resultsTree.setCellRenderer(renderer);
    add(resultsTree);
  }

  /**
   * Update the results window with the list of search results
   * @param results search results.
   * @param facet CodeSearchFacet/multiproduct associated with the search request.
   */
  public void update(List<CodeSearchResult> results, CodeSearchFacet facet) {
    ApplicationManager.getApplication().invokeLater(() -> {
      markLoading();
      try {
        DefaultTreeModel model = (DefaultTreeModel) ((FilteredTreeModel) resultsTree.getModel()).getTreeModel();
        root.setUserObject(Messages.message("ui.codesearch.results.title", facet.getQuery(), facet.getName()));

        if (root.getChildCount() > 0) {
          ResultsTreeNode lastNode = (ResultsTreeNode) root.getLastChild();
          if (lastNode.isLastNode()) {
            root.remove(lastNode);
          }
        }

        for (CodeSearchResult result : results) {
          ResultsTreeNode node = new ResultsTreeNode(result);
          root.add(node);
        }
        int totalCount = facet.getMatchCount();

        if (root.getChildCount() < totalCount) {
          root.add(new ResultsTreeNode());
        }
        model.reload(root);
        resultsTree.expandPath(new TreePath(root.getPath()));
      } catch (Exception e) {
        LOGGER.debug("Exception updating results", e);
      }
    });
  }

  /**
   * Clears all the search results.
   */
  public void clear() {
    root.removeAllChildren();
    markReady();
  }

  /**
   * Returns true if the view is empty.
   * @return
   */
  public boolean isEmpty() {
    return root.getChildCount() == 0;
  }

  void markLoading() {
    root.setUserObject(Messages.message("ui.codesearch.results.loading.message"));
    reload();
  }

  /**
   * reloads the tree
   */
  private void reload() {
    if (resultsTree.getModel() instanceof FilteredTreeModel) {
      FilteredTreeModel model = ((FilteredTreeModel) resultsTree.getModel());
      if (model.getTreeModel() instanceof DefaultTreeModel) {
        ((DefaultTreeModel) model.getTreeModel()).reload(root);
      }
    }
  }

  /**
   * Prompt the user to perform a action.
   */
  private void markReady() {
    root.setUserObject(Messages.message("ui.codesearch.results.ready.message"));
    reload();
  }

  /**
   * Open the next search result in a editor.
   * Focuses if already open.
   */
  public void showNextResult() {
    TreePath selectedPath = resultsTree.getSelectionPath();
    if (root.getFirstChild() instanceof DefaultMutableTreeNode) {
      TreePath treePath = new TreePath(((DefaultMutableTreeNode) root.getFirstChild()).getPath());
      if (selectedPath != null && selectedPath.getLastPathComponent() instanceof ResultsTreeNode) {
        ResultsTreeNode selectedResult = ((ResultsTreeNode) selectedPath.getLastPathComponent());
        DefaultMutableTreeNode nextResult = selectedResult.getNextSibling();
        if (!(nextResult instanceof ResultsTreeNode) || ((ResultsTreeNode) nextResult).isLastNode()) {
          return;
        }
        treePath = new TreePath(nextResult.getPath());
      }
      resultsTree.getSelectionModel().setSelectionPath(treePath);
      resultsTree.scrollPathToVisible(treePath);
    }
  }

  /**
   * Open the prev search result in a editor.
   * Focuses if already open.
   */
  public void showPrevResult() {
    TreePath selectedResult = resultsTree.getSelectionPath();
    TreeNode prevResult = root.getLastChild();

    if (prevResult instanceof ResultsTreeNode && ((ResultsTreeNode) prevResult).isLastNode()) {
      prevResult = ((ResultsTreeNode) prevResult).getPreviousSibling();
    }

    if (selectedResult != null && selectedResult.getLastPathComponent() instanceof DefaultMutableTreeNode) {
      prevResult = ((DefaultMutableTreeNode) selectedResult.getLastPathComponent()).getPreviousSibling();
    }
    if (!(prevResult instanceof ResultsTreeNode)) {
      return;
    }
    TreePath treePath = new TreePath(((ResultsTreeNode) prevResult).getPath());
    resultsTree.setSelectionPath(treePath);
    resultsTree.scrollPathToVisible(treePath);
  }

  /**
   * Adds a file filter to the current filtered results view.
   * @param fileType
   */
  void addFilter(FileType fileType) {
    FilteredTreeModel model = ((FilteredTreeModel) resultsTree.getModel());
    model.addFilter(fileType.getDefaultExtension().toLowerCase());
    ((DefaultTreeModel) model.getTreeModel()).reload(root);
  }

  /**
   * Removes a file type from the current filtered results view.
   * @param fileType
   */
  void removeFilter(FileType fileType) {
    FilteredTreeModel model = ((FilteredTreeModel) resultsTree.getModel());
    model.removeFilter(fileType.getDefaultExtension().toLowerCase());
    ((DefaultTreeModel) model.getTreeModel()).reload(root);
  }
}
