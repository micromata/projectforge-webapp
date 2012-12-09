/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalsComparator;
import org.projectforge.plugins.teamcal.admin.TeamCalsProvider;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TeamEventListForm extends AbstractListForm<TeamEventFilter, TeamEventListPage>
{
  private static final long serialVersionUID = 3659495003810851072L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventListForm.class);

  MultiChoiceListHelper<TeamCalDO> calendarsListHelper;

  public TeamEventListForm(final TeamEventListPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected TeamEventFilter newSearchFilterInstance()
  {
    return new TeamEventFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#init()
   */
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newColumnsPanel();
    gridBuilder.newColumnPanel(DivType.COL_66);
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
      fs.setOutputMarkupId(true);
    }
    {
      // Team calendar
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.calendar"));// .setLabelSide(false);
      final TeamCalsProvider calendarProvider = new TeamCalsProvider();
      calendarsListHelper = new MultiChoiceListHelper<TeamCalDO>().setComparator(new TeamCalsComparator()).setFullList(
          calendarProvider.getSortedCalenders());
      final Collection<TeamCalDO> list = getFilter().getTeamCals();
      if (list != null) {
        for (final TeamCalDO cal : list) {
          calendarsListHelper.addOriginalAssignedItem(cal).assignItem(cal);
        }
      }
      final Select2MultiChoice<TeamCalDO> calendars = new Select2MultiChoice<TeamCalDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<TeamCalDO>>(this.calendarsListHelper, "assignedItems"), calendarProvider);
      fs.add(calendars);
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_33);
      addPageSizeFieldset();
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @return the filter
   */
  public TeamEventFilter getFilter()
  {
    return getSearchFilter();
  }
}
