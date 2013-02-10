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

package org.projectforge.plugins.teamcal.admin;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.plugins.teamcal.dialog.TeamCalICSExportDialog;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.user.GroupsComparator;
import org.projectforge.web.user.GroupsProvider;
import org.projectforge.web.user.UsersComparator;
import org.projectforge.web.user.UsersProvider;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.AjaxIconLinkPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * Creates a top form-panel to add filter functions or other options.
 * 
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalEditForm extends AbstractEditForm<TeamCalDO, TeamCalEditPage>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalEditForm.class);

  private static final long serialVersionUID = 1379614008604844519L;

  @SpringBean(name = "accessChecker")
  protected AccessChecker accessChecker;

  private boolean access = false;

  private JodaDatePanel datePanel;

  MultiChoiceListHelper<PFUserDO> fullAccessUsersListHelper, readonlyAccessUsersListHelper, minimalAccessUsersListHelper;

  MultiChoiceListHelper<GroupDO> fullAccessGroupsListHelper, readonlyAccessGroupsListHelper, minimalAccessGroupsListHelper;

  private TeamCalICSExportDialog icsExportDialog;

  /**
   * @param parentPage
   * @param data
   */
  public TeamCalEditForm(final TeamCalEditPage parentPage, final TeamCalDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();

    gridBuilder.newSplitPanel(GridSize.COL50);

    // checking visibility rights
    final TeamCalRight right = new TeamCalRight();
    if (isNew() == true || right.hasUpdateAccess(getUser(), data, data) == true) {
      access = true;
    }

    // set title
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.title"));
      final RequiredMaxLengthTextField title = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title"));
      if (isNew() == true) {
        title.add(WicketUtils.setFocus());
      }
      WicketUtils.setStrong(title);
      fs.add(title);
      if (access == false) {
        title.setEnabled(false);
      }
    }

    // set description
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.description"));
      final MaxLengthTextArea descr = new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description"));
      fs.add(descr).setAutogrow();
      if (access == false) {
        descr.setEnabled(false);
      }
    }

    gridBuilder.newSplitPanel(GridSize.COL50);
    // set owner
    {
      if (data.getOwner() == null) {
        data.setOwner(getUser());
      }
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.owner"));//.setLabelFor(this);
      fs.add(new Label(fs.newChildId(), data.getOwner().getUsername() + ""));
    }

    if (accessChecker.isRestrictedUser() == false && isNew() == false) {
      icsExportDialog = new TeamCalICSExportDialog(parentPage.newModalDialogId(), new ResourceModel("plugins.teamcal.download"));
      parentPage.add(icsExportDialog);
      icsExportDialog.init();
      icsExportDialog.redraw(getData());
      final FieldsetPanel fsSubscribe = gridBuilder.newFieldset(getString("plugins.teamcal.abonnement")).supressLabelForWarning();
      fsSubscribe.add(new AjaxIconLinkPanel(fsSubscribe.newChildId(), IconType.ABONNEMENT, new ResourceModel(
          "plugins.teamcal.abonnement.tooltip")) {
        @Override
        public void onClick(final AjaxRequestTarget target)
        {
          icsExportDialog.open(target);
        };
      });
    }

    if (access == true) {
      gridBuilder.newSplitPanel(GridSize.COL50);
      // set access users
      {
        // Full access users
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.fullAccess.users"));
        final UsersProvider usersProvider = new UsersProvider();
        final Collection<PFUserDO> fullAccessUsers = new UsersProvider().getSortedUsers(getData().getFullAccessUserIds());
        fullAccessUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
            usersProvider.getSortedUsers());
        if (fullAccessUsers != null) {
          for (final PFUserDO user : fullAccessUsers) {
            fullAccessUsersListHelper.addOriginalAssignedItem(user).assignItem(user);
          }
        }
        final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<PFUserDO>>(this.fullAccessUsersListHelper, "assignedItems"), usersProvider);
        fs.add(users);
      }
      {
        // Read-only access users
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.readonlyAccess.users"));
        final UsersProvider usersProvider = new UsersProvider();
        final Collection<PFUserDO> readOnlyAccessUsers = new UsersProvider().getSortedUsers(getData().getReadonlyAccessUserIds());
        readonlyAccessUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
            usersProvider.getSortedUsers());
        if (readOnlyAccessUsers != null) {
          for (final PFUserDO user : readOnlyAccessUsers) {
            readonlyAccessUsersListHelper.addOriginalAssignedItem(user).assignItem(user);
          }
        }
        final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<PFUserDO>>(this.readonlyAccessUsersListHelper, "assignedItems"), usersProvider);
        fs.add(users);
      }
      {
        // Minimal access users
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.minimalAccess.users"));
        final UsersProvider usersProvider = new UsersProvider();
        final Collection<PFUserDO> minimalAccessUsers = new UsersProvider().getSortedUsers(getData().getMinimalAccessUserIds());
        minimalAccessUsersListHelper = new MultiChoiceListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
            usersProvider.getSortedUsers());
        if (minimalAccessUsers != null) {
          for (final PFUserDO user : minimalAccessUsers) {
            minimalAccessUsersListHelper.addOriginalAssignedItem(user).assignItem(user);
          }
        }
        final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<PFUserDO>>(this.minimalAccessUsersListHelper, "assignedItems"), usersProvider);
        fs.addHelpIcon(getString("plugins.teamcal.minimalAccess.users.hint"));
        fs.add(users);
      }

      gridBuilder.newSplitPanel(GridSize.COL50);
      // set access groups
      {
        // Full access groups
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.fullAccess.groups"));
        final GroupsProvider groupsProvider = new GroupsProvider();
        final Collection<GroupDO> fullAccessGroups = new GroupsProvider().getSortedGroups(getData().getFullAccessGroupIds());
        fullAccessGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
            groupsProvider.getSortedGroups());
        if (fullAccessGroups != null) {
          for (final GroupDO group : fullAccessGroups) {
            fullAccessGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
          }
        }
        final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<GroupDO>>(this.fullAccessGroupsListHelper, "assignedItems"), groupsProvider);
        fs.add(groups);
      }
      {
        // Read-only access groups
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.readonlyAccess.groups"));
        final GroupsProvider groupsProvider = new GroupsProvider();
        final Collection<GroupDO> readOnlyAccessGroups = new GroupsProvider().getSortedGroups(getData().getReadonlyAccessGroupIds());
        readonlyAccessGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
            groupsProvider.getSortedGroups());
        if (readOnlyAccessGroups != null) {
          for (final GroupDO group : readOnlyAccessGroups) {
            readonlyAccessGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
          }
        }
        final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<GroupDO>>(this.readonlyAccessGroupsListHelper, "assignedItems"), groupsProvider);
        fs.add(groups);
      }
      {
        // Minimal access groups
        final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.minimalAccess.groups"));
        final GroupsProvider groupsProvider = new GroupsProvider();
        final Collection<GroupDO> minimalAccessGroups = new GroupsProvider().getSortedGroups(getData().getMinimalAccessGroupIds());
        minimalAccessGroupsListHelper = new MultiChoiceListHelper<GroupDO>().setComparator(new GroupsComparator()).setFullList(
            groupsProvider.getSortedGroups());
        if (minimalAccessGroups != null) {
          for (final GroupDO group : minimalAccessGroups) {
            minimalAccessGroupsListHelper.addOriginalAssignedItem(group).assignItem(group);
          }
        }
        final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
            new PropertyModel<Collection<GroupDO>>(this.minimalAccessGroupsListHelper, "assignedItems"), groupsProvider);
        fs.addHelpIcon(getString("plugins.teamcal.minimalAccess.groups.hint"));
        fs.add(groups);
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @return the datePanel
   */
  public JodaDatePanel getDatePanel()
  {
    return datePanel;
  }

  /**
   * @param datePanel the datePanel to set
   * @return this for chaining.
   */
  public void setDatePanel(final JodaDatePanel datePanel)
  {
    this.datePanel = datePanel;
  }
}
