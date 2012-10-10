/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.util.Collection;

import org.projectforge.plugins.teamcal.TeamCalDO;
import org.projectforge.web.calendar.CalendarFilter;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalCalendarFilter extends CalendarFilter
{
  private static final long serialVersionUID = -8318037558891653348L;

  @XStreamAsAttribute
  private Collection<TeamCalDO> assignedItems;

  /**
   * @param assignedItems
   */
  public void setAssignedtItems(final Collection<TeamCalDO> assignedItems)
  {
    this.assignedItems = assignedItems;
  }

  public Collection<TeamCalDO> getAssignedtItems()
  {
    return assignedItems;
  }

}
