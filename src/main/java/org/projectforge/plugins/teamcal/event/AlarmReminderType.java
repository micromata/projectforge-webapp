/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

/**
 * To define if alert duration type is minute, hour or day.
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public enum AlarmReminderType
{
  MINUTES("M", "plugins.teamcal.event.reminder.MINUTES_BEFORE"), //
  HOURS("H", "plugins.teamcal.event.reminder.HOURS_BEFORE"), //
  DAYS("D", "plugins.teamcal.event.reminder.DAYS_BEFORE");

  private final String type;
  private final String i18n;

  private AlarmReminderType(final String type , final String i18n)
  {
    this.type = type;
    this.i18n = i18n;

  }

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @return the i18n
   */
  public String getI18n()
  {
    return i18n;
  }
}
