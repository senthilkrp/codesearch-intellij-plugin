package com.senthil.codesearch.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.MessageException;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.utils.CodeSearchUtils;


/**
 * Opens the currently viewed search result in Web View (web browser).
 */
public class OpenSearchResultInBrowserAction extends AnAction {
  public OpenSearchResultInBrowserAction() {
    super(AllIcons.Actions.Preview);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    openInBrowser(file);
  }

  void openInBrowser(VirtualFile file) {
    if (file != null) {
      CodeSearchResult result = CodeSearchUtils.getSearchResult(file);
      if (result != null) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
          throw new MessageException(result.getExternalLink());
        }
        CodeSearchUtils.openInBrowser(result.getExternalLink());
      }
    }
  }

  @Override
  public void update(AnActionEvent e) {
    VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    Presentation presentation = e.getPresentation();
    //We store the search result as user data in the virtual file. We can use this to distinguish between a search result file and an user-opened file.
    presentation.setEnabledAndVisible(file != null && CodeSearchUtils.getSearchResult(file) != null);
  }
}
