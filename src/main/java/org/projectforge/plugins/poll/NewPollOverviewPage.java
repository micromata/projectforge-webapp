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

package org.projectforge.plugins.poll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDao;
import org.projectforge.plugins.poll.attendee.PollAttendeeDisabledChoiceProvider;
import org.projectforge.plugins.poll.attendee.PollAttendeePage;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.event.PollEventDao;
import org.projectforge.plugins.poll.event.PollEventDisabledChoiceProvider;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class NewPollOverviewPage extends PollBasePage
{
  private static final long serialVersionUID = 7667632498760754905L;

  @SpringBean(name = "pollDao")
  private PollDao pollDao;

  @SpringBean(name = "pollAttendeeDao")
  private PollAttendeeDao pollAttendeeDao;

  @SpringBean(name = "pollEventDao")
  private PollEventDao pollEventDao;

  private final NewPollFrontendModel model;

  /**
   * 
   */
  public NewPollOverviewPage(final PageParameters parameters)
  {
    super(parameters);
    NewPollPage.redirectToNewPollPage(parameters);
    this.model = null;
  }

  public NewPollOverviewPage(final PageParameters parameters, final NewPollFrontendModel model)
  {
    super(parameters);
    this.model = model;

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
    fsTitle.add(new Label(fsTitle.newChildId(), model.getPollDo().getTitle()));

    final FieldsetPanel fsLocation = gridBuilder.newFieldset(getString("plugins.poll.new.location"), true).setLabelFor(this);
    fsLocation.add(new Label(fsLocation.newChildId(), model.getPollDo().getLocation()));

    final FieldsetPanel fsDescription = gridBuilder.newFieldset(getString("plugins.poll.new.description"), true).setLabelFor(this);
    fsDescription.add(new Label(fsDescription.newChildId(), model.getPollDo().getDescription()));

    final FieldsetPanel fsUsers = gridBuilder.newFieldset(getString("plugins.poll.attendee.users"), true).setLabelFor(this);

    createDisabledChoices(fsUsers, model.getCalculatedAttendeeList(), true);

    gridBuilder.newGrid16();

    final FieldsetPanel fsEMails = gridBuilder.newFieldset(getString("plugins.poll.attendee.emails"), true).setLabelFor(this);
    createDisabledChoices(fsEMails, model.getCalculatedAttendeeList(), false);

    final FieldsetPanel fsEvents = gridBuilder.newFieldset(getString("plugins.poll.attendee.events"), true).setLabelFor(this);
    createDisabledChoices(fsEvents, model.getAllEvents());
  }

  /**
   * @param fieldset
   * @param modelList
   * @param b
   */
  private void createDisabledChoices(final FieldsetPanel fieldset, final List<PollAttendeeDO> rawList, final boolean isUser)
  {
    final List<PollAttendeeDO> modelList = new LinkedList<PollAttendeeDO>();
    for (final PollAttendeeDO attendee : rawList) {
      if (attendee.getUser() != null && isUser == true) {
        modelList.add(attendee);
      } else if (attendee.getEmail() != null && isUser == false) {
        modelList.add(attendee);
      }
    }
    final MultiChoiceListHelper<PollAttendeeDO> assignHelper = new MultiChoiceListHelper<PollAttendeeDO>().setAssignedItems(modelList);
    final Select2MultiChoice<PollAttendeeDO> multiChoices = new Select2MultiChoice<PollAttendeeDO>(fieldset.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<PollAttendeeDO>>(assignHelper, "assignedItems"), new PollAttendeeDisabledChoiceProvider(modelList));
    fieldset.add(multiChoices);
    multiChoices.setEnabled(false);
  }

  /**
   * @param fieldset
   * @param modelList
   * @param b
   */
  private void createDisabledChoices(final FieldsetPanel fieldset, final List<PollEventDO> modelList)
  {
    final MultiChoiceListHelper<PollEventDO> assignHelper = new MultiChoiceListHelper<PollEventDO>().setAssignedItems(modelList);
    final Select2MultiChoice<PollEventDO> multiChoices = new Select2MultiChoice<PollEventDO>(fieldset.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<PollEventDO>>(assignHelper, "assignedItems"), new PollEventDisabledChoiceProvider(modelList));
    fieldset.add(multiChoices);
    multiChoices.setEnabled(false);
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
    pollDao.save(model.getPollDo());
    final List<PollEventDO> pollEvents = new ArrayList<PollEventDO>();
    pollEvents.addAll(model.getAllEvents());
    pollEventDao.save(pollEvents);
    pollAttendeeDao.save(model.getCalculatedAttendeeList());

    // PollResultDO pollResult = new PollResultDO();
    // pollResult.setPollAttendee(pollAttendeeList);
    // pollResult.setPollEvent(pollEvents);
    setResponsePage(PollListPage.class);
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onCancel()
   */
  @Override
  protected void onCancel()
  {
    setResponsePage(new PollAttendeePage(getPageParameters(), model));
  }

}
