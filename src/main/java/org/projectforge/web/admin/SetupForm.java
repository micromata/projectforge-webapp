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

package org.projectforge.web.admin;

import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.database.HibernateUtils;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.UserDao;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TimeZonePanel;

public class SetupForm extends AbstractForm<SetupForm, SetupPage>
{
  private static final long serialVersionUID = -277853572580468505L;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  private SetupTarget setupMode = SetupTarget.EMPTY_DATABASE;

  private TimeZone timeZone = TimeZone.getDefault();

  private String sysopEMail;

  private String feedbackEMail;

  private String adminUsername = InitDatabaseDao.DEFAULT_ADMIN_USER;

  @SuppressWarnings("unused")
  private String password;

  @SuppressWarnings("unused")
  private String passwordRepeat;

  private String encryptedPassword;

  public SetupForm(final SetupPage parentPage)
  {
    super(parentPage);

  }

  @SuppressWarnings("serial")
  protected void init()
  {
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    // RadioChoice mode
    final LabelValueChoiceRenderer<SetupTarget> modeChoiceRenderer = new LabelValueChoiceRenderer<SetupTarget>(this, SetupTarget.values());
    final RadioChoice<SetupTarget> modeChoice = new RadioChoice<SetupTarget>("setupMode",
        new PropertyModel<SetupTarget>(this, "setupMode"), modeChoiceRenderer.getValues(), modeChoiceRenderer);
    add(modeChoice);
    final RequiredMaxLengthTextField adminUsernameField = new RequiredMaxLengthTextField("adminUsername", new PropertyModel<String>(this, "adminUsername"));
    add(adminUsernameField);
    final PasswordTextField passwordRepeatField = new PasswordTextField("passwordRepeat", new PropertyModel<String>(this, "passwordRepeat"));
    passwordRepeatField.setResetPassword(true).setRequired(true);
    add(passwordRepeatField);
    final PasswordTextField passwordField = new PasswordTextField("password", new PropertyModel<String>(this, "password"));
    passwordField.setResetPassword(true).setRequired(true);
    passwordField.add(new AbstractValidator<String>() {
      private String errorMsgKey = null;

      @Override
      protected void onValidate(IValidatable<String> validatable)
      {
        final String passwordInput = validatable.getValue();
        final String passwordRepeatInput = passwordRepeatField.getConvertedInput();
        errorMsgKey = null;
        if (StringUtils.equals(passwordInput, passwordRepeatInput) == false) {
          errorMsgKey = "user.error.passwordAndRepeatDoesNotMatch";
          error(validatable);
        } else {
          errorMsgKey = userDao.checkPasswordQuality(passwordInput);
          if (errorMsgKey != null) {
            error(validatable);
          }
        }
        if (errorMsgKey == null) {
          encryptedPassword = userDao.encryptPassword(passwordInput);
        }
      }

      @Override
      protected String resourceKey()
      {
        return errorMsgKey;
      }
    });
    add(passwordField);
    passwordField.add(new FocusOnLoadBehavior());

    add(new TimeZonePanel("timezone", new PropertyModel<TimeZone>(this, "timeZone")));
    add(new MaxLengthTextField("sysopEMail", new PropertyModel<String>(this, "sysopEMail"), HibernateUtils.getPropertyLength(
        ConfigurationDO.class, "stringValue")));
    add(new MaxLengthTextField("feedbackEMail", new PropertyModel<String>(this, "feedbackEMail"), HibernateUtils.getPropertyLength(
        ConfigurationDO.class, "stringValue")));
    final Button finishButton = new Button("button", new Model<String>("finish")) {
      @Override
      public final void onSubmit()
      {
        parentPage.finishSetup();
      }
    };
    finishButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    add(new SingleButtonPanel("finish", finishButton));
    setDefaultButton(finishButton);
  }

  public SetupTarget getSetupMode()
  {
    return setupMode;
  }

  public TimeZone getTimeZone()
  {
    return timeZone;
  }

  public String getSysopEMail()
  {
    return sysopEMail;
  }

  public String getFeedbackEMail()
  {
    return feedbackEMail;
  }

  public String getEncryptedPassword()
  {
    return encryptedPassword;
  }
  
  public String getAdminUsername()
  {
    return adminUsername;
  }
}
