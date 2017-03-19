package com.senthil.ui.search;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.messages.Messages;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.Preconditions;


/**
 * Panel to display facets information in a tree format.
 */
@SuppressWarnings("serial")
class FacetsPanel extends JBPanel {

    private Tree facetsTree;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    private static final Logger LOG = Logger.getInstance(FacetsPanel.class);

    FacetsPanel(Tree tree) {
        facetsTree = tree;
        Preconditions.condition(facetsTree.getModel() instanceof DefaultTreeModel, "Facets tree model is not an instance of DefaultTreeModel");
        model = (DefaultTreeModel) facetsTree.getModel();
        Preconditions.condition(model.getRoot() instanceof DefaultMutableTreeNode, "Facets tree mode is not an instance of DefaultMutableTreeNode");
        root = (DefaultMutableTreeNode) model.getRoot();
        root.setUserObject(Messages.message("ui.codesearch.facets.loading.message"));
        root.removeAllChildren();
        model.reload(root);
        new TreeSpeedSearch(facetsTree, TreeSpeedSearch.NODE_DESCRIPTOR_TOSTRING, false);
        @SuppressWarnings("serial")
        ColoredTreeCellRenderer renderer = new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object node, boolean selected, boolean expanded,
                                              boolean leaf, int row, boolean hasFocus) {
                Object userObj = ((DefaultMutableTreeNode) node).getUserObject();
                //The userObj can either be a string in case of the root node
                //CodeSearchFacet otherwise.
                append(userObj.toString());
                setIcon(userObj instanceof CodeSearchFacet ? AllIcons.Modules.SourceRoot : null);
            }
        };
        facetsTree.setCellRenderer(renderer);
        setLayout(new BorderLayout());
        add(new JBScrollPane(facetsTree), BorderLayout.CENTER);
    }

    /**
     * Updates the facets panel with the given multi products.
     *
     * @param products list of multi products matching for the given query.
     * @param query    search query string.
     */
    public void update(List<CodeSearchFacet> products, String query) {
        model.reload(root);
        root.removeAllChildren();
        markBusy();
        if (products.size() == 0) {
            LOG.info("No facets found for the given query " + query);
            root.setUserObject(Messages.message("ui.codesearch.facets.resultsnotfound", query));
            model.reload(root);
            return;
        }
        for (CodeSearchFacet product : products) {
            FacetTreeNode node = new FacetTreeNode(product);
            root.add(node);
        }
        markReady();
        ((DefaultTreeModel) facetsTree.getModel()).reload(root);
        facetsTree.expandPath(new TreePath(root.getPath()));
    }

    /**
     * Display a busy message in the facets panel.
     */
    private void markBusy() {
        if (root != null) {
            root.setUserObject(Messages.message("ui.codesearch.facets.loading.message"));
            model.reload(root);
        }
    }

    /**
     * Displays the default message.
     */
    private void markReady() {
        if (root != null) {
            root.setUserObject(Messages.message("ui.codesearch.facets.ready.message"));
            model.reload(root);
        }
    }

    /**
     * Displays an error message to the user.
     */
    void displayError() {
        root.removeAllChildren();
        root.setUserObject(Messages.message("ui.codesearch.facets.error.message"));
        model.reload(root);
    }
}


