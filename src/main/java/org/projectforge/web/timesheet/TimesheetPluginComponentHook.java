/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.timesheet;

import org.apache.wicket.Component;

/**
 * Hook which is used to display plugin components at time sheet pages and components.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public interface TimesheetPluginComponentHook
{

  Component renderComponentToTimesheetEditPage(String wicketId);
}
