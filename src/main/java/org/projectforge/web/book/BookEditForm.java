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

package org.projectforge.web.book;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.book.BookStatus;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class BookEditForm extends AbstractEditForm<BookDO, BookEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BookEditForm.class);

  @SpringBean(name = "bookDao")
  private BookDao bookDao;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  private TextField<String> authorsField, signatureField, publisherField, editorField, yearOfPublishingField;

  public BookEditForm(BookEditPage parentPage, BookDO data)
  {
    super(parentPage, data);

    this.colspan = 4;
    if (isNew() == true) {
      data.setStatus(BookStatus.PRESENT);
    }
    if (getData().getTaskId() == null) {
      bookDao.setTask(getData(), bookDao.getDefaultTaskId());
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
    final LabelValueChoiceRenderer<BookStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<BookStatus>(this, BookStatus.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
        statusChoiceRenderer);
    statusChoice.setNullValid(false).setRequired(true);
    add(statusChoice);
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
    final StringBuffer buf = new StringBuffer();
    if (getData().getLendOutBy() != null) {
      // Show full user name:
      buf.append(getData().getLendOutBy().getFullname());
      if (getData().getLendOutDate() != null) {
        buf.append(", ");
        // Show lend out date:
        buf.append(DateTimeFormatter.instance().getFormattedDate(getData().getLendOutDate()));
      }
    }
    final Label lendOutStatus = new Label("lendOutStatus", buf.toString());
    bottomRowsFragment.add(lendOutStatus);
    bottomRowsFragment.add(new MaxLengthTextField("lendOutComment", new PropertyModel<String>(data, "lendOutComment")));

    @SuppressWarnings("serial")
    final Button lendOutButton = new Button("button", new Model<String>(getString("lendOut"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.lendOut();
      }
    };
    
    lendOutButton.add(new SimpleAttributeModifier("class","submit "));
    final SingleButtonPanel lendOutButtonPanel = new SingleButtonPanel("lendOutButton", lendOutButton);
    bottomRowsFragment.add(lendOutButtonPanel);

    @SuppressWarnings("serial")
    final Button returnBookButton = new Button("button", new Model<String>(getString("returnBook"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.returnBook();
      }
    };
    returnBookButton.add(new SimpleAttributeModifier("class","reset"));
    final SingleButtonPanel returnBookButtonPanel = new SingleButtonPanel("returnBookButton", returnBookButton);
    bottomRowsFragment.add(returnBookButtonPanel);
    if (getData().getLendOutById() == null) {
      returnBookButtonPanel.setVisible(false);
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
    getData().setSignature(signatureField.getConvertedInput());
    if (bookDao.doesSignatureAlreadyExist(getData()) == true) {
      addComponentError(signatureField, "book.error.signatureAlreadyExists");
    }
    if (StringUtils.isBlank(authorsField.getConvertedInput())
        && StringUtils.isBlank(publisherField.getConvertedInput())
        && StringUtils.isBlank(editorField.getConvertedInput())
        && StringUtils.isBlank(signatureField.getConvertedInput())
        && StringUtils.isBlank(yearOfPublishingField.getConvertedInput())) {
      addComponentError(authorsField, "book.error.toFewFields");
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
