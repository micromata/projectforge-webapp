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
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public enum TeamEventMailType
{
  INVITATION("invitation"), UPDATE("update"), REJECTION("rejection");

  private String key;

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  TeamEventMailType(final String key)
  {
    this.key = key;
  }
}
