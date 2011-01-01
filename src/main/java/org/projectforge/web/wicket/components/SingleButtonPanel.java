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

package org.projectforge.web.wicket.components;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Panel for using as single button (needed for css decoration).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SingleButtonPanel extends Panel
{
  private static final long serialVersionUID = -3241045253014479836L;

  private Button button;

  public SingleButtonPanel(final String id, final Button button)
  {
    super(id);
    this.button = button;
    setRenderBodyOnly(true);
    add(button);
  }

  public SingleButtonPanel(final String id, final Button button, final String tooltip)
  {
    this(id, button, tooltip, null);
  }

  public SingleButtonPanel(final String id, final Button button, final String tooltip, final String classname)
  {
    super(id);
    this.button = button;
    setRenderBodyOnly(true);
    add(button);
    button.add(new SimpleAttributeModifier("title", tooltip));
    if (classname != null) {
      button.add(new SimpleAttributeModifier("class", classname));
    }
  }

  public Button getButton()
  {
    return button;
  }
}
