package com.senthil.codesearch.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import com.senthil.messages.Messages;
import com.senthil.utils.CodeSearchUtils;


/**
 * Created by spanneer on 3/19/17.
 */
public class RepositoryCodeSearchAction extends GenericCodeSearchAction {

  @Override
  public void actionPerformed(AnActionEvent event) {
    String query = getSearchString(event.getDataContext());

    if (query == null) {
      return;
    }
    VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
    if(file == null)
      return;
    CodeSearchResult result = CodeSearchUtils.getSearchResult(file);

    CodeSearchRequest request = SearcherFactory.createRequest().setQuery(query).setFacet(result.getProductName());
    doSearch(event.getProject(), request);
  }

  @Override
  public void update(AnActionEvent e) {
    VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    Presentation presentation = e.getPresentation();
    boolean isEnabled = file != null && CodeSearchUtils.getSearchResult(file) != null;
    //We store the search result as user data in the virtual file. We can use this to distinguish between a search result file and an user-opened file.
    presentation.setEnabledAndVisible(isEnabled);
    if (isEnabled) {
      CodeSearchResult result = CodeSearchUtils.getSearchResult(file);
      String searchString = getDisplayString(e.getDataContext());
      if (searchString != null) {
        //we set the menu item's text dynamically.
        presentation.setText(
            Messages.message("action.repository.codesearch.label", searchString, result.getProductName()));
      }
      presentation.setEnabledAndVisible(
          e.getProject() != null && searchString != null && !searchString.trim().isEmpty());
    }
  }
}
