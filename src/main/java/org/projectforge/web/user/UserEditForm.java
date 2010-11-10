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

package org.projectforge.web.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.common.KeyValueBean;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;

public class UserEditForm extends UserBaseEditForm<UserEditPage>
{
  public static final String TUTORIAL_DEFAULT_PASSWORD = "test";

  private static final long serialVersionUID = 7872294377838461659L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditForm.class);

  private static final String MAGIC_PASSWORD = "******";
  
  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  protected UserRightsEditData rightsData;

  private String password;

  private String passwordRepeat;

  private String encryptedPassword;

  TwoListHelper<Integer, String> groups;

  private List<Integer> valuesToAssign = new ArrayList<Integer>();

  private List<Integer> valuesToUnassign = new ArrayList<Integer>();

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

  public UserEditForm(UserEditPage parentPage, PFUserDO data)
  {
    super(parentPage, data);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    if (data != null && TUTORIAL_DEFAULT_PASSWORD.equals(data.getPassword()) == true) {
      encryptedPassword = userDao.encryptPassword(TUTORIAL_DEFAULT_PASSWORD);
      password = passwordRepeat = MAGIC_PASSWORD;
    }
    add(new RequiredMaxLengthTextField("username", new PropertyModel<String>(getData(), "username")).add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable)
      {
        final String username = validatable.getValue();
        if (username == null) {
          return;
        }
        getData().setUsername(username);
        if (userDao.doesUsernameAlreadyExist(getData()) == true) {
          error(validatable);
        }
      }

      @Override
      protected String resourceKey()
      {
        return "user.error.usernameAlreadyExists";
      }
    }));
    final PasswordTextField passwordRepeatField = new PasswordTextField("passwordRepeat", new PropertyModel<String>(this, "passwordRepeat")) {
      protected void onComponentTag(final ComponentTag tag)
      {
        super.onComponentTag(tag);
        if (encryptedPassword == null) {
          tag.put("value", "");
        } else if (StringUtils.isEmpty(getConvertedInput()) == false) {
          tag.put("value", MAGIC_PASSWORD);
        }
      }
    };
    passwordRepeatField.setResetPassword(false).setRequired(false);
    add(passwordRepeatField);
    final PasswordTextField passwordField = new PasswordTextField("password", new PropertyModel<String>(this, "password")) {
      protected void onComponentTag(final ComponentTag tag)
      {
        super.onComponentTag(tag);
        if (encryptedPassword == null) {
          tag.put("value", "");
        } else if (StringUtils.isEmpty(getConvertedInput()) == false) {
          tag.put("value", MAGIC_PASSWORD);
        }
      }
    };
    passwordField.add(new AbstractValidator<String>() {
      private String errorMsgKey = null;

      @Override
      protected void onValidate(IValidatable<String> validatable)
      {
        final String passwordInput = validatable.getValue();
        final String passwordRepeatInput = passwordRepeatField.getConvertedInput();
        if (StringUtils.isEmpty(passwordInput) == true && StringUtils.isEmpty(passwordRepeatInput) == true) {
          return;
        }
        if (StringUtils.equals(passwordInput, passwordRepeatInput) == false) {
          errorMsgKey = "user.error.passwordAndRepeatDoesNotMatch";
          encryptedPassword = null;
          error(validatable);
          return;
        }
        if (MAGIC_PASSWORD.equals(passwordInput) == false || encryptedPassword == null) {
          errorMsgKey = userDao.checkPasswordQuality(passwordInput);
          if (errorMsgKey != null) {
            encryptedPassword = null;
            error(validatable);
          } else {
            encryptedPassword = userDao.encryptPassword(passwordInput);
          }
        }
      }

      @Override
      protected String resourceKey()
      {
        return errorMsgKey;
      }
    });
    passwordField.setResetPassword(false).setRequired(isNew());
    add(passwordField);

    final RepeatingView rightsRows = new RepeatingView("rightsRows");
    add(rightsRows);
    rightsData = new UserRightsEditData();
    int colCounter = 0;
    final List<UserRightVO> userRights = userRightDao.getUserRights(data);
    WebMarkupContainer rowItem = null;
    RepeatingView rightsCols = null;
    for (final UserRightVO rightVO : userRights) {
      final UserRight right = rightVO.getRight();
      final UserRightValue[] availableValues = right.getAvailableValues(userDao.getUserGroupCache(), getData());
      if (right.isConfigurable(userDao.getUserGroupCache(), getData()) == false) {
        continue;
      }
      if (rowItem == null) {
        rowItem = new WebMarkupContainer(rightsRows.newChildId());
        rightsRows.add(rowItem);
        rightsCols = new RepeatingView("rightsCols");
        rowItem.add(rightsCols);
      }
      rightsData.addRight(rightVO);
      final WebMarkupContainer item = new WebMarkupContainer(rightsCols.newChildId());
      rightsCols.add(item);
      item.add(new Label("label", getString(right.getId().getI18nKey())));
      final CheckBox checkBox = new CheckBox("checkbox", new PropertyModel<Boolean>(rightVO, "booleanValue"));
      item.add(checkBox);
      final LabelValueChoiceRenderer<UserRightValue> valueChoiceRenderer = new LabelValueChoiceRenderer<UserRightValue>(this,
          availableValues);
      final DropDownChoice<UserRightValue> valueChoice = new DropDownChoice<UserRightValue>("valueChoice",
          new PropertyModel<UserRightValue>(rightVO, "value"), valueChoiceRenderer.getValues(), valueChoiceRenderer);
      valueChoice.setNullValid(true);
      item.add(valueChoice);
      if (right.isBooleanType() == false) {
        checkBox.setVisible(false);
      } else {
        valueChoice.setVisible(false);
      }
      if (++colCounter >= 2) {
        colCounter = 0;
        rowItem = null;
      }
    }

    final SubmitLink unassignButton = new SubmitLink("unassignButton") {
      public void onSubmit()
      {
        groups.unassign(valuesToUnassign);
        valuesToUnassign.clear();
        refreshGroupLists();
      };
    };
    add(unassignButton);
    unassignButton.add(new TooltipImage("buttonUnassignImage", getResponse(), WebConstants.IMAGE_BUTTON_ASSIGN_TO_RIGHT,
        getString("tooltip.unassign")));

    final List<Integer> assignedGroups = new ArrayList<Integer>();
    final Collection<Integer> set = userDao.getAssignedGroups(getData());
    if (set != null) {
      for (Integer groupId : set) {
        assignedGroups.add(groupId);
      }
    }
    final SubmitLink assignButton = new SubmitLink("assignButton") {
      public void onSubmit()
      {
        groups.assign(valuesToAssign);
        valuesToAssign.clear();
        refreshGroupLists();
      };
    };
    add(assignButton);
    assignButton.add(new TooltipImage("buttonAssignImage", getResponse(), WebConstants.IMAGE_BUTTON_ASSIGN_TO_LEFT,
        getString("tooltip.assign")));
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
    final List<GroupDO> result = (List<GroupDO>) groupDao.getList(groupDao.getDefaultFilter());
    for (final GroupDO group : result) {
      fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
    }
    this.groups = new TwoListHelper<Integer, String>(fullList, assignedGroups);
    this.groups.sortLists();
    valuesToAssignChoice = new ListMultipleChoice<Integer>("valuesToAssign");
    valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
    add(valuesToAssignChoice);
    valuesToUnassignChoice = new ListMultipleChoice<Integer>("valuesToUnassign");
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
    add(valuesToUnassignChoice);
    refreshGroupLists();
  }

  private void refreshGroupLists()
  {
    final LabelValueChoiceRenderer<Integer> valuesToAssignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.groups.getUnassignedItems()) {
      valuesToAssignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    valuesToAssignChoice.setChoiceRenderer(valuesToAssignChoiceRenderer);
    valuesToAssignChoice.setChoices(valuesToAssignChoiceRenderer.getValues());
    final LabelValueChoiceRenderer<Integer> valuesToUnassignChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final KeyValueBean<Integer, String> group : this.groups.getAssignedItems()) {
      valuesToUnassignChoiceRenderer.addValue(group.getKey(), group.getValue());
    }
    valuesToUnassignChoice.setChoiceRenderer(valuesToUnassignChoiceRenderer);
    valuesToUnassignChoice.setChoices(valuesToUnassignChoiceRenderer.getValues());
  }

  /**
   * Return always empty or dummy string due to security reasons.
   * @return Empty string.
   */
  public String getPassword()
  {
    if (StringUtils.isEmpty(this.password) == true) {
      return "";
    } else {
      return MAGIC_PASSWORD;
    }
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  String getEncryptedPassword()
  {
    return encryptedPassword;
  }

  /**
   * Return always empty or dummy string due to security reasons.
   * @return Empty string.
   */
  public String getPasswordRepeat()
  {
    if (StringUtils.isEmpty(this.passwordRepeat) == true) {
      return "";
    } else {
      return MAGIC_PASSWORD;
    }
  }

  public void setPasswordRepeat(String passwordRepeat)
  {
    this.passwordRepeat = passwordRepeat;
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
