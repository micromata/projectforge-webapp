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

package org.projectforge.web.admin;

import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.database.HibernateUtils;
import org.projectforge.database.InitDatabaseDao;
import org.projectforge.user.UserDao;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelForPanel;
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

  private SetupTarget setupMode = SetupTarget.TEST_DATA;

  private TimeZone timeZone = TimeZone.getDefault();

  private String sysopEMail;

  private String feedbackEMail;

  private String adminUsername = InitDatabaseDao.DEFAULT_ADMIN_USER;

//  @SuppressWarnings("unused")
//  private String organization;

  @SuppressWarnings("unused")
  private String password;

  @SuppressWarnings("unused")
  private String passwordRepeat;

  private String encryptedPassword;

  protected FileUploadField fileUploadField;

  protected String filename;

  public SetupForm(final SetupPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));
    setMaxSize(Bytes.megabytes(100));
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
    // final RequiredMaxLengthTextField organizationField = new RequiredMaxLengthTextField(this, "organization", getString("organization"),
    // new PropertyModel<String>(this, "organization"), 100);
    // add(organizationField);
    final RequiredMaxLengthTextField adminUsernameField = new RequiredMaxLengthTextField(this, "adminUsername", getString("username"),
        new PropertyModel<String>(this, "adminUsername"), 100);
    add(adminUsernameField);

    String str = getString("passwordRepeat");
    final PasswordTextField passwordRepeatField = new PasswordTextField("passwordRepeat", new PropertyModel<String>(this, "passwordRepeat"));
    passwordRepeatField.setLabel(new Model<String>(str));
    passwordRepeatField.setResetPassword(true).setRequired(true);
    add(passwordRepeatField);
    final LabelForPanel passwordRepeatLabel = new LabelForPanel("passwordRepeatLabel", passwordRepeatField, str);
    add(passwordRepeatLabel);

    str = getString("password");
    final PasswordTextField passwordField = new PasswordTextField("password", new PropertyModel<String>(this, "password"));
    passwordField.setLabel(new Model<String>(str));
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
    final LabelForPanel passwordLabel = new LabelForPanel("passwordLabel", passwordField, str);
    add(passwordLabel);

    add(new TimeZonePanel("timezone", new PropertyModel<TimeZone>(this, "timeZone")));
    add(new MaxLengthTextField(this, "sysopEMail", getString("administration.configuration.param.systemAdministratorEMail"),
        new PropertyModel<String>(this, "sysopEMail"), HibernateUtils.getPropertyLength(ConfigurationDO.class, "stringValue")));
    add(new MaxLengthTextField(this, "feedbackEMail", getString("administration.configuration.param.feedbackEMail"),
        new PropertyModel<String>(this, "feedbackEMail"), HibernateUtils.getPropertyLength(ConfigurationDO.class, "stringValue")));
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
    add(new Label("filename", new Model<String>() {
      @Override
      public String getObject()
      {
        return fileUploadField.getFileUpload() != null ? fileUploadField.getFileUpload().getClientFileName() : "";
      }
    }));
    final Button uploadButton = new Button("button", new Model<String>(getString("upload"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.upload();
      }
    };
    uploadButton.setDefaultFormProcessing(false);
    add(new SingleButtonPanel("upload", uploadButton));
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
