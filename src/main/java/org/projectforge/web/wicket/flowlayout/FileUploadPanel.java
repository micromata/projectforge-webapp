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

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Represents an icon.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class FileUploadPanel extends Panel implements ComponentWrapperPanel
{
  public static final String WICKET_ID = "input";

  private static final long serialVersionUID = -4126462093466172226L;

  private FileUploadField fileUploadField;

  public FileUploadPanel(final String id, final FileUploadField fileUploadField)
  {
    super(id);
    add(this.fileUploadField = fileUploadField);
    // add(new Label("filename", new Model<String>() {
    // /**
    // * @see org.apache.wicket.model.Model#getObject()
    // */
    // @Override
    // public String getObject()
    // {
    // final FileUpload fileUpload = fileUploadField.getFileUpload();
    // if (fileUpload == null) {
    // return getString("file.upload.noFileSelected");
    // } else {
    // return fileUpload.getClientFileName();
    // }
    // }
    // }));
    // add(new Label("action", getString("file.upload.choose")));
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
  public FileUploadField getField()
  {
    return fileUploadField;
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
