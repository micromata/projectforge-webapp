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

import static org.projectforge.web.wicket.layout.LayoutLength.DOUBLE;
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
import org.projectforge.orga.PosteingangDO;
import org.projectforge.orga.PosteingangDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class PosteingangFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = 2532546903021383010L;

  private PosteingangDao posteingangDao;

  private PosteingangDO data;

  private ISelectCallerPage callerPage;

  protected DatePanel datumPanel;

  public PosteingangFormRenderer(final MarkupContainer container, final ISelectCallerPage callerPage, final LayoutContext layoutContext,
      final PosteingangDao posteingangDao, final PosteingangDO data)
  {
    super(container, layoutContext);
    this.data = data;
    this.callerPage = callerPage;
    this.posteingangDao = posteingangDao;
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
      doPanel.addDateFieldPanel(data, "datum", getString("date"), HALF, datumPanel, FULL);
    }
    {
      final PFAutoCompleteMaxLengthTextField absenderTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "absender")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return posteingangDao.getAutocompletion("absender", input);
        }
      };
      absenderTextField.withMatchContains(true).withMinChars(2).withFocus(true);
      absenderTextField.setRequired(true);
      doPanel.addTextField(getString("orga.posteingang.absender"), HALF, absenderTextField, DOUBLE).setStrong();
    }
    {
      final PFAutoCompleteMaxLengthTextField personTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "person")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return posteingangDao.getAutocompletion("person", input);
        }
      };
      personTextField.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(getString("orga.posteingang.person"), HALF, personTextField, DOUBLE);
    }
    {
      final PFAutoCompleteMaxLengthTextField inhaltTextField = new PFAutoCompleteMaxLengthTextField(TextFieldLPanel.INPUT_ID,
          new PropertyModel<String>(data, "inhalt")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return posteingangDao.getAutocompletion("inhalt", input);
        }
      };
      inhaltTextField.withMatchContains(true).withMinChars(2);
      inhaltTextField.setRequired(true);
      doPanel.addTextField(getString("orga.post.inhalt"), HALF, inhaltTextField, DOUBLE).setStrong();
    }
    doPanel.addTextArea(data, "bemerkung", getString("comment"), HALF, DOUBLE, false).setCssStyle("height: 20em;");
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<PostType> typeChoiceRenderer = new LabelValueChoiceRenderer<PostType>(container, PostType.values());
      @SuppressWarnings("unchecked")
      final DropDownChoice typeChoice = new DropDownChoice(DropDownChoiceLPanel.SELECT_ID, new PropertyModel(data, "type"), typeChoiceRenderer.getValues(),
          typeChoiceRenderer);
      typeChoice.setNullValid(false);
      typeChoice.setRequired(true);
      doPanel.addDropDownChoice(data, "type", getString("orga.post.type"), HALF, typeChoice, FULL);
    }
  }
}
