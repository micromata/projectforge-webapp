/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import org.projectforge.core.I18nEnum;

/**
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public enum GanttActivityVisualizationType implements I18nEnum
{
  MILESTONE("plugins.chimney.enum.ganttactivityvisualizationtype.milestone"),
  BAR("plugins.chimney.enum.ganttactivityvisualizationtype.bar"),
  SPAN("plugins.chimney.enum.ganttactivityvisualizationtype.span");

  private String key;

  private GanttActivityVisualizationType(final String key)
  {
    this.key = key;
  }

  @Override
  public String getI18nKey()
  {
    return key;
  }
}
