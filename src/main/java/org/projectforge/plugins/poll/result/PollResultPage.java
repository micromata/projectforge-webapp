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

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.poll.PollBasePage;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollResultPage extends PollBasePage
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

    gridBuilder.newGrid16();

    FieldsetPanel fsTitle = gridBuilder.newFieldset("Title", true).setLabelFor(this);
    fsTitle.add(new Label(fsTitle.newChildId(), pollDo.getTitle()));

    FieldsetPanel fsLocation = gridBuilder.newFieldset("Locatoin", true).setLabelFor(this);
    fsLocation.add(new Label(fsLocation.newChildId(), pollDo.getLocation()));

    FieldsetPanel fsDescription = gridBuilder.newFieldset("Description", true).setLabelFor(this);
    fsDescription.add(new Label(fsDescription.newChildId(), pollDo.getDescription()));

    FieldsetPanel fsUsers = gridBuilder.newFieldset("Users", true).setLabelFor(this);
    String userList = "";
    if (pollAttendeeList != null) {
      for (PollAttendeeDO attendee : pollAttendeeList) {
        if (attendee.getUser() != null)
          userList += attendee.getUser().getFullname() + "; ";
      }
    }
    fsUsers.add(new Label(fsUsers.newChildId(), userList));

    // FieldsetPanel fsGroups = gridBuilder.newFieldset("Groups", true);
    // for (PollAttendeeDO attendee : pollAttendeeList) {
    // fsGroups.add(new Label(fsTitle.newChildId(), attendee.get));
    // }
    gridBuilder.newGrid16();

    // TODO http://www.wicket-library.com/wicket-examples/mailtemplate/?0
    FieldsetPanel fsEMails = gridBuilder.newFieldset("EmailListe", true).setLabelFor(this);
    // String emailList = "";
    if (pollAttendeeList != null) {
      SideWaysPanel sideWay = new SideWaysPanel(fsEMails.newChildId(), 6, 6);
      fsEMails.add(sideWay);// new Label(fsEMails.newChildId(), emailList));
      for (PollAttendeeDO attendee : pollAttendeeList) {
        if (attendee.getEmail() != null)
          sideWay.addLabels(attendee.getEmail(), "");
      }
    }

    if (allEvents != null) {
      FieldsetPanel fsEvents = gridBuilder.newFieldset("Events", true).setLabelFor(this);
      SideWaysPanel sideWay = new SideWaysPanel(fsEvents.newChildId(), 6, 5);
      fsEvents.add(sideWay);
      for (PollEventDO event : allEvents) {
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
    // TODO add title
    return "addf";
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onConfirm()
   */
  @Override
  protected void onConfirm()
  {
    // TODO onConfirm
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
