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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.access.AccessChecker;
import org.projectforge.common.StringHelper;
import org.projectforge.common.TimeNotation;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.ldap.LdapPosixAccountsUtils;
import org.projectforge.ldap.LdapUserDao;
import org.projectforge.ldap.LdapUserValues;
import org.projectforge.ldap.PFUserDOConverter;
import org.projectforge.user.GroupDO;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRightDao;
import org.projectforge.user.UserRightVO;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.I18nCore;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.MultiChoiceListHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TimeZoneField;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;

import com.vaynberg.wicket.select2.Select2MultiChoice;

public class UserEditForm extends AbstractEditForm<PFUserDO, UserEditPage>
{
  private static final long serialVersionUID = 7872294377838461659L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditForm.class);

  private static final String MAGIC_PASSWORD = "******";

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected UserRightsEditData rightsData;

  String password;

  @SuppressWarnings("unused")
  private String passwordRepeat;

  private String encryptedPassword;

  boolean invalidateAllStayLoggedInSessions;

  MultiChoiceListHelper<GroupDO> assignListHelper;

  LdapUserValues ldapUserValues;

  TextField< ? > usernameTextField, uidNumberField, gidNumberField, homeDirectoryField, loginShellField;

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

  @SuppressWarnings("serial")
  public static void createAuthenticationToken(final GridBuilder gridBuilder, final PFUserDO user, final UserDao userDao,
      final Form< ? > form)
  {
    // Authentication token
    final FieldsetPanel fs = gridBuilder.newFieldset(gridBuilder.getString("user.authenticationToken"), true).setNoLabelFor();
    fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
      @Override
      public String getObject()
      {
        if (PFUserContext.getUserId().equals(user.getId()) == true) {
          return userDao.getAuthenticationToken(user.getId());
        } else {
          // Administrators shouldn't see the token.
          return "*****";
        }
      }
    }));
    fs.addHelpIcon(gridBuilder.getString("user.authenticationToken.tooltip"));
    final Button button = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("renewAuthenticationKey")) {
      @Override
      public final void onSubmit()
      {
        userDao.renewAuthenticationToken(user.getId());
        form.error(getString("user.authenticationToken.renew.successful"));
      }
    };
    button.add(WicketUtils.javaScriptConfirmDialogOnClick(form.getString("user.authenticationToken.renew.securityQuestion")));
    fs.add(new SingleButtonPanel(fs.newChildId(), button, gridBuilder.getString("user.authenticationToken.renew"), SingleButtonPanel.RED));
    WicketUtils.addTooltip(button, gridBuilder.getString("user.authenticationToken.renew.tooltip"));
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
      fs.addHelpIcon(gridBuilder.getString("user.personalMebMobileNumbers.tooltip")
          + "<br/>"
          + gridBuilder.getString("user.personalMebMobileNumbers.format"));
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
    ldapUserValues = PFUserDOConverter.readLdapUserValues(data.getLdapValues());
    if (ldapUserValues == null) {
      ldapUserValues = new LdapUserValues();
    }
    final boolean adminAccess = accessChecker.isLoggedInUserMemberOfAdminGroup();
    gridBuilder.newGrid8();
    {
      // User
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("user"));
      if (adminAccess == true) {
        usernameTextField = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "username"));
        WicketUtils.setStrong(usernameTextField);
        fs.add(usernameTextField);
        usernameTextField.add(new AbstractValidator<String>() {

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
    createAuthenticationToken(gridBuilder, data, (UserDao) getBaseDao(), this);
    createJIRAUsername(gridBuilder, data);
    if (adminAccess == true) {
      WicketUtils.addYesNoRadioFieldset(gridBuilder, getString("user.hrPlanningEnabled"), "hrPlanning", new PropertyModel<Boolean>(data,
          "hrPlanning"), getString("user.hrPlanningEnabled.tooltip"));
      WicketUtils.addYesNoRadioFieldset(gridBuilder, getString("user.activated"), "activated", new Model<Boolean>() {
        @Override
        public Boolean getObject()
        {
          return data.isDeactivated() == false;
        };

        @Override
        public void setObject(final Boolean activated)
        {
          data.setDeactivated(!activated);
        };
      }, getString("user.activated.tooltip"));
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
    if (adminAccess == true && Login.getInstance().hasExternalUsermanagementSystem() == true) {
      gridBuilder.newGrid16(true);
      gridBuilder.newFormHeading(getString("ldap"));
      gridBuilder.newColumnsPanel();
      gridBuilder.newColumnPanel(DivType.COL_50);
      WicketUtils.addYesNoRadioFieldset(gridBuilder, getString("user.localUser"), "localUser",
          new PropertyModel<Boolean>(data, "localUser"), getString("user.localUser.tooltip"));
      if (LdapUserDao.isPosixAccountsConfigured() == true) {
        {
          final FieldsetPanel fs = gridBuilder.newFieldset(getString("ldap.uidNumber"), getString("ldap.posixAccount"), true);
          uidNumberField = new MinMaxNumberField<Integer>(fs.getTextFieldId(), new PropertyModel<Integer>(ldapUserValues, "uidNumber"), 1,
              65535);
          WicketUtils.setSize(uidNumberField, 6);
          fs.add(uidNumberField);
          if (ldapUserValues.isPosixAccountValuesEmpty() == true) {
            final AjaxButton createButton = new AjaxButton(SingleButtonPanel.WICKET_ID, this) {
              @Override
              protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
              {
                data.setUsername(usernameTextField.getRawInput());
                LdapPosixAccountsUtils.setDefaultValues(ldapUserValues, data);
                uidNumberField.setEnabled(true);
                gidNumberField.setEnabled(true);
                homeDirectoryField.setEnabled(true);
                loginShellField.setEnabled(true);
                this.setVisible(false);
                target.add(this, uidNumberField, gidNumberField, homeDirectoryField, loginShellField);
              }

              @Override
              protected void onError(final AjaxRequestTarget target, final Form< ? > form)
              {
                target.add(UserEditForm.this.feedbackPanel);
              }
            };
            createButton.setDefaultFormProcessing(false);
            fs.add(new SingleButtonPanel(fs.newChildId(), createButton, gridBuilder.getString("create"), SingleButtonPanel.GREY));
            WicketUtils.addTooltip(createButton, gridBuilder.getString("ldap.posixAccount.createDefault.tooltip"));
          }
        }
        {
          final FieldsetPanel fs = gridBuilder.newFieldset(getString("ldap.gidNumber"), getString("ldap.posixAccount"));
          gidNumberField = new MinMaxNumberField<Integer>(fs.getTextFieldId(), new PropertyModel<Integer>(ldapUserValues, "gidNumber"), 1,
              65535);
          WicketUtils.setSize(gidNumberField, 6);
          fs.add(gidNumberField);
        }
      }
      gridBuilder.newColumnPanel(DivType.COL_50);
      WicketUtils.addYesNoRadioFieldset(gridBuilder, getString("user.restrictedUser"), "restrictedUser", new PropertyModel<Boolean>(data,
          "restrictedUser"), getString("user.restrictedUser.tooltip"));
      if (LdapUserDao.isPosixAccountsConfigured() == true) {
        {
          final FieldsetPanel fs = gridBuilder.newFieldset(getString("ldap.homeDirectory"), getString("ldap.posixAccount"));
          homeDirectoryField = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(ldapUserValues, "homeDirectory"));
          fs.add(homeDirectoryField);
        }
        {
          final FieldsetPanel fs = gridBuilder.newFieldset(getString("ldap.loginShell"), getString("ldap.posixAccount"));
          loginShellField = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(ldapUserValues, "loginShell"));
          fs.add(loginShellField);
        }
        if (ldapUserValues.isPosixAccountValuesEmpty() == true) {
          uidNumberField.setEnabled(false);
          gidNumberField.setEnabled(false);
          homeDirectoryField.setEnabled(false);
          loginShellField.setEnabled(false);
        }
      }
    }
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
    passwordField.setResetPassword(false).setRequired(isNew());
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

      /**
       * @see org.apache.wicket.validation.validator.AbstractValidator#validateOnNullValue()
       */
      @Override
      public boolean validateOnNullValue()
      {
        // Should be validated (e. g. if password field is given but password repeat field not).
        return true;
      }
    });
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

  private void addAssignedGroups(final boolean adminAccess)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("user.assignedGroups"), true).setLabelSide(false);
    final Collection<Integer> set = ((UserDao) getBaseDao()).getAssignedGroups(data);
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
    final Select2MultiChoice<GroupDO> groups = new Select2MultiChoice<GroupDO>(fs.getSelect2MultiChoiceId(),
        new PropertyModel<Collection<GroupDO>>(this.assignListHelper, "assignedItems"), groupsProvider);
    fs.add(groups);
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
