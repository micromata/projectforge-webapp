/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import org.projectforge.core.I18nEnum;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public enum ReminderActionType implements I18nEnum
{
  MESSAGE("DISPLAY", "plugins.teamcal.event.reminder.MESSAGE"), //
  MESSAGE_SOUND("AUDIO", "plugins.teamcal.event.reminder.MESSAGE_SOUND");

  private final String type;

  private final String i18n;

  private ReminderActionType(final String type, final String i18n)
  {
    this.i18n = i18n;
    this.type = type;
  }

  /**
   * @return the type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @see org.projectforge.core.I18nEnum#getI18nKey()
   */
  @Override
  public String getI18nKey()
  {
    return i18n;
  }
}
