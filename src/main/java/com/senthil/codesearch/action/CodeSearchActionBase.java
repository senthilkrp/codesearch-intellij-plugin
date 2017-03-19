package com.senthil.codesearch.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPlainText;
import com.intellij.psi.PsiPlainTextFile;
import com.intellij.refactoring.actions.BaseRefactoringAction;
import com.intellij.util.ui.SwingHelper;
import com.senthil.codesearch.model.CodeSearchFacet;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import com.senthil.ui.StudioIcons;
import com.senthil.ui.search.CodesearchPanel;
import com.senthil.utils.CodeSearchUtils;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * CODESEARCH
 * Base class for all code search related operations.
 */
abstract class CodeSearchActionBase extends AnAction {

    public CodeSearchActionBase(@Nullable String description) {
        super("", description, StudioIcons.Actions.CODESEARCH);
    }

    /**
     * Stop characters used for parsing the text at the caret.
     * The text will be parsed if idea could not recognize the file.
     */
    private final List<Character> stopChars = Arrays.asList(',', ' ', '"', '\'', '\n', '\t', '\r', '=', '(', ')');

    /**
     * Length of display string in menu item
     */
    private static final int DISPLAY_LENGTH = 20;

    private static final Logger LOG = Logger.getInstance(CodeSearchActionBase.class);
    /**
     * Minimum length for search query. If the search query is less than MIN_QUERY_LENGTH characters,
     * search will not be performed. This is to avoid noises like ; , } etc
     */
    private static final int MIN_QUERY_LENGTH = 3;

    CodeSearchActionBase(String text, String message, Icon icon) {
        super(text, message, icon);
    }

    /**
     * @param context from invoked action
     * @return query string if its length is less than DISPLAY_LENGTH, else query string truncated to DISPLAY_LENGTH
     */
    public String getDisplayString(DataContext context) {
        String queryString = getSearchString(context);
        if (queryString == null || queryString.length() < DISPLAY_LENGTH) {
            return queryString;
        }

        return queryString.substring(0, DISPLAY_LENGTH) + SwingHelper.ELLIPSIS;
    }

    /**
     * @param context from invoked action
     * @return codesearch parameters or null if cannot find from current context
     */
    protected String getSearchString(DataContext context) {
        Editor editor = context.getData(CommonDataKeys.EDITOR);

        //text selection has highest priority
        if (editor != null) {
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (selectedText != null) {
                return selectedText;
            }
        }

        PsiElement element = getPsiElement(context, editor);
        String searchString = getSearchStringForPsiElement(editor, element);
//  do not treat very short strings as search strings because 1) Codesearch wont provide any meaningful result.
//  2) The short strings are mostly delimiters and will be ignored during a search.
        return (searchString != null && searchString.length() >= MIN_QUERY_LENGTH) ? searchString : null;
    }

    /**
     * Gets an element from the context or from the caret position.
     *
     * @param context data context
     * @param editor  file editor
     * @return psi element at caret position.
     */
    protected PsiElement getPsiElement(DataContext context, Editor editor) {
        PsiElement element = context.getData(CommonDataKeys.PSI_ELEMENT);
        return element != null ? element : getPsiElementAtCaret(context, editor);
    }

    /**
     * Returns the psi element at caret
     */
    @Nullable
    protected PsiElement getPsiElementAtCaret(DataContext context, Editor editor) {
        PsiFile psiFile = context.getData(CommonDataKeys.PSI_FILE);
        return psiFile != null && editor != null ? BaseRefactoringAction.getElementAtCaret(editor, psiFile) : null;
    }

    /**
     * Gets the search string for the current caret position.
     *
     * @param editor  file editor
     * @param element psi element.
     * @return search query string.
     */
    protected String getSearchStringForPsiElement(Editor editor, PsiElement element) {
        if (element == null) {
            return null;
        } else if (editor != null && (element instanceof PsiPlainText || element instanceof PsiPlainTextFile)) {
            //If the file is not recognized by idea, for example a scala file, getText() will return entire file content.
            //Lets try our best to get the search string by parsing the content.
            return getWordUnderCaret(editor, element);
        } else if (element instanceof PsiNamedElement) {
            return ((PsiNamedElement) element).getName();
        }
        return element.getText();
    }

    /**
     * Parses the plain text and returns the word under caret.
     *
     * @param editor  the file editor
     * @param element the element being searched for
     * @return the search string.
     */
    private String getWordUnderCaret(Editor editor, PsiElement element) {
      if (editor == null || element == null) {
        return null;
      }
        int offset = editor.getCaretModel().getOffset();
        String text = element.getText();

        if (text.trim().isEmpty()) {
            return text;
        }

        int startPos = offset - 1;
        int endPos = offset + 1;
        while (startPos > 0 && !stopChars.contains(text.charAt(startPos))) {
            startPos--;
        }

        while (endPos < text.length() && !(stopChars.contains(text.charAt(endPos)))) {
            endPos++;
        }
        return (startPos >= text.length() || endPos > text.length()) ? null : text.substring(startPos + 1, endPos);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        String query = getSearchString(event.getDataContext());

        if (query == null) {
            return;
        }

        CodeSearchRequest request = SearcherFactory.createRequest().setQuery(query);
        doSearch(event.getProject(), request);
    }

    /**
     * Performs a search with the given request.
     *
     * @param project current project
     * @param request search request to be sent to codesearch.
     */
    protected void doSearch(Project project, CodeSearchRequest request) {
        //There is nothing for us to do if request is null.
        if (request == null || project == null) {
            return;
        }

        //Create and display the search results window.
        CodesearchPanel panel = new CodesearchPanel(project);
        //Show the window first and then perform the search for a better user experience.
        CodeSearchUtils.showWindow(panel, request.toString());

        Task.Backgroundable task = new Task.Backgroundable(project, request.getDescription(), false) {
            @Override
            public void onCancel() {
                //close the tab if the search request is cancelled.
                CodeSearchUtils.hideWindow(project, request.getQuery());
            }

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    List<CodeSearchFacet> facets = SearcherFactory.getSearcher().getFacets(request).get();
                    if (indicator.isCanceled()) {
                        LOG.debug("User cancelled search.", request);
                        return;
                    }
                    //We need the application thread to access UI elements.
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (facets == null) {
                            panel.displayError();
                            //Alert the user with a error message
                            CodeSearchUtils.displayErrorMessage(panel.getProject());
                        } else {
                            panel.updateFacets(facets, request);
                        }
                    }, ModalityState.NON_MODAL);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.debug("Search interrupted", request);
                    CodeSearchUtils.displayErrorMessage(panel.getProject());
                }
            }
        };
        task.queue();
    }
}
