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

package org.projectforge.web.wicket.components;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Panel containing only one check-box.
 * <br/>
 * This component calls setRenderBodyOnly(true). If the outer html element is needed, please call setRenderBodyOnly(false).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@SuppressWarnings("serial")
public class CheckBoxPanel extends Panel
{
  private CheckBox checkBox;

  public CheckBoxPanel(final String id, final IModel<Boolean> model)
  {
    super(id);
    checkBox = new CheckBox("checkBox", model);
    add(checkBox);
    setRenderBodyOnly(true);
  }

  /**
   * 
   * @param id
   * @param model
   * @param preventBubble Should be true for mass update mode in AbstractListPage. Handles double click event (check-box and row).
   */
  public CheckBoxPanel(final String id, final IModel<Boolean> model, final boolean preventBubble)
  {
    this(id, model);
    // Oups, ugly hack: Instead of preventing bubble fake a third click:
    checkBox.add(new SimpleAttributeModifier("onclick", "javascript:$(this).attr('checked', !cb.is(':checked'));"));
  }
}
