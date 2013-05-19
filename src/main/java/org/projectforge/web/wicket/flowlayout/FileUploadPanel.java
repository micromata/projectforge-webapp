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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.common.FileHelper;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.components.SingleButtonPanel;

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

  /**
   * @param id Component id
   * @param fs Optional FieldsetPanel for creation of filename with download link and upload button.
   * @param createFilenameLink If true (and fs is given) the filename is displayed with link for download.
   * @param createUploadButton If true (and fs is given) the upload button is displayed.
   * @param
   */
  @SuppressWarnings("serial")
  public FileUploadPanel(final String id, final FieldsetPanel fs, final boolean createFilenameLink, final boolean createUploadButton,
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
      // DELETE BUTTON
      final IconButtonPanel deleteFileButton = new IconButtonPanel(fs.newChildId(), IconType.TRASH, fs.getString("delete")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit()
        {
          file.setObject(null);
          filename.setObject(null);
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
      fs.add(deleteFileButton);
    }
    add(this.fileUploadField = new FileUploadField(FileUploadPanel.WICKET_ID));
    fs.add(this);
    if (fs != null) {
      if (createUploadButton == true) {
        final Button uploadButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("upload")) {
          @Override
          public final void onSubmit()
          {
            upload();
          }
        };
        fs.add(new SingleButtonPanel(fs.newChildId(), uploadButton, getString("upload"), SingleButtonPanel.GREY));
      }
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
  }

  /**
   * Is called, if FileUploadPanel is created with createUploadButton = true and the upload button was pressed by the user.
   */
  protected void upload()
  {
    final FileUpload fileUpload = getFileUpload();
    if (fileUpload != null) {
      final String clientFileName = FileHelper.createSafeFilename(fileUpload.getClientFileName(), 255);
      log.info("Upload file '" + clientFileName + "'.");
      final byte[] bytes = fileUpload.getBytes();
      filename.setObject(clientFileName);
      if (textLinkPanel != null) {
        textLinkPanel.modelChanged();
      }
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
