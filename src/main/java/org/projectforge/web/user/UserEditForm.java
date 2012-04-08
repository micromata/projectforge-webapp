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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.access.AccessChecker;
import org.projectforge.common.KeyValueBean;
import org.projectforge.common.StringHelper;
import org.projectforge.common.TimeNotation;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.user.GroupDO;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.I18nCore;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.TwoListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TimeZoneField;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;

public class UserEditForm extends AbstractEditForm<PFUserDO, UserEditPage>
{
  public static final String TUTORIAL_DEFAULT_PASSWORD = "test";

  public static final String TUTORIAL_ADD_GROUPS = "addGroups";

  private static final long serialVersionUID = 7872294377838461659L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditForm.class);

  private static final String MAGIC_PASSWORD = "******";

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  protected UserRightsEditData rightsData;

  @SuppressWarnings("unused")
  private String password;

  @SuppressWarnings("unused")
  private String passwordRepeat;

  private String encryptedPassword;

  TwoListHelper<Integer, String> groups;

  private final List<Integer> valuesToAssign = new ArrayList<Integer>();

  private final List<Integer> valuesToUnassign = new ArrayList<Integer>();

  private ListMultipleChoice<Integer> valuesToAssignChoice;

  private ListMultipleChoice<Integer> valuesToUnassignChoice;

  boolean invalidateAllStayLoggedInSessions;

  public UserEditForm(final UserEditPage parentPage, final PFUserDO data)
  {
    super(parentPage, data);
  }

  public static void createFirstName(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // First name
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("firstName"));
    final RequiredMaxLengthTextField firstName = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user,
        "firstname"));
    WicketUtils.setStrong(firstName);
    fs.add(firstName);
  }

  public static void createLastName(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Last name
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("name"));
    final RequiredMaxLengthTextField name = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "lastname"));
    WicketUtils.setStrong(name);
    fs.add(name);
  }

  public static void createOrganization(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Organization
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("organization"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "organization")));
  }

  public static void createEMail(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // E-Mail
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("email"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "email")));
  }

  public static void createJIRAUsername(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // JIRA user name
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("user.jiraUsername"), true);
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "jiraUsername")));
    fs.addHelpIcon(gridBuilder.getString("user.jiraUsername.tooltip"));
  }

  public static void createLastLoginAndDeleteAllStayLogins(final GridBuilder gridBuilder, final PFUserDO user, final UserDao userDao,
      final Form< ? > form)
  {
    // Last login and deleteAllStayLoggedInSessions
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("login.lastLogin"), true).setNoLabelFor();
    fs.add(new DivTextPanel(fs.newChildId(), DateTimeFormatter.instance().getFormattedDateTime(user.getLastLogin())));
    @SuppressWarnings("serial")
    final Button button = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("invalidateStayLoggedInSessions")) {
      @Override
      public final void onSubmit()
      {
        userDao.renewStayLoggedInKey(user.getId());
        form.error(getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions.successfullDeleted"));
      }
    };
    fs.add(new SingleButtonPanel(fs.newChildId(), button, gridBuilder.getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions"),
        SingleButtonPanel.RED));
    WicketUtils.addTooltip(button, gridBuilder.getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions.tooltip"));
  }

  public static void createLocale(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Locale
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("user.locale"));
    final LabelValueChoiceRenderer<Locale> localeChoiceRenderer = new LabelValueChoiceRenderer<Locale>();
    localeChoiceRenderer.addValue(null, gridBuilder.getString("user.defaultLocale"));
    for (final String str : I18nCore.LOCALIZATIONS) {
      localeChoiceRenderer.addValue(new Locale(str), gridBuilder.getString("locale." + str));
    }
    @SuppressWarnings("serial")
    final DropDownChoice<Locale> localeChoice = new DropDownChoice<Locale>(fs.getDropDownChoiceId(), new PropertyModel<Locale>(user,
        "locale"), localeChoiceRenderer.getValues(), localeChoiceRenderer) {
      /**
       * @see org.apache.wicket.markup.html.form.AbstractSingleSelectChoice#getDefaultChoice(java.lang.String)
       */
      @Override
      protected CharSequence getDefaultChoice(final String selectedValue)
      {
        return "";
      }

      @Override
      protected Locale convertChoiceIdToChoice(final String id)
      {
        if (StringHelper.isIn(id, I18nCore.LOCALIZATIONS) == true) {
          return new Locale(id);
        } else {
          return null;
        }
      }
    };
    fs.add(localeChoice);
  }

  public static void createLanguage(final GridBuilder gridBuilder, final PFUserDO user)
  {
  }

  public static void createDateFormat(final GridBuilder gridBuilder, final PFUserDO user)
  {
    addDateFormatCombobox(gridBuilder, user, "dateFormat", "dateFormat", Configuration.getInstance().getDateFormats(), false);
  }

  public static void createExcelDateFormat(final GridBuilder gridBuilder, final PFUserDO user)
  {
    addDateFormatCombobox(gridBuilder, user, "dateFormat.xls", "excelDateFormat", Configuration.getInstance().getExcelDateFormats(), true);
  }

  public static void createTimeNotation(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Time notation
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("timeNotation"));
    final LabelValueChoiceRenderer<TimeNotation> timeNotationChoiceRenderer = new LabelValueChoiceRenderer<TimeNotation>();
    timeNotationChoiceRenderer.addValue(TimeNotation.H12, gridBuilder.getString("timeNotation.12"));
    timeNotationChoiceRenderer.addValue(TimeNotation.H24, gridBuilder.getString("timeNotation.24"));
    final DropDownChoice<TimeNotation> timeNotationChoice = new DropDownChoice<TimeNotation>(fs.getDropDownChoiceId(),
        new PropertyModel<TimeNotation>(user, "timeNotation"), timeNotationChoiceRenderer.getValues(), timeNotationChoiceRenderer);
    timeNotationChoice.setNullValid(true);
    fs.add(timeNotationChoice);
  }

  public static void createTimeZone(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Time zone
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("timezone"), true);
    final TimeZoneField timeZone = new TimeZoneField(fs.getTextFieldId(), new PropertyModel<TimeZone>(user, "timeZoneObject"));
    fs.addKeyboardHelpIcon(gridBuilder.getString("tooltip.autocomplete.timeZone"));
    fs.add(timeZone);
  }

  /**
   * If no telephone system url is set in config.xml nothing will be done.
   * @param gridBuilder
   * @param user
   */
  public static void createPhoneIds(final GridBuilder gridBuilder, final PFUserDO user)
  {
    if (StringUtils.isNotEmpty(ConfigXml.getInstance().getTelephoneSystemUrl()) == true) {
      // Personal phone identifiers
      final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("user.personalPhoneIdentifiers"), true);
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "personalPhoneIdentifiers")));
      fs.addHelpIcon(gridBuilder.getString("user.personalPhoneIdentifiers.tooltip"));
    }
  }

  /**
   * If no MEB is configured in config.xml nothing will be done.
   * @param gridBuilder
   * @param user
   */
  public static void createMEBPhoneNumbers(final GridBuilder gridBuilder, final PFUserDO user)
  {
    if (Configuration.getInstance().isMebConfigured() == true) {
      // MEB mobile phone numbers
      final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("user.personalMebMobileNumbers"), true);
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(user, "personalMebMobileNumbers")));
      fs.addHelpIcon(gridBuilder.getString("user.personalMebMobileNumbers.tooltip") + "<br/>" + gridBuilder.getString("user.personalMebMobileNumbers.format"));
    }
  }

  public static void createDescription(final GridBuilder gridBuilder, final PFUserDO user)
  {
    // Description
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("description"));
    fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(user, "description")));
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final boolean adminAccess = accessChecker.isLoggedInUserMemberOfAdminGroup();
    gridBuilder.newGrid8();
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("user"));
      if (adminAccess == true) {
        final RequiredMaxLengthTextField username = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
            "username"));
        WicketUtils.setStrong(username);
        fs.add(username);
        username.add(new AbstractValidator<String>() {

          @Override
          protected void onValidate(final IValidatable<String> validatable)
          {
            data.setUsername(validatable.getValue());
            if (StringUtils.isNotEmpty(data.getUsername()) == true && ((UserDao) getBaseDao()).doesUsernameAlreadyExist(data) == true) {
              validatable.error(new ValidationError().addMessageKey("user.error.usernameAlreadyExists"));
            }
          }
        });
      } else {
        fs.add(new DivTextPanel(fs.newChildId(), data.getUsername()));
      }
    }
    createFirstName(gridBuilder, data);
    createLastName(gridBuilder, data);
    createOrganization(gridBuilder, data);
    createEMail(gridBuilder, data);
    createJIRAUsername(gridBuilder, data);

    if (adminAccess == true) {
      addPassswordFields();
    }

    gridBuilder.newGrid8();
    createLastLoginAndDeleteAllStayLogins(gridBuilder, data, (UserDao) getBaseDao(), this);
    createLocale(gridBuilder, data);
    createDateFormat(gridBuilder, data);
    createExcelDateFormat(gridBuilder, data);
    createTimeNotation(gridBuilder, data);
    createTimeZone(gridBuilder, data);
    createPhoneIds(gridBuilder, data);
    createMEBPhoneNumbers(gridBuilder, data);

    gridBuilder.newGrid16(true);
    addAssignedGroups(adminAccess);
    if (adminAccess == true) {
      addRights();
    }

    gridBuilder.newGrid16();
    createDescription(gridBuilder, data);
  }

  @SuppressWarnings("serial")
  private void addPassswordFields()
  {
    // Password
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("password"), getString("passwordRepeat"), true);
    final PasswordTextField passwordRepeatField = new PasswordTextField(fs.getTextFieldId(), new PropertyModel<String>(this,
        "passwordRepeat")) {
      @Override
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
    final PasswordTextField passwordField = new PasswordTextField(fs.getTextFieldId(), new PropertyModel<String>(this, "password")) {
      @Override
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
    passwordRepeatField.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(final IValidatable<String> validatable)
      {
        final String passwordRepeatInput = validatable.getValue();
        passwordField.validate();
        final String passwordInput = passwordField.getConvertedInput();
        if (StringUtils.isEmpty(passwordInput) == true && StringUtils.isEmpty(passwordRepeatInput) == true) {
          return;
        }
        if (StringUtils.equals(passwordInput, passwordRepeatInput) == false) {
          encryptedPassword = null;
          validatable.error(new ValidationError().addMessageKey("user.error.passwordAndRepeatDoesNotMatch"));
          return;
        }
        if (MAGIC_PASSWORD.equals(passwordInput) == false || encryptedPassword == null) {
          final String errorMsgKey = ((UserDao) getBaseDao()).checkPasswordQuality(passwordInput);
          if (errorMsgKey != null) {
            encryptedPassword = null;
            validatable.error(new ValidationError().addMessageKey(errorMsgKey));
          } else {
            encryptedPassword = ((UserDao) getBaseDao()).encryptPassword(passwordInput);
          }
        }
      }
    });
    passwordField.setResetPassword(false).setRequired(isNew());
    WicketUtils.setPercentSize(passwordField, 50);
    WicketUtils.setPercentSize(passwordRepeatField, 50);
    fs.add(passwordField);
    fs.add(passwordRepeatField);
    fs.addHelpIcon(getString(UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK));
  }

  private static void addDateFormatCombobox(final GridBuilder gridBuilder, final PFUserDO user, final String labelKey,
      final String property, final String[] dateFormats, final boolean convertExcelFormat)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString(labelKey));
    final LabelValueChoiceRenderer<String> dateFormatChoiceRenderer = new LabelValueChoiceRenderer<String>();
    for (final String str : dateFormats) {
      String dateString = "???";
      final String pattern = convertExcelFormat == true ? str.replace('Y', 'y').replace('D', 'd') : str;
      try {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        dateString = dateFormat.format(new Date());
      } catch (final Exception ex) {
        log.warn("Invalid date format in config.xml: " + pattern);
      }
      dateFormatChoiceRenderer.addValue(str, str + ": " + dateString);
    }
    final DropDownChoice<String> dateFormatChoice = new DropDownChoice<String>(fs.getDropDownChoiceId(), new PropertyModel<String>(user,
        property), dateFormatChoiceRenderer.getValues(), dateFormatChoiceRenderer);
    dateFormatChoice.setNullValid(true);
    fs.add(dateFormatChoice);
  }

  private void addRights()
  {
    final List<UserRightVO> userRights = userRightDao.getUserRights(data);
    boolean first = true;
    boolean odd = true;
    for (final UserRightVO rightVO : userRights) {
      final UserRight right = rightVO.getRight();
      final UserRightValue[] availableValues = right.getAvailableValues(((UserDao) getBaseDao()).getUserGroupCache(), data);
      if (right.isConfigurable(((UserDao) getBaseDao()).getUserGroupCache(), data) == false) {
        continue;
      }
      if (first == true) {
        gridBuilder.newGrid16();
        gridBuilder.newFormHeading(getString("access.rights"));
        rightsData = new UserRightsEditData();
        first = false;
      }
      if (odd == true) {
        gridBuilder.newColumnsPanel();
      }
      odd = !odd;
      gridBuilder.newColumnPanel(DivType.COL_50);
      rightsData.addRight(rightVO);
      final String label = getString(right.getId().getI18nKey());
      final FieldsetPanel fs = gridBuilder.newFieldset(label);
      if (right.isBooleanType() == true) {
        final DivPanel radioGroupPanel = fs.addNewRadioBoxDiv();
        final RadioGroupPanel<Boolean> radioGroup = new RadioGroupPanel<Boolean>(radioGroupPanel.newChildId(), "booleanValue",
            new PropertyModel<Boolean>(rightVO, "booleanValue"));
        radioGroupPanel.add(radioGroup);
        WicketUtils.addYesNo(radioGroup);
        fs.setLabelFor(radioGroup.getRadioGroup());
      } else {
        final LabelValueChoiceRenderer<UserRightValue> valueChoiceRenderer = new LabelValueChoiceRenderer<UserRightValue>(fs,
            availableValues);
        final DropDownChoice<UserRightValue> valueChoice = new DropDownChoice<UserRightValue>(fs.getDropDownChoiceId(),
            new PropertyModel<UserRightValue>(rightVO, "value"), valueChoiceRenderer.getValues(), valueChoiceRenderer);
        valueChoice.setNullValid(true);
        fs.add(valueChoice);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "serial"})
  private void addAssignedGroups(final boolean adminAccess)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("user.assignedGroups"), true).setLabelSide(false);
    List<Integer> groupsToAdd = null;
    if (data != null) {
      if (TUTORIAL_DEFAULT_PASSWORD.equals(data.getPassword()) == true) {
        encryptedPassword = ((UserDao) getBaseDao()).encryptPassword(TUTORIAL_DEFAULT_PASSWORD);
        password = passwordRepeat = MAGIC_PASSWORD;
      }
      groupsToAdd = (List<Integer>) WicketUtils.getAsObject(parentPage.getPageParameters(), TUTORIAL_ADD_GROUPS, List.class);
    }
    final List<KeyValueBean<Integer, String>> fullList = new ArrayList<KeyValueBean<Integer, String>>();
    final List<GroupDO> result = groupDao.getList(groupDao.getDefaultFilter());
    for (final GroupDO group : result) {
      fullList.add(new KeyValueBean<Integer, String>(group.getId(), group.getName()));
    }
    final List<Integer> assignedGroups = new ArrayList<Integer>();
    final Collection<Integer> set = ((UserDao) getBaseDao()).getAssignedGroups(data);
    if (set != null) {
      for (final Integer groupId : set) {
        assignedGroups.add(groupId);
      }
    }
    this.groups = new TwoListHelper<Integer, String>(fullList, assignedGroups);
    if (groupsToAdd != null) {
      groups.assign(groupsToAdd);
    }
    this.groups.sortLists();
    valuesToUnassignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
    valuesToUnassignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToUnassign"));
    WicketUtils.setHeight(valuesToUnassignChoice, 50);
    WicketUtils.setPercentSize(valuesToUnassignChoice, 45);
    fs.add(valuesToUnassignChoice);
    if (adminAccess == true) {
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_WEST, getString("tooltip.assign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
          groups.assign(valuesToAssign);
          valuesToAssign.clear();
          refreshGroupLists();
        };
      }));
      fs.add(new IconLinkPanel(fs.newChildId(), IconType.CIRCLE_ARROW_EAST, getString("tooltip.unassign"), new SubmitLink(
          IconLinkPanel.LINK_ID) {
        @Override
        public void onSubmit()
        {
          accessChecker.checkIsLoggedInUserMemberOfAdminGroup();
          groups.unassign(valuesToUnassign);
          valuesToUnassign.clear();
          refreshGroupLists();
        };
      }));
      valuesToAssignChoice = new ListMultipleChoice<Integer>(fs.getListChoiceId());
      valuesToAssignChoice.setModel(new PropertyModel<Collection<Integer>>(this, "valuesToAssign"));
      WicketUtils.setHeight(valuesToAssignChoice, 50);
      WicketUtils.setPercentSize(valuesToAssignChoice, 45);
      fs.add(valuesToAssignChoice);
      fs.setNowrap();
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

  String getEncryptedPassword()
  {
    return encryptedPassword;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
