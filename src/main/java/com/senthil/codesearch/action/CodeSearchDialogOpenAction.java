package com.senthil.codesearch.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.senthil.messages.Messages;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.ui.search.AdvancedDialog;


/**
 *  Displays codesearch dialog which has advanced search options.
 *
 */

public class CodeSearchDialogOpenAction extends CodeSearchActionBase implements DumbAware {

  public CodeSearchDialogOpenAction() {
    super(null, Messages.message("action.codesearch.advanced.description"), null);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = event.getProject();
    if (project == null) {
      return;
    }

    AdvancedDialog dialog = new AdvancedDialog(project);

    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      dialog.show();
    }

    CodeSearchRequest request = dialog.getRequest();
    doSearch(event.getProject(), request);
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    presentation.setEnabledAndVisible(true);
  }
}
