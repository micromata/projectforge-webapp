/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.book.BookDO;
import org.projectforge.book.BookDao;
import org.projectforge.book.BookStatus;
import org.projectforge.book.BookType;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.PlainLabel;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LayoutAlignment;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class BookFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private final BookDao bookDao;

  private BookDO data;

  private BookEditPage bookEditPage;

  protected TextField<String> authorsField, signatureField, publisherField, editorField, yearOfPublishingField;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.DOUBLE;

  public BookFormRenderer(final BookEditPage bookEditPage, final MarkupContainer container, final LayoutContext layoutContext,
      final BookDao bookDao, final BookDO data)
  {
    super(container, layoutContext);
    this.bookEditPage = bookEditPage;
    this.data = data;
    this.bookDao = bookDao;
  }

  protected void validation()
  {
    signatureField.validate();
    authorsField.validate();
    publisherField.validate();
    editorField.validate();
    yearOfPublishingField.validate();
    data.setSignature(signatureField.getConvertedInput());
    if (bookDao.doesSignatureAlreadyExist(data) == true) {
      signatureField.error(getString("book.error.signatureAlreadyExists"));
    }
    if (StringUtils.isBlank(authorsField.getConvertedInput())
        && StringUtils.isBlank(publisherField.getConvertedInput())
        && StringUtils.isBlank(editorField.getConvertedInput())
        && StringUtils.isBlank(signatureField.getConvertedInput())
        && StringUtils.isBlank(yearOfPublishingField.getConvertedInput())) {
      authorsField.error(getString("book.error.toFewFields"));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void add()
  {
    doPanel.addTextField(new PanelContext(data, "title", VALUE_LENGTH, getString("book.title"), LABEL_LENGTH).setStrong().setRequired()
        .setFocus());
    IField field;
    field = doPanel.addTextField(new PanelContext(data, "authors", VALUE_LENGTH, getString("book.authors"), LABEL_LENGTH));
    if (field instanceof TextFieldLPanel) {
      authorsField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    {
      // DropDownChoice bookType
      final LabelValueChoiceRenderer<BookType> bookTypeChoiceRenderer = new LabelValueChoiceRenderer<BookType>(container, BookType.values());
      final DropDownChoice<BookType> bookTypeChoice = new DropDownChoice<BookType>(SELECT_ID, new PropertyModel<BookType>(data, "type"),
          bookTypeChoiceRenderer.getValues(), bookTypeChoiceRenderer);
      bookTypeChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(bookTypeChoice, new PanelContext(data, "type", THREEQUART, getString("book.type"), HALF));
    }
    final String yearLabel = getString("book.yearOfPublishing");
    doPanel.addLabel(yearLabel, new PanelContext(FULL).setAlignment(LayoutAlignment.RIGHT).setBreakBeforeLabel(false));
    field = doPanel.addTextField(new PanelContext(data, "yearOfPublishing", QUART, yearLabel));
    if (field instanceof TextFieldLPanel) {
      yearOfPublishingField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    {
      // DropDownChoice bookStatus
      final LabelValueChoiceRenderer<BookStatus> bookStatusChoiceRenderer = new LabelValueChoiceRenderer<BookStatus>(container, BookStatus
          .values());
      final DropDownChoice<BookStatus> bookStatusChoice = new DropDownChoice<BookStatus>(SELECT_ID, new PropertyModel<BookStatus>(data,
          "status"), bookStatusChoiceRenderer.getValues(), bookStatusChoiceRenderer);
      bookStatusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(bookStatusChoice, new PanelContext(data, "status", THREEQUART, getString("status"), LABEL_LENGTH));
    }
    field = doPanel.addTextField(new PanelContext(data, "signature", THREEQUART, getString("book.signature"), LABEL_LENGTH));
    if (field instanceof TextFieldLPanel) {
      signatureField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    final String isbnLabel = getString("book.isbn");
    doPanel.addLabel(isbnLabel, new PanelContext(HALF).setAlignment(LayoutAlignment.RIGHT).setBreakBeforeLabel(false));
    doPanel.addTextField(new PanelContext(data, "isbn", THREEQUART, isbnLabel));
    doPanel.addTextField(new PanelContext(data, "keywords", VALUE_LENGTH, getString("book.keywords"), LABEL_LENGTH));
    field = doPanel.addTextField(new PanelContext(data, "publisher", VALUE_LENGTH, getString("book.publisher"), LABEL_LENGTH));
    if (field instanceof TextFieldLPanel) {
      publisherField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    field = doPanel.addTextField(new PanelContext(data, "editor", VALUE_LENGTH, getString("book.editor"), LABEL_LENGTH));
    if (field instanceof TextFieldLPanel) {
      editorField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    doPanel.addTextArea(new PanelContext(data, "abstractText", VALUE_LENGTH, getString("book.abstract"), LABEL_LENGTH)
        .setCssStyle("height: 10em;"));
    doPanel.addTextArea(new PanelContext(data, "comment", VALUE_LENGTH, getString("comment"), LABEL_LENGTH).setCssStyle("height: 10em;"));

    if (isNew() == false) {
      doPanel.addLabel(getString("book.lending"), new PanelContext(HALF));
      final StringBuffer buf = new StringBuffer();
      if (data.getLendOutBy() != null) {
        // Show full user name:
        buf.append(data.getLendOutBy().getFullname());
        if (data.getLendOutDate() != null) {
          buf.append(", ");
          // Show lend out date:
          buf.append(DateTimeFormatter.instance().getFormattedDate(data.getLendOutDate()));
        }
      }
      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(LayoutLength.ONEHALF)).getRepeatingView();
      repeatingView.add(new PlainLabel(repeatingView.newChildId(), buf.toString()));
      @SuppressWarnings("serial")
      final Button lendOutButton = new Button("button", new Model<String>(getString("book.lendOut"))) {
        @Override
        public final void onSubmit()
        {
          bookEditPage.lendOut();
        }
      };
      lendOutButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
      final SingleButtonPanel lendOutButtonPanel = new SingleButtonPanel(repeatingView.newChildId(), lendOutButton);
      repeatingView.add(lendOutButtonPanel);

      @SuppressWarnings("serial")
      final Button returnBookButton = new Button("button", new Model<String>(getString("book.returnBook"))) {
        @Override
        public final void onSubmit()
        {
          bookEditPage.returnBook();
        }
      };
      returnBookButton.add(WebConstants.BUTTON_CLASS_RESET);
      final SingleButtonPanel returnBookButtonPanel = new SingleButtonPanel(repeatingView.newChildId(), returnBookButton);
      repeatingView.add(returnBookButtonPanel);
      if (data.getLendOutById() == null) {
        returnBookButtonPanel.setVisible(false);
      }
      doPanel.addTextArea(new PanelContext(data, "lendOutComment", VALUE_LENGTH, getString("book.lendOutNote"), LABEL_LENGTH)
          .setCssStyle("height: 10em;"));
    }
  }
}
