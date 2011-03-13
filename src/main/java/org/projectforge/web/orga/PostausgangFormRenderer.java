/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.orga;

import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;

import java.util.Date;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.calendar.DayHolder;
import org.projectforge.orga.PostType;
import org.projectforge.orga.PostausgangDO;
import org.projectforge.orga.PostausgangDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class PostausgangFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = 2532546903021383010L;

  private PostausgangDao postausgangDao;

  private PostausgangDO data;

  private ISelectCallerPage callerPage;

  protected DatePanel datumPanel;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.DOUBLE;

  public PostausgangFormRenderer(final MarkupContainer container, final ISelectCallerPage callerPage, final LayoutContext layoutContext,
      final PostausgangDao postausgangDao, final PostausgangDO data)
  {
    super(container, layoutContext);
    this.data = data;
    this.callerPage = callerPage;
    this.postausgangDao = postausgangDao;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    {
      datumPanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "datum"), DatePanelSettings.get()
          .withCallerPage(callerPage).withTargetType(java.sql.Date.class).withSelectProperty("datum"));
      datumPanel.setRequired(true);
      datumPanel.add(new AbstractValidator<Date>() {
        @Override
        protected void onValidate(IValidatable<Date> validatable)
        {
          final Date value = validatable.getValue();
          if (value == null) {
            return;
          }
          final DayHolder today = new DayHolder();
          final DayHolder date = new DayHolder(value);
          if (today.before(date) == true) { // No dates in the future accepted.
            error(validatable);
          }
        }

        @Override
        protected String resourceKey()
        {
          return "error.dateInFuture";
        }
      });
      doPanel.addDateFieldPanel(datumPanel, new PanelContext(data, "datum", FULL, getString("date"), HALF));
    }
    {
      final PFAutoCompleteMaxLengthTextField empfaengerTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "empfaenger")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return postausgangDao.getAutocompletion("empfaenger", input);
        }
      };
      empfaengerTextField.withMatchContains(true).withMinChars(2).withFocus(true);
      empfaengerTextField.setRequired(true);
      doPanel.addTextField(empfaengerTextField, new PanelContext(VALUE_LENGTH, getString("orga.postausgang.empfaenger"), LABEL_LENGTH)
          .setStrong());
    }
    {
      final PFAutoCompleteMaxLengthTextField personTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "person")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return postausgangDao.getAutocompletion("person", input);
        }
      };
      personTextField.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(personTextField, new PanelContext(VALUE_LENGTH, getString("orga.postausgang.person"), LABEL_LENGTH));
    }
    {
      final PFAutoCompleteMaxLengthTextField inhaltTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "inhalt")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return postausgangDao.getAutocompletion("inhalt", input);
        }
      };
      inhaltTextField.withMatchContains(true).withMinChars(2);
      inhaltTextField.setRequired(true);
      doPanel.addTextField(inhaltTextField, new PanelContext(VALUE_LENGTH, getString("orga.post.inhalt"), LABEL_LENGTH).setStrong());
    }
    doPanel.addTextArea(new PanelContext(data, "bemerkung", VALUE_LENGTH, getString("comment"), LABEL_LENGTH).setCssStyle("height: 20em;"));
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<PostType> typeChoiceRenderer = new LabelValueChoiceRenderer<PostType>(container, PostType.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice typeChoice = new DropDownChoice(DropDownChoiceLPanel.SELECT_ID, new PropertyModel(data, "type"),
          typeChoiceRenderer.getValues(), typeChoiceRenderer);
      typeChoice.setNullValid(false);
      typeChoice.setRequired(true);
      doPanel.addDropDownChoice(typeChoice, new PanelContext(data, "type", FULL, getString("orga.post.type"), LABEL_LENGTH));
    }
  }
}
