package com.senthil.codesearch.visitors;

import java.util.List;

/**
 * Visitor for particular file type.
 * The visitor constructs the context aware menu items.
 */
public interface CodeSearchUsageContextProvider {
  /**
   * Returns a list of usage contexts for the element under the cursor.
   * @return
   */
  List<UsageContext> getContexts();
}
