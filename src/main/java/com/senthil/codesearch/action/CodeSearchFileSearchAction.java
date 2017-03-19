package com.senthil.codesearch.action;

import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ListChooseByNameModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.messages.Messages;
import com.senthil.codesearch.CodesearchFileProvider;
import com.senthil.utils.CodeSearchUtils;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;


/**
 * Performs a quick file search on repository.
 */
public class CodeSearchFileSearchAction extends GotoActionBase implements DumbAware {
  @Override
  protected void gotoActionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) {
      return;
    }

    ChooseByNamePopup popup = ChooseByNamePopup.createPopup(project,
        new CodeSearchChooseByNameMode(project, Messages.message("ui.codesearch.quickdialog.heading"),
            Messages.message("ui.codesearch.quickdialog.file.not.found"),
            Collections.<CodeSearchResult>emptyList()), new CodesearchFileProvider(), null, false, 0);
    popup.setAdText(Messages.message("ui.codesearch.quickdialog.help"));
    //show navigation will throw illegal state exception during unit test. This is to prevent that.
    //We cant throw MessageException because the base class catches Throwable.
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    showNavigationPopup(new FileSearchCallback<FileType>(project),
        Messages.message("ui.codesearch.quickdialog.heading"), popup, false);
  }

  /**
   * Call back for file selection.
   * @param <T> The type of filter to be used. (We use only the elementChosen method and not createFilter method. Hence this will be ignored.)
   */
  public static class FileSearchCallback<T> extends GotoActionCallback<T> {

    private final Project project;

    public FileSearchCallback(Project project) {
      this.project = project;
    }

    @Override
    public void elementChosen(final ChooseByNamePopup popup, final Object element) {
      if (element instanceof CodeSearchResult) {
        CodeSearchUtils.openAndHighlightFile(project, (CodeSearchResult) element);
      }
    }
  }

  /**
   * model for the file search ui.
   */
  public static class CodeSearchChooseByNameMode extends ListChooseByNameModel<CodeSearchResult> {

    public CodeSearchChooseByNameMode(@NotNull Project project, String prompt, String notInMessage,
        List<CodeSearchResult> items) {
      super(project, prompt, notInMessage, items);
    }

    @Override
    public String getCheckBoxName() {
      return Messages.message("ui.codesearch.quickdialog.option.label");
    }

    @Override
    public String getElementName(Object element) {
      if(element instanceof CodeSearchResult) {
        CodeSearchResult result = (CodeSearchResult) element;
        return result.getName() + " in " + result.getProductName();
      }
      return super.getElementName(element);
    }

    @Override
    public String getFullName(Object element) {
      return getElementName(element);
    }
  }
}


