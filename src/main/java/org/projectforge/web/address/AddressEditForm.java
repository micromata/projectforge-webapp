/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.address;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;

public class AddressEditForm extends AbstractEditForm<AddressDO, AddressEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditForm.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  private TextField<String> authorsField, signatureField, publisherField, editorField, yearOfPublishingField;

  public AddressEditForm(AddressEditPage parentPage, AddressDO data)
  {
    super(parentPage, data);
    this.colspan = 4;
    if (isNew() == true) {
    }
  }

  @Override
  protected void init()
  {
    super.init();
    add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    final TextField<String> titleField = new RequiredMaxLengthTextField("title", new PropertyModel<String>(data, "title"));
    titleField.add(new FocusOnLoadBehavior());
    add(titleField);
    add(new MaxLengthTextField("keywords", new PropertyModel<String>(data, "keywords")));
    add(new MaxLengthTextField("isbn", new PropertyModel<String>(data, "isbn")));
    add(signatureField = new MaxLengthTextField("signature", new PropertyModel<String>(data, "signature")));
    add(publisherField = new MaxLengthTextField("publisher", new PropertyModel<String>(data, "publisher")));
    add(editorField = new MaxLengthTextField("editor", new PropertyModel<String>(data, "editor")));
    add(yearOfPublishingField = new MaxLengthTextField("yearOfPublishing", new PropertyModel<String>(data, "yearOfPublishing")));
    add(authorsField = new MaxLengthTextField("authors", new PropertyModel<String>(data, "authors")));
    add(new MaxLengthTextArea("abstract", new PropertyModel<String>(data, "abstractText")));
    add(new MaxLengthTextArea("comment", new PropertyModel<String>(data, "comment")));

    // DropDownChoice status
    /*final LabelValueChoiceRenderer<AddressStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(this, AddressStatus.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
        statusChoiceRenderer);
    statusChoice.setNullValid(false).setRequired(true);
    add(statusChoice);*/
  }

  @Override
  protected void addBottomRows()
  {
    final Fragment bottomRowsFragment = new Fragment("bottomRows", "bottomRowsFragment", this);
    bottomRowsFragment.setRenderBodyOnly(true);
    add(bottomRowsFragment);
    if (isNew() == true) {
      bottomRowsFragment.setVisible(false);
      return;
    }
  }

  @Override
  protected void validation()
  {
    signatureField.validate();
    authorsField.validate();
    publisherField.validate();
    editorField.validate();
    yearOfPublishingField.validate();
    if (StringUtils.isBlank(authorsField.getConvertedInput())
        && StringUtils.isBlank(publisherField.getConvertedInput())
        && StringUtils.isBlank(editorField.getConvertedInput())
        && StringUtils.isBlank(signatureField.getConvertedInput())
        && StringUtils.isBlank(yearOfPublishingField.getConvertedInput())) {
      addComponentError(authorsField, "address.error.toFewFields");
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
