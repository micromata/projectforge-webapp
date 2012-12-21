/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import org.apache.commons.lang.StringUtils;



/**
 * Utility class for recurrency handling
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class RecurrencyUtil
{

  public static boolean isEventRecurrent(final TeamEventDO event)
  {
    // TODO kai: is this correct?
    return StringUtils.isNotBlank(event.getRecurrenceRule());
  }
}
