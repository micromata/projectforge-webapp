/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.calendar;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;

import org.projectforge.user.PFUserDO;

/**
 * @author Johannes Unterstein(j.unterstein@micromata.de)
 * 
 */
public interface CalendarFeedHook
{

  /**
   * 
   * @param req
   */
  void onInit(HttpServletRequest req);

  /**
   * @param user
   * @param timezone
   * @param cal
   */
  List<VEvent> getEvents(PFUserDO user, final TimeZone timezone, final java.util.Calendar cal);

}
