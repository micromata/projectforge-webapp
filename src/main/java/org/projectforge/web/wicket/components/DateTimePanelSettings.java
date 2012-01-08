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



/**
 * Fluent design pattern.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DateTimePanelSettings extends DatePanelSettings
{
  private static final long serialVersionUID = 2568255629162030005L;

  public static DateTimePanelSettings get()
  {
    return new DateTimePanelSettings();
  }

  protected Integer tabIndex;

  /**
   * Default is null.
   * @param tabIndex Use tabIndex as html tab index of date field (if visible), hours and minutes.
   * @return this
   */
  public DateTimePanelSettings withTabIndex(Integer tabIndex)
  {
    this.tabIndex = tabIndex;
    return this;
  }
}
