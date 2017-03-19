package com.senthil.ui.search;

import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.find.FindBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.JBColor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.usages.UsageView;
import com.senthil.messages.Messages;
import com.senthil.codesearch.CodeSearchHistoryManager;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import com.senthil.utils.CodeSearchUtils;
import com.sun.glass.events.KeyEvent;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * The window displaying the search results for a facet.
 *
 */
@SuppressWarnings("serial")
class ResultsPanel extends JBPanel {

  private final Tree resultsViewTree;
  /**
   * tree display of results.
   */
  private final ResultsViewComponent resultsViewComponent;

  ResultsPanel(Tree tree) {
    resultsViewTree = tree;
    resultsViewComponent = new ResultsViewComponent(tree);
    setLayout(new BorderLayout());
    setBackground(JBColor.WHITE);
    JComponent actionToolbar = createActionToolbar().getComponent();
    JComponent filterToolbar = createFilterToolbar().getComponent();
    actionToolbar.setBorder(this.getBorder());
    add(actionToolbar, BorderLayout.WEST);

    JBPanel resultsView = new JBPanel();
    resultsView.setLayout(new BorderLayout());
    resultsView.add(filterToolbar, BorderLayout.NORTH);
    //The load more button doesnt work if the horizontal scrollbar is present. So we disable horizontal scrollbar
    //Fix this if required.
    resultsView.add(new JBScrollPane(resultsViewComponent, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    add(resultsView, BorderLayout.CENTER);
    new TreeSpeedSearch(resultsViewTree, TreeSpeedSearch.NODE_DESCRIPTOR_TOSTRING, false);
  }

  /**
   * Create file filters for important file types.
   * @return
   */
  private ActionToolbar createFilterToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();

    //File filters
    FileType[] fileTypes =
        {JavaFileType.INSTANCE, PlainTextFileType.INSTANCE, XmlFileType.INSTANCE, JsonFileType.INSTANCE, HtmlFileType.INSTANCE};
    for (FileType fileType : fileTypes) {
      FileFilterAction filter = new FileFilterAction(fileType);
      group.add(filter);
    }

    ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
    actionToolbar.setLayoutPolicy(ActionToolbar.AUTO_LAYOUT_POLICY);
    return actionToolbar;
  }

  void clearView() {
    resultsViewComponent.clear();
  }

  void markBusy() {
    resultsViewComponent.markLoading();
  }

  /**
   * Displays a list of previous search queries to the user.
   */
  public static class HistoryAction extends AnAction implements DumbAware {

    class HistoryPopup extends BaseListPopupStep<CodeSearchRequest> {
      private final Project project;

      public HistoryPopup(@NotNull Project project, @Nullable String title,
          java.util.List<? extends CodeSearchRequest> values) {
        super(title, values);
        this.project = project;
      }

      @Override
      public PopupStep onChosen(CodeSearchRequest request, boolean finalChoice) {
        CodesearchPanel panel = new CodesearchPanel(project);
        CodeSearchUtils.showWindow(panel, request.toString());
        SearcherFactory.getSearcher()
            .getFacets(request)
            .thenAccept(facets -> panel.updateFacets(facets, request))
            .exceptionally(ex -> {
              panel.displayError();
              CodeSearchUtils.displayErrorMessage(project);
              return null;
            });
        return FINAL_CHOICE;
      }

      @NotNull
      @Override
      public String getTextFor(CodeSearchRequest request) {
        return request.toString();
      }

      @Override
      public boolean isSpeedSearchEnabled() {
        return true;
      }
    }

    public HistoryAction() {
      super(Messages.message("ui.codesearch.toolbar.hints.findrecentusages"), null, AllIcons.Actions.Back);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      UsageView usageView = e.getData(UsageView.USAGE_VIEW_KEY);
      Project project = e.getProject();
      if (project == null) {
        return;
      }

      //Display the pop to the user and get input.

      RelativePoint point = null;
      if (e.getInputEvent() instanceof MouseEvent) {
        point = new RelativePoint((MouseEvent) e.getInputEvent());
      } else if (usageView != null) {
        point = new RelativePoint(usageView.getComponent(), new Point(4, 4));
      }

      displayPopup(project, point);
    }

    void displayPopup(Project project, RelativePoint point) {

      List<CodeSearchRequest> recentQueries = CodeSearchHistoryManager.getInstance()
          .getRecentQueries()
          .stream()
          .map(CodeSearchRequest::copy)
          .collect(Collectors.toList());

      if (!recentQueries.isEmpty()) {
        Collections.reverse(recentQueries);
      }

      if (recentQueries.isEmpty()) {
        recentQueries.add(null); // to fill the popup
      }

      ListPopup popup = JBPopupFactory.getInstance()
          .createListPopup(
              new HistoryPopup(project, FindBundle.message("recent.find.usages.action.title"), recentQueries));

      if (point != null) {
        popup.show(point);
      }
    }

    @Override
    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(CodeSearchHistoryManager.getInstance().getRecentQueries().size() > 1);
    }
  }

