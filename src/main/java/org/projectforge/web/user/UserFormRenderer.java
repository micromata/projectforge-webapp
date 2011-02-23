/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.access.AccessChecker;
import org.projectforge.common.KeyValueBean;
import org.projectforge.common.StringHelper;
import org.projectforge.common.TimeNotation;
import org.projectforge.core.Configuration;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TimeZoneField;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.ListMultipleChoiceLPanel;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.RepeatingViewLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class UserFormRenderer extends AbstractDOFormRenderer
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserFormRenderer.class);

  public static final String TUTORIAL_DEFAULT_PASSWORD = "test";

  public static final String TUTORIAL_ADD_GROUPS = "addGroups";

  private static final long serialVersionUID = 6802305266859905435L;

  private final AccessChecker accessChecker;

  private final UserDao userDao;

  private PFUserDO data;

  protected TextField<String> usernameField;

  private static final String MAGIC_PASSWORD = "******";

  private UserRightDao userRightDao;

  private GroupDao groupDao;

  private AbstractEditPage< ? , ? , ? > parentPage;

  protected UserRightsEditData rightsData;

  private String password;

  private String passwordRepeat;

  private String encryptedPassword;

  boolean invalidateAllStayLoggedInSessions;

  TwoListHelper<Integer, String> groups;

  private List<Integer> valuesToAssign = new ArrayList<Integer>();

  private List<Integer> valuesToUnassign = new ArrayList<Integer>();

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

  final LayoutLength labelLength = LayoutLength.THREEQUART;

  public UserFormRenderer(final MarkupContainer container, final AbstractEditPage< ? , ? , ? > parentPage,
      final LayoutContext layoutContext, final UserDao userDao, final UserRightDao userRightDao, final GroupDao groupDao,
      final AccessChecker accessChecker, final PFUserDO data)
  {
    super(container, layoutContext);
    this.parentPage = parentPage;
    this.data = data;
    this.userDao = userDao;
    this.userRightDao = userRightDao;
    this.groupDao = groupDao;
    this.accessChecker = accessChecker;
  }

  @SuppressWarnings( { "unchecked", "serial"})
  @Override
  public void add()
  {
    final boolean adminAccess = accessChecker.isUserMemberOfAdminGroup();
    IField field;
    doPanel.newFieldSetPanel(isNew() == false ? data.getFullname() : getString("user"));
    if (adminAccess == true) {
      field = doPanel.addTextField(new PanelContext(data, "username", FULL, getString("username"), labelLength).setRequired().setStrong());
      if (field instanceof TextFieldLPanel) { // Isn't true for read-only fields.
        usernameField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
      }
    } else {
      doPanel.addLabel(getString("username"), labelLength);
      doPanel.addLabel(data.getUsername(), FULL);
    }
    doPanel.addTextField(new PanelContext(data, "firstname", FULL, getString("firstName"), labelLength).setRequired().setStrong());
    doPanel.addTextField(new PanelContext(data, "lastname", FULL, getString("name"), labelLength).setRequired().setStrong());
    doPanel.addTextField(new PanelContext(data, "organization", FULL, getString("organization"), labelLength));
    doPanel.addTextField(new PanelContext(data, "email", FULL, getString("email"), labelLength));
    doPanel.addTextField(new PanelContext(data, "jiraUsername", FULL, getString("user.jiraUsername"), labelLength)
        .setTooltip(getString("user.jiraUsername.tooltip")));
    {
      final LabelValueChoiceRenderer<String> localeChoiceRenderer = new LabelValueChoiceRenderer<String>();
      localeChoiceRenderer.addValue("", getString("user.defaultLocale"));
      for (final String str : Configuration.LOCALIZATIONS) {
        localeChoiceRenderer.addValue(str, getString("locale." + str));
      }
      final DropDownChoice localeChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "locale"),
          localeChoiceRenderer.getValues(), localeChoiceRenderer) {

        @Override
        protected CharSequence getDefaultChoice(Object selected)
        {
          return "";
        }

        @Override
        protected Object convertChoiceIdToChoice(final String id)
        {
          if (StringHelper.isIn(id, Configuration.LOCALIZATIONS) == true) {
            return new Locale(id);
          } else {
            return null;
          }
        }
      };
      doPanel.addDropDownChoice(localeChoice, new PanelContext(FULL, getString("user.locale"), labelLength));
    }

    final Date today = new Date();
    addDateFormatCombobox(today, "dateFormat", "dateFormat", Configuration.getInstance().getDateFormats(), false);
    addDateFormatCombobox(today, "dateFormat.xls", "excelDateFormat", Configuration.getInstance().getExcelDateFormats(), true);

    {
      final LabelValueChoiceRenderer<TimeNotation> timeNotationChoiceRenderer = new LabelValueChoiceRenderer<TimeNotation>();
      timeNotationChoiceRenderer.addValue(TimeNotation.H12, getString("timeNotation.12"));
      timeNotationChoiceRenderer.addValue(TimeNotation.H24, getString("timeNotation.24"));
      final DropDownChoice timeNotationChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "timeNotation"),
          timeNotationChoiceRenderer.getValues(), timeNotationChoiceRenderer);
      timeNotationChoice.setNullValid(true);
      doPanel.addDropDownChoice(timeNotationChoice, new PanelContext(FULL, getString("timeNotation"), labelLength));
    }

    final TimeZoneField timeZone = new TimeZoneField(TextFieldLPanel.INPUT_ID, new PropertyModel<TimeZone>(data, "timeZoneObject"));
    doPanel.addTextField(timeZone, new PanelContext(FULL, getString("timezone"), labelLength)
        .setTooltip(getString("tooltip.autocomplete.timeZone")));
    if (StringUtils.isNotEmpty(Configuration.getInstance().getTelephoneSystemUrl()) == true) {
      doPanel
          .addTextField(new PanelContext(data, "personalPhoneIdentifiers", FULL, getString("user.personalPhoneIdentifiers"), labelLength)
              .setTooltip(getString("user.personalPhoneIdentifiers.tooltip")));
    }
    if (Configuration.getInstance().isMebConfigured() == true) {
      doPanel
          .addTextField(new PanelContext(data, "personalMebMobileNumbers", FULL, getString("user.personalMebMobileNumbers"), labelLength)
              .setTooltip(getString("user.personalMebMobileNumbers.tooltip") + "<br/>" + getString("user.personalMebMobileNumbers.format")));
    }

    if (adminAccess == true) {
      final PasswordTextField passwordRepeatField = new PasswordTextField(TextFieldLPanel.INPUT_ID, new PropertyModel<String>(this,
          "passwordRepeat")) {
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
      final PasswordTextField passwordField = new PasswordTextField(TextFieldLPanel.INPUT_ID, new PropertyModel<String>(this, "password")) {
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
          passwordRepeatField.validate();
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
      doPanel.addPasswordTextField(passwordField, new PanelContext(FULL, getString("password"), labelLength)
          .setTooltip(getString(UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK)));
      doPanel.addPasswordTextField(passwordRepeatField, new PanelContext(FULL, getString("passwordRepeat"), labelLength));
    }
    doPanel.addTextArea(new PanelContext(data, "description", FULL, getString("description"), labelLength).setCssStyle("height: 10em;"));

    doPanel.addCheckBox(new PanelContext(this, "invalidateAllStayLoggedInSessions", FULL,
        getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions"), labelLength)
        .setTooltip(getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions.tooltip")));

    doPanel.addLabel(getString("login.lastLogin"), labelLength).setBreakBefore();
    doPanel.addLabel(DateTimeFormatter.instance().getFormattedDateTime(data.getLastLogin()), FULL);

    // Doesn't work:
    // doPanel.addLabel(getString("login.loginFailures"), labelLength).setBreakBefore();
    // doPanel.addLabel(NumberFormatter.format(data.getLoginFailures()), FULL);

    addAssignedGroups();
    if (adminAccess == true) {
      addRights();
    }
  }

  private void addDateFormatCombobox(final Date today, final String labelKey, final String property, final String[] dateFormats,
      final boolean convertExcelFormat)
  {
    final LabelValueChoiceRenderer<String> dateFormatChoiceRenderer = new LabelValueChoiceRenderer<String>();
    for (final String str : dateFormats) {
      String dateString = "???";
      final String pattern = convertExcelFormat == true ? str.replace('Y', 'y').replace('D', 'd') : str;
      try {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateString = dateFormat.format(today);
      } catch (final Exception ex) {
        log.warn("Invalid date format in config.xml: " + pattern);
      }
      dateFormatChoiceRenderer.addValue(str, str + ": " + dateString);
    }
    @SuppressWarnings("unchecked")
    final DropDownChoice dateFormatChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, property), dateFormatChoiceRenderer
        .getValues(), dateFormatChoiceRenderer);
    dateFormatChoice.setNullValid(true);
    doPanel.addDropDownChoice(dateFormatChoice, new PanelContext(FULL, getString(labelKey), labelLength));
  }

  protected void validation()
  {
    if (usernameField != null) {
      usernameField.validate();
      data.setUsername(usernameField.getConvertedInput());
      if (StringUtils.isNotEmpty(data.getUsername()) == true && userDao.doesUsernameAlreadyExist(data) == true) {
        usernameField.error(getString("user.error.usernameAlreadyExists"));
      }
    }
  }

  private void addRights()
  {
    doPanel.newFieldSetPanel(getString("access.rights"));
    rightsData = new UserRightsEditData();
    final List<UserRightVO> userRights = userRightDao.getUserRights(data);
    for (final UserRightVO rightVO : userRights) {
      final UserRight right = rightVO.getRight();
      final UserRightValue[] availableValues = right.getAvailableValues(userDao.getUserGroupCache(), data);
      if (right.isConfigurable(userDao.getUserGroupCache(), data) == false) {
        continue;
      }
      rightsData.addRight(rightVO);
      final String label = getString(right.getId().getI18nKey());
      if (right.isBooleanType() == true) {
        doPanel.addCheckBox(new PanelContext(rightVO, "booleanValue", FULL, label, labelLength).setRequired().setStrong());
      } else {
        final LabelValueChoiceRenderer<UserRightValue> valueChoiceRenderer = new LabelValueChoiceRenderer<UserRightValue>(container,
            availableValues);
        final DropDownChoice<UserRightValue> valueChoice = new DropDownChoice<UserRightValue>(DropDownChoiceLPanel.SELECT_ID,
            new PropertyModel<UserRightValue>(rightVO, "value"), valueChoiceRenderer.getValues(), valueChoiceRenderer);
        valueChoice.setNullValid(true);
        doPanel.addDropDownChoice(valueChoice, new PanelContext(FULL, label, labelLength));
      }
    }
  }

  @SuppressWarnings( { "unchecked", "serial"})
  private void addAssignedGroups()
  {
    final boolean adminAccess = accessChecker.isUserMemberOfAdminGroup();
    doPanel.newFieldSetPanel(getString("group.groups"));

    List<Integer> groupsToAdd = null;
    if (data != null) {
      if (TUTORIAL_DEFAULT_PASSWORD.equals(data.getPassword()) == true) {
        encryptedPassword = userDao.encryptPassword(TUTORIAL_DEFAULT_PASSWORD);
        password = passwordRepeat = MAGIC_PASSWORD;
      }
      groupsToAdd = (List<Integer>) parentPage.getPageParameters().get(TUTORIAL_ADD_GROUPS);
    }
    final List<Integer> assignedGroups = new ArrayList<Integer>();
    final Collection<Integer> set = userDao.getAssignedGroups(data);
    if (set != null) {
      for (Integer groupId : set) {
        assignedGroups.add(groupId);
      }
    }
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
    final List<GroupDO> result = (List<GroupDO>) groupDao.getList(groupDao.getDefaultFilter());
    for (final GroupDO group : result) {
      fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
    }
    this.groups = new TwoListHelper<Integer, String>(fullList, assignedGroups);
    if (groupsToAdd != null) {
      groups.assign(groupsToAdd);
    }
    this.groups.sortLists();
    valuesToAssignChoice = new ListMultipleChoice<Integer>(ListMultipleChoiceLPanel.SELECT_ID);
    valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
    valuesToUnassignChoice = new ListMultipleChoice<Integer>(ListMultipleChoiceLPanel.SELECT_ID);
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));

    doPanel.addListMultipleChoice(valuesToUnassignChoice, new PanelContext(FULL, getString("user.assignedGroups"), labelLength)
        .setBreakBetweenLabelAndField(true).setCssStyle("width: 95%; height:20em;"));

    if (adminAccess == true) {
      final RepeatingViewLPanel repeatingViewPanel = doPanel.addRepeater(LayoutLength.ONEHALF);
      repeatingViewPanel.setBreakBefore();
      final RepeatingView repeatingView = repeatingViewPanel.getRepeatingView();
      final Button unassignButton = new Button("button", new Model<String>(getString("unassign"))) {
        @Override
        public final void onSubmit()
        {
          accessChecker.checkIsUserMemberOfAdminGroup();
          groups.unassign(valuesToUnassign);
          valuesToUnassign.clear();
          refreshGroupLists();
        }
      };
      unassignButton.add(WebConstants.BUTTON_CLASS_RESET);
      final SingleButtonPanel unassignButtonPanel = new SingleButtonPanel(repeatingView.newChildId(), unassignButton);
      repeatingView.add(unassignButtonPanel);

      final Button assignButton = new Button("button", new Model<String>(getString("assign"))) {
        @Override
        public final void onSubmit()
        {
          accessChecker.checkIsUserMemberOfAdminGroup();
          groups.assign(valuesToAssign);
          valuesToAssign.clear();
          refreshGroupLists();
        }
      };
      assignButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
      final SingleButtonPanel assignButtonPanel = new SingleButtonPanel(repeatingView.newChildId(), assignButton);
      repeatingView.add(assignButtonPanel);

      doPanel.addListMultipleChoice(valuesToAssignChoice, new PanelContext(FULL, getString("user.unassignedGroups"), labelLength)
          .setBreakBetweenLabelAndField(true).setCssStyle("width: 95%; height:20em;"));
    }
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

  /**
   * If true then update button results in generating a new stay-logged-in key, therefore any existing stay-logged-in session will be
   * invalid.
   * @return
   */
  public boolean isInvalidateAllStayLoggedInSessions()
  {
    return invalidateAllStayLoggedInSessions;
  }

  public void setInvalidateAllStayLoggedInSessions(boolean invalidateAllStayLoggedInSessions)
  {
    this.invalidateAllStayLoggedInSessions = invalidateAllStayLoggedInSessions;
  }
}
