/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
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