  /**
   * Base class for all toolbar action buttons
   */
  private abstract class ToolBarAction extends AnAction implements HintManagerImpl.ActionToIgnore {
    ToolBarAction(String text, String description, Icon icon) {
      super(text, description, icon);
    }

    @Override
    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(!resultsViewComponent.isEmpty());
    }
  }

  /**
   * Navigate to previous search result file.
   */
  private class PrevAction extends ToolBarAction {
    public PrevAction() {
      super(Messages.message("ui.codesearch.toolbar.hints.prevresult"), null, AllIcons.ToolbarDecorator.MoveUp);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      resultsViewComponent.showPrevResult();
    }
  }

  /**
   * Navigate to the next search result file.
   */
  private class NextAction extends ToolBarAction {
    public NextAction() {
      super(Messages.message("ui.codesearch.toolbar.hints.nextresult"), null,
          AllIcons.ToolbarDecorator.MoveDown);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      resultsViewComponent.showNextResult();
    }
  }

  /**
   * Base class for all the file filters.
   */
  private class FileFilterAction extends ToggleAction {
    FileType fileType;
    boolean selected = false;

    public FileFilterAction(FileType fileType) {
      super(fileType.getDescription(), null, fileType.getIcon());
      this.fileType = fileType;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
      return selected;
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean state) {
      selected = state;
      if (state) {
        resultsViewComponent.addFilter(fileType);
      } else {
        resultsViewComponent.removeFilter(fileType);
      }
    }
  }

  /**
   * Opens the current search result in browser.
   */
  private class PreviewAction extends ToolBarAction {

    public PreviewAction() {
      super(Messages.message("action.codesearch.open.browser.view"), null, AllIcons.Actions.Preview);
    }

    @Override
    public void update(AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabled(!resultsViewTree.isEmpty() && resultsViewTree.getLastSelectedPathComponent() != null
          && resultsViewTree.getLastSelectedPathComponent() instanceof ResultsTreeNode);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
      if (resultsViewTree.getLastSelectedPathComponent() instanceof ResultsTreeNode) {
        ResultsTreeNode currentNode = (ResultsTreeNode) resultsViewTree.getLastSelectedPathComponent();
        CodeSearchUtils.openInBrowser(currentNode.getSearchResult().getFilePath());
      }
    }
  }

  public void update(java.util.List<CodeSearchResult> searchResults, CodeSearchFacet facet) {
    resultsViewComponent.update(searchResults, facet);
  }

  /**
   * Creates the action buttons for the tool bar.
   * @return
   */
  ActionToolbar createActionToolbar() {
    DefaultActionGroup group = new DefaultActionGroup();

    HistoryAction back = new HistoryAction();
    group.add(back);

    PrevAction prev = new PrevAction();
    prev.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), this);
    group.add(prev);

    NextAction next = new NextAction();
    next.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), this);
    group.add(next);

    PreviewAction openExternal = new PreviewAction();
    openExternal.registerCustomShortcutSet(
        new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.SHIFT_MASK)), this);
    group.add(openExternal);

    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, false);
  }
}

