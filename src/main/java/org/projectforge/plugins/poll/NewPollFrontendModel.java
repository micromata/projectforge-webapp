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

import org.apache.wicket.injection.Injector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDao;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.event.PollEventDao;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

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

  private PollDO pollDo;

  private List<PollEventDO> allEvents;

  private List<PollAttendeeDO> pollAttendeeList;

  private final List<GroupDO> pollGroupList;

  private final List<PollAttendeeDO> calculatedUserList;

  private boolean exist;

  @SpringBean(name = "pollAttendeeDao")
  private PollAttendeeDao pollAttendeeDao;

  @SpringBean(name = "pollEventDao")
  private PollEventDao pollEventDao;

  @SpringBean(name = "pollDao")
  private PollDao pollDao;

  /**
   * 
   */
  public NewPollFrontendModel(final PollDO pollDo)
  {
    Injector.get().inject(this);
    this.pollDo = pollDo;
    this.exist = false;
    this.allEvents = new LinkedList<PollEventDO>();
    this.pollAttendeeList = new LinkedList<PollAttendeeDO>();
    this.pollGroupList = new LinkedList<GroupDO>();
    this.calculatedUserList = new LinkedList<PollAttendeeDO>();
  }

  public void initModelByPoll()
  {
    try {
      pollDo = pollDao.getById(pollDo.getId());
      if (pollDo != null) {
        pollAttendeeList = pollAttendeeDao.getListByPoll(pollDo);
        allEvents = pollEventDao.getListByPoll(pollDo);
        exist = true;
      }
    } catch (Exception ex) {
      // TODO log entry
    }
  }

  public boolean isExisting()
  {
    return exist;
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

  /**
   * @return the pollGroupList
   */
  public List<GroupDO> getPollGroupList()
  {
    return pollGroupList;
  }

  /**
   * @return the calculatedUserList
   */
  public List<PollAttendeeDO> getCalculatedAttendeeList()
  {
    return calculatedUserList;
  }

  public List<PFUserDO> getUserDoFromAttendees()
  {
    final List<PFUserDO> result = new LinkedList<PFUserDO>();
    for (final PollAttendeeDO attendee : getPollAttendeeList()) {
      if (attendee.getUser() != null) {
        result.add(attendee.getUser());
      }
    }
    return result;
  }
}
