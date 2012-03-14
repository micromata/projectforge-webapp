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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.common.KeyValueBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class GroupEditForm extends AbstractEditForm<GroupDO, GroupEditPage>
{
  private static final long serialVersionUID = 3044732844606748738L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupEditForm.class);

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  TwoListHelper<Integer, String> users;

  private List<Integer> valuesToAssign = new ArrayList<Integer>();

  private List<Integer> valuesToUnassign = new ArrayList<Integer>();

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

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
    gridBuilder.newGrid8();
    {
      // User lists
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("group.assignedUsers"), true).setLabelSide(false);
      final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
      final List<PFUserDO> result = userDao.getList(userDao.getDefaultFilter());
      for (final PFUserDO user : result) {
        fullList.add(new KeyValueBean<Integer, String>(user.getId(), user.getFullname()));
      }
      final List<Integer> assignedUsers = new ArrayList<Integer>();
      final Collection<PFUserDO> set = getData().getAssignedUsers();
      if (set != null) {
        for (final PFUserDO user : set) {
          assignedUsers.add(user.getId());
        }
      }
      this.users = new TwoListHelper<Integer, String>(fullList, assignedUsers);
      this.users.sortLists();
      valuesToUnassignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
      valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
      WicketUtils.setHeight(valuesToUnassignChoice, 50);
      WicketUtils.setPercentSize(valuesToUnassignChoice, 45);
      fs.add(valuesToUnassignChoice);
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_WEST, getString("tooltip.assign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          users.assign(valuesToAssign);
          valuesToAssign.clear();
          refreshUsersLists();
        };
      }));
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_EAST, getString("tooltip.unassign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          users.unassign(valuesToUnassign);
          valuesToUnassign.clear();
          refreshUsersLists();
        };
      }));
      valuesToAssignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
      valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
      WicketUtils.setHeight(valuesToAssignChoice, 50);
      WicketUtils.setPercentSize(valuesToAssignChoice, 45);
      fs.add(valuesToAssignChoice);
      fs.setNowrap();
    }
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

  public void setValuesToAssign(final List<Integer> valuesToAssign)
  {
    this.valuesToAssign = valuesToAssign;
  }

  public List<Integer> getValuesToUnassign()
  {
    return valuesToUnassign;
  }

  public void setValuesToUnassign(final List<Integer> valuesToUnassign)
  {
    this.valuesToUnassign = valuesToUnassign;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
