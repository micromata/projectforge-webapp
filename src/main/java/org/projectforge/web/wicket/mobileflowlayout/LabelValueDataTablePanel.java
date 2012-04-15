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

package org.projectforge.web.wicket.mobileflowlayout;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.address.AddressMobileViewPage;
import org.projectforge.web.mobile.CollapsiblePanel;

/**
 * Represents a table with two columns. This table is used for displaying data objects (refer {@link AddressMobileViewPage} as an example).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LabelValueDataTablePanel extends Panel
{
  private static final long serialVersionUID = 384228381018466893L;

  private RepeatingView rows;

  /**
   */
  public LabelValueDataTablePanel(final CollapsiblePanel parent)
  {
    this(parent.newChildId());
    parent.add(this);
  }

  public LabelValueDataTablePanel(final String id)
  {
    super(id);
    add(rows = new RepeatingView("row"));
  }

  public void addRow(final String label, final String value)
  {
    final WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
    rows.add(row);
    row.add(new Label("label", label));
    row.add(new Label("value", value));
  }
}
