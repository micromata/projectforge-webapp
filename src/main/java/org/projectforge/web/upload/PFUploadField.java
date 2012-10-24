/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
