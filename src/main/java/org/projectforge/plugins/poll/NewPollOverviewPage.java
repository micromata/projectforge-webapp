/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
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
import org.projectforge.user.PFUserDO;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.user.UsersComparator;
import org.projectforge.web.user.UsersProvider;
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
    if (parameters == null) {
      NewPollPage.redirectToNewPollPage(parameters);
      this.model = null;
    } else {
      final Integer id = new Integer(parameters.get("id").toString());
      this.model = new NewPollFrontendModel(pollDao.getById(id));
      this.model.initModelByPoll();
    }
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

    final FieldsetPanel fsTitle = gridBuilder.newFieldset(getString("plugins.poll.new.title"));
    final TextField<String> title = new TextField<String>(fsTitle.getTextFieldId(), new PropertyModel<String>(model.getPollDo(), "title"));
    title.setEnabled(this.model.isNew());
    fsTitle.add(title);

    final FieldsetPanel fsLocation = gridBuilder.newFieldset(getString("plugins.poll.new.location")).setLabelFor(this);
    final TextField<String> location = new TextField<String>(fsLocation.getTextFieldId(),
        new PropertyModel<String>(model.getPollDo(), "location"));
    location.setEnabled(this.model.isNew());
    fsLocation.add(location);

    final FieldsetPanel fsDescription = gridBuilder.newFieldset(getString("plugins.poll.new.description"));
    final TextArea<String> description = new TextArea<String>(fsDescription.getTextAreaId(), new PropertyModel<String>(this.model.getPollDo(),
        "description"));
    description.setEnabled(this.model.isNew());
    fsDescription.add(description);

    gridBuilder.newGridPanel();

    if (this.model.isNew() == true) {
      createEnabledChoices();
    } else {
      final FieldsetPanel fsUsers = gridBuilder.newFieldset(getString("plugins.poll.attendee.users"));

      if (model.isNew() == false) {
        createDisabledChoices(fsUsers, model.getCalculatedAttendeeList(), true);
      } else {
        createDisabledChoices(fsUsers, model.getPollAttendeeList(), true);
      }
    }

    final FieldsetPanel fsEMails = gridBuilder.newFieldset(getString("plugins.poll.attendee.emails"));
    if (model.isNew() == false) {
      createDisabledChoices(fsEMails, model.getCalculatedAttendeeList(), false);
    } else {
      createDisabledChoices(fsEMails, model.getPollAttendeeList(), false);
    }

    final FieldsetPanel fsEvents = gridBuilder.newFieldset(getString("plugins.poll.attendee.events"));
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

  private void createEnabledChoices()
  {
    final UsersProvider usersProvider = new UsersProvider();
    // User select
    final FieldsetPanel fsUserSelect = gridBuilder.newFieldset(getString("plugins.poll.attendee.users"));
    final MultiChoiceListHelper<PFUserDO> assignUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator())
        .setFullList(usersProvider.getSortedUsers());
    final HashSet<PFUserDO> attendeess = new HashSet<PFUserDO>();
    for (final PollAttendeeDO attendee : model.getPollAttendeeList()) {
      if (attendee.getUser() != null) {
        attendeess.add(attendee.getUser());
      } else {
        // TODO email list
      }
    }
    assignUsersListHelper.setAssignedItems(attendeess);
    final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fsUserSelect.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<PFUserDO>>(assignUsersListHelper, "assignedItems"), usersProvider);
    fsUserSelect.add(users);
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
    pollDao.saveOrUpdate(model.getPollDo());
    final List<PollEventDO> pollEvents = new ArrayList<PollEventDO>();
    pollEvents.addAll(model.getAllEvents());
    if (model.isNew() == false) {
      for (final PollEventDO event : pollEvents) {
        event.setPoll(model.getPollDo());
      }
      for (final PollAttendeeDO attendee : model.getCalculatedAttendeeList()) {
        attendee.setPoll(model.getPollDo());
      }
    }
    pollEventDao.saveOrUpdate(pollEvents);
    pollAttendeeDao.saveOrUpdate(model.getCalculatedAttendeeList());

    setResponsePage(PollListPage.class);
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onCancel()
   */
  @Override
  protected void onCancel()
  {
    setResponsePage(PollListPage.class);
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onBack()
   */
  @Override
  protected void onBack()
  {
    setResponsePage(new PollAttendeePage(getPageParameters(), model));
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onDelete()
   */
  @Override
  protected void onDelete()
  {
    if (model != null && model.getPollDo() != null) {
      model.getPollDo().setDeleted(true);
      pollDao.save(model.getPollDo());
    }
  }
}
