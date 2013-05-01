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
package org.projectforge.plugins.teamcal.externalsubscription;

import java.util.ArrayList;
import java.util.List;

import org.projectforge.core.I18nEnum;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public enum SubscriptionUpdateInterval implements I18nEnum
{
  FIVE_MINUTES(5L * 60 * 1000, "interval5Min"), //
  ONE_HOUR(60L * 60 * 1000, "interval1h"), //
  ONE_DAY(24L * 60 * 60 * 1000, "interval1d")//
  ;

  private Long interval;

  private String i18nKey;

  private SubscriptionUpdateInterval(final Long interval, final String i18nKey)
  {
    this.interval = interval;
    this.i18nKey = i18nKey;
  }

  public Long getInterval()
  {
    return interval;
  }

  public String getI18nKey()
  {
    return "plugins.teamcal.externalsubscription.updateInterval." + i18nKey;
  }

  public static List<Long> getIntervals()
  {
    final List<Long> result = new ArrayList<Long>();
    for (final SubscriptionUpdateInterval value : values()) {
      result.add(value.getInterval());
    }
    return result;
  }

  public static String getI18nKeyForInterval(final Long interval)
  {
    for (final SubscriptionUpdateInterval value : values()) {
      if (value.getInterval() == interval) {
        return value.getI18nKey();
      }
    }
    return "";
  }
}
