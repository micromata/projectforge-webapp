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

package org.projectforge.web.upload;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * Upload form component. The list of {@link FileUpload} is needed because of multi file uploads.<br/>
 * In the most case, you are only interested in the first element of that list. Therefore you can use the {@link #PFUploadField(String)}
 * constructor and the {@link #getFileUpload()} method.
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class PFUploadField extends Panel
{
  private static final long serialVersionUID = 25377102152234232L;

  private FileUploadField fileUploadField;

  private String fileName;

  private final List<FileUpload> list;

  private TextField<String> label;

  /**
   * @param id
   */
  public PFUploadField(final String id)
  {
    this(id, new LinkedList<FileUpload>());
  }

  /**
   * @param id
   */
  public PFUploadField(final String id, final List<FileUpload> list)
  {
    super(id);
    this.list = list;
    this.fileUploadField = new FileUploadField("input", new PropertyModel<List<FileUpload>>(this, "list")) {
      private static final long serialVersionUID = 2353649153343534798L;

      @Override
      public IModel<String> getLabel()
      {
        return Model.of(id);
      }
    };
    label = new TextField<String>("label", new PropertyModel<String>(this, "fileName"));
    label.setOutputMarkupId(true);
    label.add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 6180636088120654324L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target)
      {
        // do nothing, just update model
      }
    });
    add(label);
    add(this.fileUploadField);
  }

  /**
   * the current file upload
   * @return
   */
  public FileUpload getFileUpload()
  {
    if (list != null && list.size() > 0) {
      return list.get(0);
    }
    return null;
  }

  /**
   * @return the label
   */
  public TextField<String> getLabel()
  {
    return label;
  }

  /**
   * the full model object
   * @return
   */
  public List<FileUpload> getModelObject()
  {
    return list;
  }

  public FileUploadField getUploadField()
  {
    return this.fileUploadField;
  }


  /**
   * @param fileName the fileName to set
   * @return this for chaining.
   */
  public void setFileName(final String fileName)
  {
    this.fileName = fileName;
  }

  /**
   * @return the fileName
   */
  public String getFileName()
  {
    return fileName;
  }


}
