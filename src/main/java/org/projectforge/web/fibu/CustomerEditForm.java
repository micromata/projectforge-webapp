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

package org.projectforge.web.fibu;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeStatus;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.converter.IntegerConverter;

public class CustomerEditForm extends AbstractEditForm<KundeDO, CustomerEditPage>
{
  private static final long serialVersionUID = -6018131069720611834L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomerEditForm.class);

  public CustomerEditForm(CustomerEditPage parentPage, KundeDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final MinMaxNumberField<Integer> nummerField = new MinMaxNumberField<Integer>("nummer", new PropertyModel<Integer>(data, "id"), 0, 999) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(3);
      }
    };
    add(nummerField);
    final RequiredMaxLengthTextField nameField = new RequiredMaxLengthTextField("name", new PropertyModel<String>(data, "name"));
    add(nameField);
    if (isNew() == true) {
      nummerField.add(new FocusOnLoadBehavior());
    } else {
      nummerField.setEnabled(false);
      nameField.add(new FocusOnLoadBehavior());
    }
    add(new MaxLengthTextField("identifier", new PropertyModel<String>(data, "identifier")));
    add(new MaxLengthTextField("division", new PropertyModel<String>(data, "division")));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
    // DropDownChoice status
    final LabelValueChoiceRenderer<KundeStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<KundeStatus>(this, KundeStatus.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
        statusChoiceRenderer);
    statusChoice.setNullValid(false).setRequired(true);
    add(statusChoice);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
