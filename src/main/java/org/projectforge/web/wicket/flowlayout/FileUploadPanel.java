/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.flowlayout;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.common.FileHelper;
import org.projectforge.web.dialog.ModalQuestionDialog;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.DownloadUtils;

/**
 * Represents an upload field of a form. If configured it also provides a delete button and the file name with download link.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FileUploadPanel extends Panel implements ComponentWrapperPanel
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileUploadPanel.class);

  public static final String WICKET_ID = "input";

  private static final long serialVersionUID = -4126462093466172226L;

  private FileUploadField fileUploadField;

  private IModel<String> filename;

  private IModel<byte[]> file;

  private TextLinkPanel textLinkPanel;

  private ModalQuestionDialog deleteExistingFileDialog;

  private AjaxIconButtonPanel deleteFileButton;

  private Label removeFileSelection;

  private static final String REMOVE_FILE_SELECTION_LABEL = "X";

  /**
   * @param id Component id
   * @param fs Optional FieldsetPanel for creation of filename with download link and upload button.
   * @param createFilenameLink If true (and fs is given) the filename is displayed with link for download.
   * @param
   */
  @SuppressWarnings("serial")
  public FileUploadPanel(final String id, final FieldsetPanel fs, final Form< ? > form, final boolean createFilenameLink,
      final IModel<String> filename, final IModel<byte[]> file)
  {
    super(id);
    this.filename = filename;
    this.file = file;
    if (fs != null) {
      fs.add(this.textLinkPanel = new TextLinkPanel(fs.newChildId(), new Link<Void>(TextLinkPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          final byte[] data = file.getObject();
          DownloadUtils.setDownloadTarget(data, getFilename());
        }
      }, new Model<String>() {
        /**
         * @see org.apache.wicket.model.Model#getObject()
         */
        @Override
        public String getObject()
        {
          return getFilename();
        }
      }) {
        @Override
        public boolean isVisible()
        {
          return file.getObject() != null;
        };
      });
      textLinkPanel.getLink().setOutputMarkupPlaceholderTag(true);
      // DELETE BUTTON
      deleteFileButton = new AjaxIconButtonPanel(fs.newChildId(), IconType.TRASH, fs.getString("delete")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          deleteExistingFileDialog.open(target);
          // file.setObject(null);
          // filename.setObject(null);
        }

        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return file.getObject() != null;
        }
      };
      deleteFileButton.getButton().setOutputMarkupPlaceholderTag(true);
      fs.add(deleteFileButton);
    }
    add(this.fileUploadField = new FileUploadField(FileUploadPanel.WICKET_ID) {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return file.getObject() == null;
      }
    });
    this.fileUploadField.setOutputMarkupPlaceholderTag(true);
    add(this.removeFileSelection = new Label("removeFileSelection", REMOVE_FILE_SELECTION_LABEL) {
      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return file.getObject() == null;
      }
    });
    this.removeFileSelection.setOutputMarkupPlaceholderTag(true);
    if (fs != null) {
      fs.add(this);
      final AbstractSecuredPage parentPage = (AbstractSecuredPage) getPage();
      deleteExistingFileDialog = new ModalQuestionDialog(parentPage.newModalDialogId(), new ResourceModel(
          "file.panel.deleteExistingFile.heading"), new ResourceModel("file.panel.deleteExistingFile.question")) {
        /**
         * @see org.projectforge.web.dialog.ModalQuestionDialog#onCloseButtonSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected boolean onCloseButtonSubmit(final AjaxRequestTarget target)
        {
          super.onCloseButtonSubmit(target);
          if (isConfirmed() == true) {
            file.setObject(null);
            filename.setObject(null);
            if (textLinkPanel != null) {
              textLinkPanel.getLabel().modelChanged();
              textLinkPanel.getLink().setVisible(false);
              deleteFileButton.getButton().setVisible(false);
              fileUploadField.setVisible(true);
              removeFileSelection.setVisible(true);
              target.add(textLinkPanel.getLink(), deleteFileButton.getButton(), fileUploadField, removeFileSelection);
            }
          }
          return true;
        }
      };
      parentPage.add(deleteExistingFileDialog);
      deleteExistingFileDialog.init();
      // fs.add(new UploadProgressBar("progress", form, this.fileUploadField));
    }
  }

  public FileUploadPanel(final String id)
  {
    this(id, new FileUploadField(FileUploadPanel.WICKET_ID));
  }

  public FileUploadPanel(final String id, final FileUploadField fileUploadField)
  {
    super(id);
    add(this.fileUploadField = fileUploadField);
    add(this.removeFileSelection = new Label("removeFileSelection", REMOVE_FILE_SELECTION_LABEL));
  }

  /**
   * Call this to upload the exisiting files.
   */
  public void upload()
  {
    final FileUpload fileUpload = getFileUpload();
    if (fileUpload != null) {
      final String clientFileName = FileHelper.createSafeFilename(fileUpload.getClientFileName(), 255);
      log.info("Upload file '" + clientFileName + "'.");
      final byte[] bytes = fileUpload.getBytes();
      filename.setObject(clientFileName);
      file.setObject(bytes);
    }
  }

  /**
   * Return the file name itself if given, if the given file name is null, the i18n translation of 'file' is returned.
   * @param filename
   * @return
   */
  protected String getFilename()
  {
    final String fname = this.filename.getObject() != null ? this.filename.getObject() : getString("file");
    return fname;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    fileUploadField.setOutputMarkupId(true);
    return fileUploadField.getMarkupId();
  }

  /**
   * @return the field
   */
  public FileUploadField getFileUploadField()
  {
    return fileUploadField;
  }

  public FileUpload getFileUpload()
  {
    return fileUploadField.getFileUpload();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getFormComponent()
   */
  @Override
  public FormComponent< ? > getFormComponent()
  {
    return fileUploadField;
  }
}
