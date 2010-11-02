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

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.IntegerConverter;

public class Kost2ArtEditForm extends AbstractEditForm<Kost2ArtDO, Kost2ArtEditPage>
{
  private static final long serialVersionUID = 1207258100682337083L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Kost2ArtEditForm.class);

  public Kost2ArtEditForm(Kost2ArtEditPage parentPage, Kost2ArtDO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();

    final TextField<Integer> nummerField = new RequiredMinMaxNumberField<Integer>("nummer", new PropertyModel<Integer>(data, "id"), 0,
        99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    if (isNew() == false) {
      nummerField.setEnabled(false);
    }
    add(nummerField);
    add(new CheckBox("fakturiertCheckBox", new PropertyModel<Boolean>(data, "fakturiert")));
    add(new CheckBox("projektStandardCheckBox", new PropertyModel<Boolean>(data, "projektStandard")));
    add(new MaxLengthTextField("name", new PropertyModel<String>(data, "name")));
    add(new MinMaxNumberField<BigDecimal>("workFraction", new PropertyModel<BigDecimal>(data, "workFraction"), BigDecimal.ZERO,
        BigDecimal.ONE));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
