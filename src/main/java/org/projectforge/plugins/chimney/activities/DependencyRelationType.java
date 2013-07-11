/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import org.projectforge.core.I18nEnum;

/**
 * Represents the four different relation types between activities:
 * Begin-Begin, Begin-End, End-Begin, End-End
 * @author Sweeps <pf@byte-storm.com>
 */
public enum DependencyRelationType implements I18nEnum
{
  BEGIN_BEGIN("plugins.chimney.enum.dependencyrelationtype.begin_begin"),
  BEGIN_END("plugins.chimney.enum.dependencyrelationtype.begin_end"),
  END_BEGIN("plugins.chimney.enum.dependencyrelationtype.end_begin"),
  END_END("plugins.chimney.enum.dependencyrelationtype.end_end");

  private String key;

  private DependencyRelationType(final String key)
  {
    this.key = key;
  }

  @Override
  public String getI18nKey()
  {
    return key;
  }

}
