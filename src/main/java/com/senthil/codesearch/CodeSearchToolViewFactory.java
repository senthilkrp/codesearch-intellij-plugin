package com.senthil.codesearch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.senthil.ui.StudioIcons;
import org.jetbrains.annotations.NotNull;


/**
 * Class for registering codesearch results window.
 *
 * The UI creation and initialization happens in CodeSearchUtils.
 */
public class CodeSearchToolViewFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

  }

  @Override
  public void init(ToolWindow window) {
    window.setIcon(StudioIcons.Actions.CODESEARCH);
  }

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return false;
  }

  @Override
  public boolean isDoNotActivateOnStart() {
    return true;
  }
}
