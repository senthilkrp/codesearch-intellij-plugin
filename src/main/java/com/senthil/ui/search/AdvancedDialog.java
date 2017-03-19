package com.senthil.ui.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.senthil.messages.Messages;
import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.net.SearcherFactory;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jetbrains.annotations.Nullable;


/**
 * Code search dialog that displays advanced search options.
 */
public class AdvancedDialog extends DialogWrapper {

  private Project project;

  private FormBuilder formBuilder = FormBuilder.createFormBuilder();
  private JLabel queryLabel = new JLabel(Messages.message("ui.codesearch.dialog.query"));
  private JLabel selectMpLabel = new JLabel(Messages.message("ui.codesearch.product.label"));
  private JLabel fileTypeLabel = new JLabel(Messages.message("ui.codesearch.dialog.file.type"));
  private JLabel fileNameLabel = new JLabel(Messages.message("ui.codesearch.dialog.file.name"));
  private JLabel filePathLabel = new JLabel(Messages.message("ui.codesearch.dialog.file.path"));

  private enum FileType {
    All(""),
    Java("java"),
    PHP("php"),
    Groovy("groovy"),
    HTML("html"),
    JavaScript("js"),
    Gradle("gradle"),
    JSON("json"),
    Python("py"),
    Pig("pig"),
    Job("job"),
    Properties("properties");

    private final String extention;

    FileType(String extension) {
      this.extention = extension;
    }

    @Override
    public String toString() {
      return extention;
    }

  }

  private ComboBox<FileType> fileTypeCombo = new ComboBox<>(FileType.values());
  private JBTextField queryField = new JBTextField();
  private JBTextField fileNameField = new JBTextField();
  private JBTextField filePathField = new JBTextField();

  private JBTextField multiproducts;

  private CodeSearchRequest searchRequest;

  public AdvancedDialog(Project project) {
    super(project);
    this.project = project;
    initialize();
    setTitle(Messages.message("action.codesearch.advanced.title"));
    init();
    queryField.getEmptyText().setText(Messages.message("action.codesearch.advanced.empty.text"));
    pack();
    setOKActionEnabled(false);
    queryField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        validateInput();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        validateInput();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        validateInput();
      }

      private void validateInput() {
        setOKActionEnabled(!queryField.getText().isEmpty());
      }
    });
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = formBuilder.getPanel();
    panel.setMinimumSize(new Dimension(400, 200));
    return panel;
  }

  private void initialize() {
    formBuilder = FormBuilder.createFormBuilder();
    multiproducts = new JBTextField();
    formBuilder.addLabeledComponent(queryLabel, queryField);
    formBuilder.addLabeledComponent(selectMpLabel, multiproducts);
    formBuilder.addLabeledComponent(fileTypeLabel, fileTypeCombo);
    formBuilder.addLabeledComponent(fileNameLabel, fileNameField);
    formBuilder.addLabeledComponent(filePathLabel, filePathField);
  }

  @Override
  protected void doOKAction() {
    this.searchRequest = null;
    //the actual search will be performed the caller.
    if (isOKActionEnabled()) {
      CodeSearchRequest request = SearcherFactory.createRequest();

      request.setQuery(queryField.getText())
          .setFacet(multiproducts.getText())
          .setFileName(fileNameField.getText())
          .setFileType(fileTypeCombo.getSelectedItem().toString())
          .setFilePath(filePathField.getText());
      this.searchRequest = request;
      close(OK_EXIT_CODE);
    }
  }

  @Override
  public void doCancelAction() {
    this.searchRequest = null;
    close(CANCEL_EXIT_CODE);
  }

  @Override
  public boolean shouldCloseOnCross() {
    return true;
  }

  public CodeSearchRequest getRequest() {
    return searchRequest;
  }
}
