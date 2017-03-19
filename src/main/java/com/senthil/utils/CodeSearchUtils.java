package com.senthil.utils;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.senthil.codesearch.model.CodeSearchResult;
import com.senthil.messages.Messages;
import com.senthil.notification.NotificationManager;
import com.senthil.codesearch.model.CodeSearchHighlightData;
import com.senthil.ui.search.CodesearchPanel;
import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;


/**
 * Utility function for codesearch
 */
public final class CodeSearchUtils {

  private static final Key<CodeSearchResult> CODESEARCH_RESULT = new Key<>("CodeSearchResult");

  //Utility classes should not have a public or default constructor
  private CodeSearchUtils() {

  }

  public static final String CODESEARCH_WINDOW_ID = "CodeSearch";

  private static final NotificationGroup CODESEARCH_CONNECTION_NOTIFICATION_GROUP =
      new NotificationGroup(Messages.message("action.codesearch.error.notification"),
          NotificationDisplayType.BALLOON, false);

  private static final NotificationGroup HIGHLIGHT_NOTIFICATION_GROUP =
      new NotificationGroup(Messages.message("ui.codesearch.highlight.notification"),
          NotificationDisplayType.BALLOON, false);

  private static final Logger LOG = Logger.getInstance(CodeSearchUtils.class);

  /**
   * Display the codesearch results window.
   * @param panel ui component.
   * @param query the search tab name
   */
  public static void showWindow(CodesearchPanel panel, String query) {
    ToolWindowManager manager = ToolWindowManager.getInstance(panel.getProject());
    ToolWindow toolWindow = manager.getToolWindow(CODESEARCH_WINDOW_ID);
    Content content = toolWindow.getContentManager().findContent(query);

    if (content == null) {
      content = ContentFactory.SERVICE.getInstance().createContent(panel, query, true);
    }
    toolWindow.getContentManager().addContent(content);
    toolWindow.getContentManager().setSelectedContent(content, true);
    toolWindow.setAutoHide(false);
    toolWindow.show(null);
    toolWindow.activate(null);
  }

  /**
   * Hides the codesearch results tab with the given name.
   * @param project current project
   * @param query the search tab display name
   */
  public static void hideWindow(Project project, String query) {
    ToolWindowManager manager = ToolWindowManager.getInstance(project);
    ToolWindow toolWindow = manager.getToolWindow(CODESEARCH_WINDOW_ID);
    Content content = toolWindow.getContentManager().findContent(query);
    if (content == null) {
      return;
    }
    toolWindow.getContentManager().removeContent(content, true);

    //hide the toolwindow if there are no contents
    if (toolWindow.getContentManager().getContentCount() == 0) {
      toolWindow.hide(null);
    }
  }

  /**
   * Display an network connection error message to the user
   * @param project
   */
  public static void displayErrorMessage(Project project) {
    NotificationManager.getInstance(project)
        .showNotification(CODESEARCH_CONNECTION_NOTIFICATION_GROUP,
            Messages.message("action.codesearch.error.notification.title"),
            Messages.message("action.codesearch.error.notification.message"), NotificationType.ERROR, null);
  }

  /**
   * Open the file present @ path in a web browser
   * @param path
   */
  public static void openInBrowser(String path) {
      BrowserUtil.browse(path);
  }

  /**
   * Creates a virtual file from the search result
   * @param result
   */
  private static VirtualFile getOrCreateFile(@NotNull Project project, @NotNull CodeSearchResult result) {
    VirtualFile[] openFiles = FileEditorManager.getInstance(project).getOpenFiles();
    for (VirtualFile openFile : openFiles) {
      CodeSearchResult embeddedResult = getSearchResult(openFile);
      if (embeddedResult != null && embeddedResult.getFilePath() != null && embeddedResult.getFilePath()
          .equals(result.getFilePath())) {
        return openFile;
      }
    }
    FileType fileType;
    if (result.getFileExtension() == null) {
      fileType = PlainTextFileType.INSTANCE;
    } else {
      fileType = FileTypeManager.getInstance().getFileTypeByExtension(result.getFileExtension());
    }

    if (fileType instanceof UnknownFileType) {
      fileType = PlainTextFileType.INSTANCE;
    }

    LightVirtualFile file = new LightVirtualFile(result.getFileName(), fileType, result.getContent());
    file.putUserDataIfAbsent(CODESEARCH_RESULT, result);
    file.setWritable(false);
    return file;
  }

  public static CodeSearchResult getSearchResult(@NotNull VirtualFile file) {
    return file.getUserData(CODESEARCH_RESULT);
  }

  /**
   * Highlights the file with the highlight data present in search result
   * The file will be open in an
   * @param result search result
   */
  public static VirtualFile openAndHighlightFile(@NotNull Project project, @NotNull CodeSearchResult result) {

    VirtualFile file = getOrCreateFile(project, result);

    file.putUserDataIfAbsent(CODESEARCH_RESULT, result);

    FileEditorManager.getInstance(project).openFile(file, true);

    Collection<CodeSearchHighlightData> highlightDatas = result.getHighlightData();

    //Highlight the results
    HighlightManager highlightManager = HighlightManager.getInstance(project);
    Editor currentEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    assert currentEditor != null;
    MarkupModel markupModel = currentEditor.getMarkupModel();
    markupModel.removeAllHighlighters();
    String documentText = currentEditor.getDocument().getText().toLowerCase();
    TextAttributes attributes =
        new TextAttributes(currentEditor.getSelectionModel().getTextAttributes().getForegroundColor(),
            currentEditor.getSelectionModel().getTextAttributes().getBackgroundColor(), JBColor.YELLOW,
            EffectType.SEARCH_MATCH, 0);

    if (highlightDatas == null || highlightDatas.isEmpty()) { //just open the file if there is no highlight data
      NotificationManager.getInstance(project)
          .showNotification(HIGHLIGHT_NOTIFICATION_GROUP,
              Messages.message("ui.codesearch.highlight.notification.title"),
              Messages.message("ui.codesearch.highlight.notification.message"), NotificationType.INFORMATION,
              null);
    }

    Integer firstOccurenceLineNo = null;
    if (highlightDatas != null) {
      for (CodeSearchHighlightData highlightData : highlightDatas) {
        int startOffset = currentEditor.getDocument().getLineStartOffset(highlightData.getLineNumber());
        String matchedQuery = highlightData.getMatchedString().toLowerCase();
        int startIndex = documentText.indexOf(matchedQuery, startOffset);
        int endIndex = startIndex + matchedQuery.length();
        if (startIndex == -1) {
          continue;
        }
        highlightManager.addOccurrenceHighlight(currentEditor, startIndex, endIndex, attributes,
            HighlightManager.HIDE_BY_ESCAPE, new ArrayList<>(), JBColor.BLUE);
        if (firstOccurenceLineNo == null) {
          firstOccurenceLineNo = highlightData.getLineNumber();
          new OpenFileDescriptor(project, file, firstOccurenceLineNo, startIndex - startOffset).navigate(true);
        }
      }
    }
    return file;
  }
}
