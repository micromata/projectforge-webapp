/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.access;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.AccessType;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;

/**
 * Rows of access rights (without header).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AccessEditTablePanel extends Panel
{
  private static final long serialVersionUID = -7347864057331989812L;

  private final GroupTaskAccessDO data;

  public AccessEditTablePanel(final String id, final GroupTaskAccessDO data)
  {
    super(id);
    this.data = data;
  }

  public AccessEditTablePanel init()
  {
    final RepeatingView rowRepeater = new RepeatingView("accessRows");
    add(rowRepeater);
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TASK_ACCESS_MANAGEMENT));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TASKS));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.TIMESHEETS));
    addAccessRow(rowRepeater, data.ensureAndGetAccessEntry(AccessType.OWN_TIMESHEETS));
    return this;
  }

  private void addAccessRow(final RepeatingView rowRepeater, final AccessEntryDO accessEntry)
  {
    final WebMarkupContainer row = new WebMarkupContainer(rowRepeater.newChildId());
    rowRepeater.add(row);
    row.add(new Label("area", getString(accessEntry.getAccessType().getI18nKey())));
    final DivPanel groupPanel = new DivPanel("checkboxes", DivType.CHECKBOX);
    row.add(groupPanel);
    groupPanel.add(new CheckBoxPanel(groupPanel.newChildId(), new PropertyModel<Boolean>(accessEntry, "accessSelect"), getString("access.type.select")));
    groupPanel.add(new CheckBoxPanel(groupPanel.newChildId(), new PropertyModel<Boolean>(accessEntry, "accessInsert"), getString("access.type.insert")));
    groupPanel.add(new CheckBoxPanel(groupPanel.newChildId(), new PropertyModel<Boolean>(accessEntry, "accessUpdate"), getString("access.type.update")));
    groupPanel.add(new CheckBoxPanel(groupPanel.newChildId(), new PropertyModel<Boolean>(accessEntry, "accessDelete"), getString("access.type.delete")));
  }
}
