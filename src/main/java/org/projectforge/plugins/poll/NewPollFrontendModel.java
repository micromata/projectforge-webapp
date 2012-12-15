/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.event.PollEventDO;

/**
 * Data object which is <b>only</b> used for the model chaining in the "new poll" workflow which contains:<br/>
 * - Setting meta information<br/>
 * - Setting poll related date<br/>
 * - Setting poll related attendees<br/>
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class NewPollFrontendModel implements Serializable
{
  private static final long serialVersionUID = -6709402512895730321L;

  private final PollDO pollDo;

  private final List<PollEventDO> allEvents;

  private final List<PollAttendeeDO> pollAttendeeList;

  /**
   * 
   */
  public NewPollFrontendModel(final PollDO pollDo)
  {
    this.pollDo = pollDo;
    this.allEvents = new LinkedList<PollEventDO>();
    this.pollAttendeeList = new LinkedList<PollAttendeeDO>();
  }

  /**
   * @return the pollDo
   */
  public PollDO getPollDo()
  {
    return pollDo;
  }

  /**
   * @return the allEvents
   */
  public List<PollEventDO> getAllEvents()
  {
    return allEvents;
  }

  /**
   * @return the pollAttendeeList
   */
  public List<PollAttendeeDO> getPollAttendeeList()
  {
    return pollAttendeeList;
  }

}
