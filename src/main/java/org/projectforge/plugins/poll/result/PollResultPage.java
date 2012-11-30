/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.result;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.web.wicket.AbstractSecuredPage;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollResultPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 7667632498760754905L;

  private final PollDO pollDo;

  private final Collection<PollEventDO> allEvents;

  private final List<PollAttendeeDO> pollAttendeeList;

  public PollResultPage(PageParameters parameters, PollDO pollDo, Collection<PollEventDO> allEvents, List<PollAttendeeDO> pollAttendeeList)
  {
    super(parameters);

    this.pollDo = pollDo;
    this.allEvents = allEvents;
    this.pollAttendeeList = pollAttendeeList;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    // TODO add title
    return "addf";
  }

}
