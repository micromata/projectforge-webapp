/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.attendee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.poll.PollBasePage;
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
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollAttendeePage extends PollBasePage
{

  private static final long serialVersionUID = 8780858653279140945L;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private final PollDO pollDo;

  private final Collection<PollEventDO> allEvents;

  private MultiChoiceListHelper<GroupDO> assignGroupsListHelper;

  private MultiChoiceListHelper<PFUserDO> assignUsersListHelper;

  private List<PFUserDO> assignUsersList;

  private List<PFUserDO> filteredSelectUserList;

  private String emailList;

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

    gridBuilder.newGrid8();

    // preset assigned groups of current user
    final Collection<Integer> presetGroups = userDao.getAssignedGroups(PFUserContext.getUser());
    final GroupsProvider groupsProvider = new GroupsProvider();
    assignGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
        groupsProvider.getSortedGroups());
    if (presetGroups != null) {
      for (final Integer groupId : presetGroups) {
        final GroupDO group = userGroupCache.getGroup(groupId);
        if (group != null) {
          assignGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
        }
      }
    }

    // remove users, which already exist in preset groups.
    filteredSelectUserList = getFilteredUserList(presetGroups.toArray(new Integer[presetGroups.size()]));
    UsersProvider usersProvider = new UsersProvider() {
      private static final long serialVersionUID = 3309912250935701295L;

      /**
       * @see org.projectforge.web.user.UsersProvider#query(java.lang.String, int, com.vaynberg.wicket.select2.Response)
       */
      @Override
      public void query(String term, int page, Response<PFUserDO> response)
      {
        // response.setResults(filteredSelectUserList);
        super.query(term, page, response);
      }
    };
    // User select
    final FieldsetPanel fsUserSelect = gridBuilder.newFieldset(getString("plugins.poll.attendee.users"), true);
    assignUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
        usersProvider.getSortedUsers());
    final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fsUserSelect.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<PFUserDO>>(this.assignUsersListHelper, "assignedItems"), usersProvider);
    users.setOutputMarkupId(true);
    fsUserSelect.add(users);

    // Group select
    final FieldsetPanel fsGroupSelect = gridBuilder.newFieldset(getString("plugins.poll.attendee.groups"), true);
    final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fsGroupSelect.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<GroupDO>>(this.assignGroupsListHelper, "assignedItems"), groupsProvider);
    groups.add(new AjaxFormComponentUpdatingBehavior("onChange") {
      private static final long serialVersionUID = 7953707980102077562L;

      @Override
      protected void onUpdate(AjaxRequestTarget target)
      {
        // create array of ids to use userGroupCache.isMemberOfAtLeastOneGroup
        Integer[] groupIds = new Integer[assignGroupsListHelper.getAssignedItems().size()];
        int index = 0;
        for (GroupDO group : assignGroupsListHelper.getAssignedItems()) {
          groupIds[index] = group.getId();
          index++;
        }

        // List<PFUserDO> la = assignUsersList;

        // // remove users, which exist in assigned groups.
        List<PFUserDO> deleteList = new ArrayList<PFUserDO>();

        filteredSelectUserList = getFilteredUserList(groupIds);
        // update user select list
        if (filteredSelectUserList != null) {
          if (filteredSelectUserList.isEmpty() == false) {
            List<PFUserDO> addList = new ArrayList<PFUserDO>();
            for (PFUserDO user : filteredSelectUserList) {
              if (userGroupCache.isUserMemberOfAtLeastOneGroup(user.getId(), groupIds) == true) {
                deleteList.add(user);
              } else {
                addList.add(user);
              }
            }
            // delete form user select list
            if (deleteList.isEmpty() == false) {
              for (PFUserDO u : deleteList) {
                filteredSelectUserList.remove(u);
              }
              deleteList.clear();
            }
            // add to user select list
            if (addList.isEmpty() == false) {
              for (PFUserDO u : addList) {

                if (filteredSelectUserList.contains(u) == false)
                  filteredSelectUserList.add(u);
              }
            }
          }
          // target.add(users);
        }
      }
    });
    fsGroupSelect.add(groups);

    // TODO Email select
    final FieldsetPanel fsEMail = gridBuilder.newFieldset(getString("plugins.poll.new.description"), true);
    // May be repeating input field for every mail address
    // final RepeatingView emailRepeater = new RepeatingView(fsEMail.newChildId());
    // emailRepeater.add(getNewEMailField(emailRepeater.newChildId()));
    //
    // Button addEmailButton = new Button(SingleButtonPanel.WICKET_ID) {
    // private static final long serialVersionUID = 2351870376486149681L;
    //
    // @Override
    // public void onSubmit()
    // {
    // emailRepeater.add(getNewEMailField(emailRepeater.newChildId()));
    // }
    // };
    // SingleButtonPanel addEmailButtonPanel = new SingleButtonPanel("cancel", addEmailButton, getString("cancel"),
    // SingleButtonPanel.SEND_RIGHT);
    // fsEMail.add(emailRepeater);
    fsEMail.add(getNewEMailField(fsEMail.getTextFieldId()));

  }

  /**
   * returns list without users which already exist in assigned group.
   * 
   * @return
   */
  private List<PFUserDO> getFilteredUserList(Integer groupIds[])
  {
    UsersProvider preUsersProvider = new UsersProvider();
    List<PFUserDO> newList = new ArrayList<PFUserDO>();
    for (PFUserDO user : preUsersProvider.getSortedUsers()) {
      if (userGroupCache.isUserMemberOfAtLeastOneGroup(user.getId(), groupIds) == false && newList.contains(user) == false) {
        newList.add(user);
      }
    }
    return newList;
  }

  private TextField<String> getNewEMailField(String wicketId)
  {
    PropertyModel<String> mailModel = new PropertyModel<String>(this, "emailList");
    TextField<String> eMailField = new TextField<String>(wicketId, mailModel);
    return eMailField;
  };

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    // TODO add i18nKey
    return "PollAttendeePage";
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onConfirm()
   */
  @Override
  protected void onConfirm()
  {
    // setResponsePage(new PollResultPage(getPageParameters(), pollDo, allEvents, pollAttendeeList));
  }

  /**
   * @see org.projectforge.plugins.poll.PollBasePage#onCancel()
   */
  @Override
  protected void onCancel()
  {
    setResponsePage(new PollEventEditPage(getPageParameters(), new PropertyModel<PollDO>(PollAttendeePage.this, "pollDo"), allEvents));
  }

}
