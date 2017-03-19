package com.senthil.ui.search;

import com.senthil.codesearch.model.CodeSearchFacet;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Tree node representation for facets/multiproducts.
 */
@SuppressWarnings("serial")
class FacetTreeNode extends DefaultMutableTreeNode {
  private CodeSearchFacet facet;
  FacetTreeNode(CodeSearchFacet facet) {
    super(facet);
    this.facet = facet;
  }
  @Override
  public String toString() {
    return String.format("%s (%d)", facet.getName(), facet.getMatchCount());
  }
}
