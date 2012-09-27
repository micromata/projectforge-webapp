/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.user;

import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.common.AssignListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

public class GroupEditForm extends AbstractEditForm<GroupDO, GroupEditPage>
{
  private static final long serialVersionUID = 3044732844606748738L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupEditForm.class);

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  AssignListHelper<PFUserDO> assignUsersListHelper;

  public GroupEditForm(final GroupEditPage parentPage, final GroupDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGrid8();
    {
      // Name
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("name"));
      final RequiredMaxLengthTextField name = new RequiredMaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(getData(),
          "name"));
      name.add(new AbstractValidator<String>() {
        @Override
        protected void onValidate(final IValidatable<String> validatable)
        {
          final String groupname = validatable.getValue();
          if (groupname == null) {
            return;
          }
          getData().setName(groupname);
          if (groupDao.doesGroupnameAlreadyExist(getData()) == true) {
            validatable.error(new ValidationError().addMessageKey("group.error.groupnameAlreadyExists"));
          }
        }
      });
      fs.add(name);
      WicketUtils.setStrong(name);
    }
    {
      // Organization
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("organization"));
      fs.add(new MaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(getData(), "organization")));
    }
    {
      // Description
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(getData(), "description")));
    }
    if (Login.getInstance().hasExternalUsermanagementSystem() == true) {
      WicketUtils.addYesNoRadioFieldset(gridBuilder, getString("group.localGroup"), "localGroup", new PropertyModel<Boolean>(data,
          "localGroup"), getString("group.localGroup.tooltip"));
    }
    gridBuilder.newGrid8();
    {
      // User lists
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("group.assignedUsers"), true).setLabelSide(false);
      final Set<PFUserDO> assignedUsers = getData().getAssignedUsers();
      final UsersProvider usersProvider = new UsersProvider();
      assignUsersListHelper = new AssignListHelper<PFUserDO>().setComparator(new UsersComparator()).setFullList(
          usersProvider.getSortedUsers());
      if (assignedUsers != null) {
        for (final PFUserDO user : assignedUsers) {
          assignUsersListHelper.addOriginalAssignedItem(user).assignItem(user);
        }
      }
      final Select2MultiChoice<PFUserDO> users = new Select2MultiChoice<PFUserDO>(fs.getSelect2MultiChoiceId(),
          new PropertyModel<Collection<PFUserDO>>(this.assignUsersListHelper, "assignedItems"), usersProvider);
      fs.add(users);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
