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

package org.projectforge.web.mobile;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

/**
 * ul-Panel used for most content areas.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ListViewPanel extends Panel
{
  private static final long serialVersionUID = -5465667274834690310L;

  private RepeatingView repeater;

  public ListViewPanel(final String id)
  {
    super(id);
    repeater = new RepeatingView("itemRepeater");
    add(repeater);
  }

  public String newChildId()
  {
    return repeater.newChildId();
  }

  public ListViewPanel add(final ListViewItemPanel entryPanel)
  {
    repeater.add(entryPanel.init());
    return this;
  }
}
