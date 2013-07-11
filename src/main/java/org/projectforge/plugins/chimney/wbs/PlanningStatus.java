/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.projectforge.core.I18nEnum;

public enum PlanningStatus implements I18nEnum
{
  PLANNING("planning"), APPROVED("approved"), OBSOLETE("obsolete");

  private String key;

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  public String getI18nKey()
  {
    return "plugins.chimney.planningstatus." + key;
  }

  PlanningStatus(final String key)
  {
    this.key = key;
  }
}
