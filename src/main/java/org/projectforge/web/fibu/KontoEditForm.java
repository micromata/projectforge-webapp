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
import org.apache.wicket.model.PropertyModel;
import org.projectforge.fibu.KontoDO;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;


public class KontoEditForm extends AbstractEditForm<KontoDO, KontoEditPage>
{
  private static final long serialVersionUID = -6018131069720611834L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KontoEditForm.class);

  public KontoEditForm(KontoEditPage parentPage, KontoDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @Override
  protected void init()
  {
    super.init();
    final MinMaxNumberField<Integer> nummerField = new MinMaxNumberField<Integer>("nummer", new PropertyModel<Integer>(data, "nummer"), 0, 99999999);
    add(nummerField);
    final RequiredMaxLengthTextField nameField = new RequiredMaxLengthTextField("identifier", new PropertyModel<String>(data, "bezeichnung"));
    add(nameField);
    if (isNew() == true) {
      nummerField.add(new FocusOnLoadBehavior());
    } else {
      nummerField.setEnabled(false);
      nameField.add(new FocusOnLoadBehavior());
    }
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
