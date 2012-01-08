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

package org.projectforge.web.wicket.components;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Panel containing only one drop down choice box. <br/>
 * This component calls setRenderBodyOnly(true). If the outer html element is needed, please call setRenderBodyOnly(false).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@SuppressWarnings("serial")
public class DropDownChoicePanel<T> extends Panel
{
  public static final String WICKET_ID = "dropDownChoice";

  private DropDownChoice<T> dropDownChoice;

  public DropDownChoicePanel(final String id, final IModel<T> model, List< ? extends T> values, final IChoiceRenderer<T> renderer)
  {
    this(id, new DropDownChoice<T>(WICKET_ID, model, values, renderer));
  }

  public DropDownChoicePanel(final String id, final DropDownChoice<T> dropDownChoice)
  {
    super(id);
    this.dropDownChoice = dropDownChoice;
    add(dropDownChoice);
    setRenderBodyOnly(true);
  }

  public DropDownChoicePanel<T> setNullValid(final boolean nullValid)
  {
    dropDownChoice.setNullValid(nullValid);
    return this;
  }

  public DropDownChoice<T> getDropDownChoice()
  {
    return dropDownChoice;
  }
}
