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

package org.projectforge.web.fibu;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.KostentraegerStatus;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.IntegerConverter;


public class Kost1EditForm extends AbstractEditForm<Kost1DO, Kost1EditPage>
{
  private static final long serialVersionUID = 7867840580460197749L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost1EditForm.class);

  protected TextField<Integer> nummernkreisField;

  protected TextField<Integer> bereichField;

  protected TextField<Integer> teilbereichField;

  protected TextField<Integer> endzifferField;

  public Kost1EditForm(Kost1EditPage parentPage, Kost1DO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();

    nummernkreisField = new RequiredMinMaxNumberField<Integer>("nummernkreis", new PropertyModel<Integer>(data, "nummernkreis"), 0, 9);
    nummernkreisField.add(new FocusOnLoadBehavior());
    add(nummernkreisField);
    bereichField = new RequiredMinMaxNumberField<Integer>("bereich", new PropertyModel<Integer>(data, "bereich"), 0, 999) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(3);
      }
    };
    add(bereichField);
    teilbereichField = new RequiredMinMaxNumberField<Integer>("teilbereich", new PropertyModel<Integer>(data, "teilbereich"), 0, 99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    add(teilbereichField);
    endzifferField = new RequiredMinMaxNumberField<Integer>("endziffer", new PropertyModel<Integer>(data, "endziffer"), 0, 99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    endzifferField.setRequired(true);
    add(endzifferField);
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
    // DropDownChoice status
    final LabelValueChoiceRenderer<KostentraegerStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<KostentraegerStatus>(this,
        KostentraegerStatus.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("kostentraegerStatus", new PropertyModel(data, "kostentraegerStatus"),
        statusChoiceRenderer.getValues(), statusChoiceRenderer);
    statusChoice.setNullValid(false);
    statusChoice.setRequired(true);
    add(statusChoice);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
