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
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.TimeZoneField;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class UserFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = 6802305266859905435L;

  private final UserDao userDao;

  private PFUserDO data;

  protected TextField<String> usernameField;

  private static final String MAGIC_PASSWORD = "******";

  //
  // @SpringBean(name = "userRightDao")
  // private UserRightDao userRightDao;
  //
  // @SpringBean(name = "groupDao")
  // private GroupDao groupDao;
  //
  // protected UserRightsEditData rightsData;
  //
  private String password;

  private String passwordRepeat;

  private String encryptedPassword;

  //
  // TwoListHelper<Integer, String> groups;
  //
  // private List<Integer> valuesToAssign = new ArrayList<Integer>();
  //
  // private List<Integer> valuesToUnassign = new ArrayList<Integer>();
  //
  // private ListMultipleChoice<Integer> valuesToAssignChoice;
  //
  // private ListMultipleChoice<Integer> valuesToUnassignChoice;

  public UserFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final UserDao userDao, final PFUserDO data)
  {
    super(container, layoutContext);
    this.data = data;
    this.userDao = userDao;
  }

  @SuppressWarnings( { "unchecked", "serial"})
  @Override
  public void add()
  {
    IField field;
    doPanel.newFieldSetPanel(isNew() == false ? data.getFullname() : getString("user"));
    field = doPanel.addTextField(new PanelContext(data, "username", FULL, getString("username"), HALF).setRequired().setStrong());
    if (field instanceof TextFieldLPanel) { // Isn't true for read-only fields.
      usernameField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
    }
    doPanel.addTextField(new PanelContext(data, "firstname", FULL, getString("firstName"), HALF).setRequired().setStrong());
    doPanel.addTextField(new PanelContext(data, "lastname", FULL, getString("name"), HALF).setRequired().setStrong());
    doPanel.addTextField(new PanelContext(data, "organization", FULL, getString("organization"), HALF));
    doPanel.addTextField(new PanelContext(data, "email", FULL, getString("email"), HALF));
    doPanel.addTextField(new PanelContext(data, "jiraUsername", FULL, getString("user.jiraUsername"), HALF)
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
      doPanel.addDropDownChoice(localeChoice, new PanelContext(FULL, getString("user.locale"), HALF));
    }
    final TimeZoneField timeZone = new TimeZoneField(TextFieldLPanel.INPUT_ID, new PropertyModel<TimeZone>(data, "timeZoneObject"));
    doPanel.addTextField(timeZone, new PanelContext(FULL, getString("timezone"), HALF)
        .setTooltip(getString("tooltip.autocomplete.timeZone")));
    if (StringUtils.isNotEmpty(Configuration.getInstance().getTelephoneSystemUrl()) == true) {
      doPanel.addTextField(new PanelContext(data, "personalPhoneIdentifiers", FULL, getString("user.personalPhoneIdentifiers"), HALF)
          .setTooltip(getString("user.personalPhoneIdentifiers.tooltip")));
    }
    if (Configuration.getInstance().isMebConfigured() == true) {
      doPanel.addTextField(new PanelContext(data, "personalMebMobileNumbers", FULL, getString("user.personalMebMobileNumbers"), HALF)
          .setTooltip(getString("user.personalMebMobileNumbers.tooltip") + "<br/>" + getString("user.personalMebMobileNumbers.format")));
    }

    {
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
      doPanel.addPasswordTextField(passwordField, new PanelContext(FULL, getString("password"), HALF)
          .setTooltip(getString(UserDao.MESSAGE_KEY_PASSWORD_QUALITY_CHECK)));
      doPanel.addPasswordTextField(passwordRepeatField, new PanelContext(FULL, getString("passwordRepeat"), HALF));
    }

    // <tr>
    // <th><wicket:message key="password" /></th>
    // <td><input type="password" class="stdtext" wicket:id="password" /></td>
    // <th><wicket:message key="passwordRepeat" /></th>
    // <td><input type="password" class="stdtext" wicket:id="passwordRepeat" /></td>
    // </tr>

    // <th><wicket:message key="dateFormat" /> / <wicket:message key="dateFormat.xls" /></th>
    // <td><select wicket:id="dateFormatChoice">
    // <option>[MM/dd/yyyy]</option>
    // </select> / <select wicket:id="excelDateFormatChoice">
    // <option>[MM/DD/YYYY]</option>
    // </select></td>
    // </tr>

  }

  protected void validation()
  {
    usernameField.validate();
    data.setUsername(usernameField.getConvertedInput());
    if (StringUtils.isNotEmpty(data.getUsername()) == true && userDao.doesUsernameAlreadyExist(data) == true) {
      usernameField.error(getString("user.error.usernameAlreadyExists"));
    }
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
}
