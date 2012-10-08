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

package org.projectforge.plugins.teamcal;

import java.util.Collection;

import org.projectforge.web.calendar.CalendarFilter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Some settings for the SelectDateAction stored in the flow scope (configurable by the caller).
 */
@XStreamAlias("calendarFilter")
public class TeamCalendarFilter extends CalendarFilter
{
  private static final long serialVersionUID = 8977058375010602550L;

  @XStreamAsAttribute
  private Collection<TeamCalDO> assignedItems;

  public TeamCalendarFilter()
  {
  }

  /**
   * @param assignedItems
   */
  public void setAssignedtItems(final Collection<TeamCalDO> assignedItems)
  {
    this.assignedItems = assignedItems;
  }

  public Collection<TeamCalDO> getAssignedtItems()
  {
    return assignedItems;
  }
}
