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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.calendar.DayHolder;
import org.projectforge.orga.PostType;
import org.projectforge.orga.PosteingangDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;


public class PosteingangEditForm extends AbstractEditForm<PosteingangDO, PosteingangEditPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PosteingangEditForm.class);

  protected DatePanel datumPanel;

  public PosteingangEditForm(PosteingangEditPage parentPage, PosteingangDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    datumPanel = new DatePanel("datum", new PropertyModel<Date>(data, "datum"), DatePanelSettings.get().withCallerPage(parentPage)
        .withTargetType(java.sql.Date.class));
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

    add(datumPanel);
    PFAutoCompleteMaxLengthTextField absenderTextField = new PFAutoCompleteMaxLengthTextField("absender",
        new PropertyModel<String>(data, "absender")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("absender", input);
      }
    };
    absenderTextField.withMatchContains(true).withMinChars(2).withFocus(true);
    absenderTextField.setRequired(true);
    add(absenderTextField);
    PFAutoCompleteMaxLengthTextField personTextField = new PFAutoCompleteMaxLengthTextField("person",
        new PropertyModel<String>(data, "person")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("person", input);
      }
    };
    personTextField.withMatchContains(true).withMinChars(2);
    add(personTextField);
    PFAutoCompleteMaxLengthTextField inhaltTextField = new PFAutoCompleteMaxLengthTextField("inhalt",
        new PropertyModel<String>(data, "inhalt")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("inhalt", input);
      }
    };
    inhaltTextField.withMatchContains(true).withMinChars(2);
    add(inhaltTextField);
    inhaltTextField.setRequired(true);
    add(inhaltTextField);
    add(new MaxLengthTextArea("bemerkung", new PropertyModel<String>(data, "bemerkung")));
    // DropDownChoice status
    final LabelValueChoiceRenderer<PostType> typeChoiceRenderer = new LabelValueChoiceRenderer<PostType>(this, PostType.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice typeChoice = new DropDownChoice("type", new PropertyModel(data, "type"), typeChoiceRenderer.getValues(),
        typeChoiceRenderer);
    typeChoice.setNullValid(false);
    typeChoice.setRequired(true);
    add(typeChoice);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
