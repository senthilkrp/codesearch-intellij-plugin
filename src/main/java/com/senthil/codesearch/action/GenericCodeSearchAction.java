package com.senthil.codesearch.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.MessageException;
import com.senthil.messages.Messages;


/**
 * Generic code search which does not take the context into acccount.
 * The results of the search will be displayed in a toolwindow.
 *
 */
public class GenericCodeSearchAction extends CodeSearchActionBase {

  public GenericCodeSearchAction() {
    super(Messages.message("action.codesearch.description"));
  }

  @Override
  public void update(AnActionEvent e) {

    Presentation presentation = e.getPresentation();

    String searchString = getDisplayString(e.getDataContext());
    if (searchString != null) {
      //we set the menu item's text dynamically.
      presentation.setText(Messages.message("action.codesearch.label", searchString));
    }
    presentation.setEnabledAndVisible(e.getProject() != null && searchString != null && !searchString.trim().isEmpty());

    //we throw the query as exception for unit testing.
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      throw new MessageException(getDisplayString(e.getDataContext()));
    }
  }
}
