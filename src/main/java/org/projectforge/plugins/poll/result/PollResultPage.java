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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.NewPollPage;
import org.projectforge.plugins.poll.PollBasePage;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.plugins.poll.PollDao;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDao;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.event.PollEventDao;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollResultPage extends PollBasePage
{
  private static final long serialVersionUID = 7667632498760754905L;

  @SpringBean(name = "pollDao")
  private PollDao pollDao;

  @SpringBean(name = "pollAttendeeDao")
  private PollAttendeeDao pollAttendeeDao;

  @SpringBean(name = "pollEventDao")
  private PollEventDao pollEventDao;

  @SpringBean(name = "pollResultDao")
  private PollResultDao pollResultDao;

  private final PollDO pollDo;

  private final HashSet<PollEventDO> allEvents;

  private final List<PollAttendeeDO> pollAttendeeList;
  /**
   * 
   */
  public PollResultPage(final PageParameters parameters)
  {
    super(parameters);
    NewPollPage.redirectToNewPollPage(parameters);
    pollDo = null;
    allEvents = null;
    pollAttendeeList = null;
  }

  public PollResultPage(final PageParameters parameters, final PollDO pollDo, final HashSet<PollEventDO> allEvents, final List<PollAttendeeDO> pollAttendeeList)
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

    gridBuilder.newGrid16();

    final FieldsetPanel fsTitle = gridBuilder.newFieldset(getString("plugins.poll.new.title"), true).setLabelFor(this);
    fsTitle.add(new Label(fsTitle.newChildId(), pollDo.getTitle()));

    final FieldsetPanel fsLocation = gridBuilder.newFieldset(getString("plugins.poll.new.location"), true).setLabelFor(this);
    fsLocation.add(new Label(fsLocation.newChildId(), pollDo.getLocation()));

    final FieldsetPanel fsDescription = gridBuilder.newFieldset(getString("plugins.poll.new.description"), true).setLabelFor(this);
    fsDescription.add(new Label(fsDescription.newChildId(), pollDo.getDescription()));

    final FieldsetPanel fsUsers = gridBuilder.newFieldset(getString("plugins.poll.attendee.users"), true).setLabelFor(this);
    String userList = "";
    if (pollAttendeeList != null) {
      for (final PollAttendeeDO attendee : pollAttendeeList) {
        if (attendee.getUser() != null)
          userList += attendee.getUser().getFullname() + "; ";
      }
    }
    fsUsers.add(new Label(fsUsers.newChildId(), userList));

    gridBuilder.newGrid16();

    // TODO http://www.wicket-library.com/wicket-examples/mailtemplate/?0
    final FieldsetPanel fsEMails = gridBuilder.newFieldset(getString("plugins.poll.attendee.emails"), true).setLabelFor(this);
    if (pollAttendeeList != null) {
      final SideWaysPanel sideWay = new SideWaysPanel(fsEMails.newChildId(), 6, 6);
      fsEMails.add(sideWay);
      for (final PollAttendeeDO attendee : pollAttendeeList) {
        if (attendee.getEmail() != null)
          sideWay.addLabels(attendee.getEmail(), "");
      }
    }

    if (allEvents != null) {
      final FieldsetPanel fsEvents = gridBuilder.newFieldset(getString("plugins.poll.attendee.events"), true).setLabelFor(this);
      final SideWaysPanel sideWay = new SideWaysPanel(fsEvents.newChildId(), 6, 5);
      fsEvents.add(sideWay);
      for (final PollEventDO event : allEvents) {
        sideWay.addLabels("Start: " + DateFormatUtils.format(event.getStartDate().getMillis(), "dd.MM.yyyy HH:mm"), "Ende: "
            + DateFormatUtils.format(event.getEndDate().getMillis(), "dd.MM.yyyy HH:mm"));
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.poll.result");
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onConfirm()
   */
  @Override
  protected void onConfirm()
  {
    pollDao.save(pollDo);
    final List<PollEventDO> pollEvents = new ArrayList<PollEventDO>();
    pollEvents.addAll(allEvents);
    pollEventDao.save(pollEvents);
    pollAttendeeDao.save(pollAttendeeList);

    // PollResultDO pollResult = new PollResultDO();
    // pollResult.setPollAttendee(pollAttendeeList);
    // pollResult.setPollEvent(pollEvents);

  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onCancel()
   */
  @Override
  protected void onCancel()
  {
    // TODO onCancel
  }

}
