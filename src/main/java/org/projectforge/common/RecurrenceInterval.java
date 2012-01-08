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

package org.projectforge.common;

import org.projectforge.core.I18nEnum;


/**
 * Some objects need the attribute of a recurrence. This will be given as an interval.
 */
public enum RecurrenceInterval implements I18nEnum
{
  YEAR("year"), QUARTER_YEAR("quarteryear"), MONTH("month"), WEEK("week"), DAY("day"), HOUR("hour");

  private String key;

  @Override
  public String getI18nKey()
  {
    return "common.recurranceintervall." + key;
  }

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  RecurrenceInterval(final String key)
  {
    this.key = key;
  }

  public boolean isIn(final RecurrenceInterval... recurrance) {
    for (final RecurrenceInterval rc : recurrance) {
      if (this == rc) {
        return true;
      }
    }
    return false;
  }
}
