/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.timesheet;

import org.projectforge.timesheet.TimesheetDO;

/**
 * Hook which is used to display plugin components at time sheet pages and components.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public interface TimesheetPluginComponentHook
{

  /**
   * Offers the possibility to render an action button which can perform several actions with for given timesheet.
   * 
   * @param wicketId
   * @param timesheet
   * @return
   */
  void renderComponentsToTimesheetEditForm(TimesheetEditForm form, TimesheetDO timesheet);
}
