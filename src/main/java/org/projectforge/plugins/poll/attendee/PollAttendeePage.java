/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.attendee;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.PollDO;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.event.PollEventEditPage;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.user.GroupsComparator;
import org.projectforge.web.user.GroupsProvider;
import org.projectforge.web.user.UsersComparator;
import org.projectforge.web.user.UsersProvider;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollAttendeePage extends AbstractSecuredPage
{

  private static final long serialVersionUID = 8780858653279140945L;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private final PollDO pollDo;

  private final Collection<PollEventDO> allEvents;

  private GridBuilder gridBuilder;

  private MultiChoiceListHelper<GroupDO> assignListHelper;

  private MultiChoiceListHelper<PFUserDO> assignUsersListHelper;

  /**
   * @param parameters
   */
  public PollAttendeePage(PageParameters parameters, PollDO pollDo, Collection<PollEventDO> allEvents)
  {
    super(parameters);
    this.pollDo = pollDo;
    this.allEvents = allEvents;
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();

    final RepeatingView flowform = new RepeatingView("flowform");
    gridBuilder = new GridBuilder(flowform, getMySession());

    gridBuilder.newGrid8();

    // User select
    final FieldsetPanel fsUserSelect = gridBuilder.newFieldset(getString("plugins.poll.new.title"), true);
    final Set<PFUserDO> assignedUsers = new HashSet<PFUserDO>(userGroupCache.getAllUsers());
    final UsersProvider usersProvider = new UsersProvider();
    assignUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
        usersProvider.getSortedUsers());
    if (assignedUsers != null) {
      for (final PFUserDO user : assignedUsers) {
        assignUsersListHelper.addOriginalAssignedItem(user).assignItem(user);
      }
    }
    final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fsUserSelect.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<PFUserDO>>(this.assignUsersListHelper, "assignedItems"), usersProvider);
    fsUserSelect.add(users);

    // Group select
    final FieldsetPanel fsGroupSelect = gridBuilder.newFieldset(getString("plugins.poll.new.location"), true);
    final Collection<Integer> set = userDao.getAssignedGroups(PFUserContext.getUser());
    final GroupsProvider groupsProvider = new GroupsProvider();
    assignListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
        groupsProvider.getSortedGroups());
    if (set != null) {
      for (final Integer groupId : set) {
        final GroupDO group = userGroupCache.getGroup(groupId);
        if (group != null) {
          assignListHelper.addOriginalAssignedItem(group).assignItem(group);
        }
      }
    }
    final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fsGroupSelect.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<GroupDO>>(this.assignListHelper, "assignedItems"), groupsProvider);
    fsGroupSelect.add(groups);

    // TODO Email select
    // final FieldsetPanel fsDescription = gridBuilder.newFieldset(getString("plugins.poll.new.description"), true);
    // MaxLengthTextArea descriptionField = new MaxLengthTextArea(fsDescription.getTextAreaId(), new PropertyModel<String>(pollDoModel,
    // "description"));
    // fsDescription.add(descriptionField);

    // Back button
    AjaxButton back = new AjaxButton("button") {

      private static final long serialVersionUID = -2517605954536886155L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form< ? > form)
      {
        setResponsePage(new PollEventEditPage(getPageParameters(), new PropertyModel<PollDO>(PollAttendeePage.this, "pollDo"), allEvents));
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form< ? > form)
      {
        error("asdf");
      }
    };
    SingleButtonPanel backPanel = new SingleButtonPanel("back", back, getString("back"), SingleButtonPanel.CANCEL);

    // Confirm button
    Button confirm = new Button(SingleButtonPanel.WICKET_ID) {
      static final long serialVersionUID = -7779593314951993472L;

      @Override
      public final void onSubmit()
      {
        // setResponsePage(new PollEventEditPage(getPageParameters(), pollDoModel));
      }
    };
    SingleButtonPanel confirmPanel = new SingleButtonPanel("confirm", confirm, getString("plugins.poll.new.continue"),
        SingleButtonPanel.DEFAULT_SUBMIT);

    Form<String> form = new Form<String>("pollForm");
    body.add(form);
    form.add(backPanel);
    form.add(confirmPanel);
    form.add(flowform);

    final ContainerFeedbackMessageFilter containerFeedbackMessageFilter = new ContainerFeedbackMessageFilter(this);
    final WebMarkupContainer feedbackContainer = new WebMarkupContainer("feedbackContainer") {
      private static final long serialVersionUID = -2676548030393266940L;

      /**
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible()
      {
        return MySession.get().getFeedbackMessages().hasMessage(containerFeedbackMessageFilter);
      }
    };
    form.add(feedbackContainer);
    feedbackContainer.add(new FeedbackPanel("feedback", containerFeedbackMessageFilter));
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    // TODO change this
    return "PollAttendeePage";
  }

}
