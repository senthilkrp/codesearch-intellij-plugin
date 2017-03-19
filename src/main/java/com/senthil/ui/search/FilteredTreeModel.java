package com.senthil.ui.search;

import com.senthil.codesearch.model.CodeSearchResult;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.LinkedHashSet;


/**
 * Custom tree model that wraps the underlying tree model into a filter.
 * Only nodes that match one of the filters are displayed in the
 * tree view
 */
class FilteredTreeModel implements TreeModel {

  private TreeModel treeModel;

  private LinkedHashSet<String> filters = new LinkedHashSet<>();

  FilteredTreeModel(final TreeModel treeModel) {
    this.treeModel = treeModel;
  }

  TreeModel getTreeModel() {
    return treeModel;
  }

  /**
   * Add a file type to the list of filters.
   * @param fileType to be added to filters.
   */
  void addFilter(String fileType) {
    fileType = fileType.toLowerCase();
    if (!filters.contains(fileType)) {
      filters.add(fileType);
    }
  }

  /**
   * Removes a filetype from the list of filters.
   * @param fileType
   */
  void removeFilter(final String fileType) {
    filters.remove(fileType.toLowerCase());
  }

  /**
   * Does a recursive match
   * @param obj tree node
   * @param filters list of filters
   * @return true if there a match
   */
  private boolean recursiveMatch(final Object obj, LinkedHashSet<String> filters) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;

    boolean matches = filters.isEmpty() || (node.getUserObject() instanceof String) || filters.contains(
        ((CodeSearchResult) node.getUserObject()).getFileExtension().toLowerCase());

    int childCount = treeModel.getChildCount(node);
    for (int i = 0; i < childCount; i++) {
      Object child = treeModel.getChild(node, i);
      matches |= recursiveMatch(child, filters);
    }

    return matches;
  }

  @Override
  public Object getRoot() {
    return treeModel.getRoot();
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    int count = 0;
    int childCount = treeModel.getChildCount(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = treeModel.getChild(parent, i);
      if (recursiveMatch(child, filters)) {
        if (count == index) {
          return child;
        }
        count++;
      }
    }
    return null;
  }

  @Override
  public int getChildCount(final Object parent) {
    int count = 0;
    int childCount = treeModel.getChildCount(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = treeModel.getChild(parent, i);
      if (recursiveMatch(child, filters)) {
        count++;
      }
    }
    return count;
  }

  @Override
  public boolean isLeaf(final Object node) {
    return treeModel.isLeaf(node);
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    treeModel.valueForPathChanged(path, newValue);
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object childToFind) {
    int childCount = treeModel.getChildCount(parent);
    for (int i = 0; i < childCount; i++) {
      Object child = treeModel.getChild(parent, i);
      if (recursiveMatch(child, filters)) {
        if (childToFind.equals(child)) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    treeModel.addTreeModelListener(l);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    treeModel.removeTreeModelListener(l);
  }
}