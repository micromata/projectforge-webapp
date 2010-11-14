/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.common.KeyValueBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;

public class AccessEditForm extends AbstractEditForm<GroupDO, AccessEditPage>
{
  private static final long serialVersionUID = 3044732844606748738L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AccessEditForm.class);

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  TwoListHelper<Integer, String> users;

  private List<Integer> valuesToAssign = new ArrayList<Integer>();

  private List<Integer> valuesToUnassign = new ArrayList<Integer>();

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

  public AccessEditForm(final AccessEditPage parentPage, GroupDO data)
  {
    super(parentPage, data);
    this.colspan = 2;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    List<Integer> usersToAdd = null;
    add(new RequiredMaxLengthTextField("name", new PropertyModel<String>(getData(), "name")).add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable)
      {
        final String groupname = validatable.getValue();
        if (groupname == null) {
          return;
        }
        getData().setName(groupname);
        if (groupDao.doesGroupnameAlreadyExist(getData()) == true) {
          error(validatable);
        }
      }

      @Override
      protected String resourceKey()
      {
        return "group.error.groupnameAlreadyExists";
      }
    }));
    add(new MaxLengthTextField("organization", new PropertyModel<String>(getData(), "organization")));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(getData(), "description")));

    final SubmitLink unassignButton = new SubmitLink("unassignButton") {
      public void onSubmit()
      {
        users.unassign(valuesToUnassign);
        valuesToUnassign.clear();
        refreshUsersLists();
      };
    };
    add(unassignButton);
    unassignButton.add(new TooltipImage("buttonUnassignImage", getResponse(), WebConstants.IMAGE_BUTTON_ASSIGN_TO_RIGHT,
        getString("tooltip.unassign")));

    final List<Integer> assignedUsers = new ArrayList<Integer>();
    final Collection<PFUserDO> set = getData().getAssignedUsers();
    if (set != null) {
      for (final PFUserDO user : set) {
        assignedUsers.add(user.getId());
      }
    }
    final SubmitLink assignButton = new SubmitLink("assignButton") {
      public void onSubmit()
      {
        users.assign(valuesToAssign);
        valuesToAssign.clear();
        refreshUsersLists();
      };
    };
    add(assignButton);
    assignButton.add(new TooltipImage("buttonAssignImage", getResponse(), WebConstants.IMAGE_BUTTON_ASSIGN_TO_LEFT,
        getString("tooltip.assign")));
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
    final List<PFUserDO> result = (List<PFUserDO>) userDao.getList(userDao.getDefaultFilter());
    for (final PFUserDO user : result) {
      fullList.add(new KeyValueBean<Integer, String>(user.getId(), user.getUsername()));
    }
    this.users = new TwoListHelper<Integer, String>(fullList, assignedUsers);
    if (usersToAdd != null) {
      users.assign(usersToAdd);
    }
    this.users.sortLists();
    valuesToAssignChoice = new ListMultipleChoice<Integer>("valuesToAssign");
    valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
    add(valuesToAssignChoice);
    valuesToUnassignChoice = new ListMultipleChoice<Integer>("valuesToUnassign");
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
    add(valuesToUnassignChoice);
    refreshUsersLists();
  }

  private void refreshUsersLists()
  {
    final LabelValueChoiceRenderer<Integer> valuesToAssignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.users.getUnassignedItems()) {
      valuesToAssignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    valuesToAssignChoice.setChoiceRenderer(valuesToAssignChoiceRenderer);
    valuesToAssignChoice.setChoices(valuesToAssignChoiceRenderer.getValues());
    final LabelValueChoiceRenderer<Integer> valuesToUnassignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.users.getAssignedItems()) {
      valuesToUnassignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    valuesToUnassignChoice.setChoiceRenderer(valuesToUnassignChoiceRenderer);
    valuesToUnassignChoice.setChoices(valuesToUnassignChoiceRenderer.getValues());
  }

  public List<Integer> getValuesToAssign()
  {
    return valuesToAssign;
  }

  public void setValuesToAssign(List<Integer> valuesToAssign)
  {
    this.valuesToAssign = valuesToAssign;
  }

  public List<Integer> getValuesToUnassign()
  {
    return valuesToUnassign;
  }

  public void setValuesToUnassign(List<Integer> valuesToUnassign)
  {
    this.valuesToUnassign = valuesToUnassign;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
