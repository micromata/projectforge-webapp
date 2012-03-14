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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;

public class ChangePasswordForm extends AbstractForm<ChangePasswordForm, ChangePasswordPage>
{
  private static final long serialVersionUID = -3424973755250980758L;

  private String oldPassword, newPassword, passwordRepeat;

  public ChangePasswordForm(final ChangePasswordPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    add(new PasswordTextField("oldPassword", new PropertyModel<String>(this, "oldPassword")).setResetPassword(true).setRequired(true).add(new FocusOnLoadBehavior()));
    add(new PasswordTextField("newPassword", new PropertyModel<String>(this, "newPassword")).setResetPassword(true).setRequired(true));
    add(new PasswordTextField("passwordRepeat", new PropertyModel<String>(this, "passwordRepeat")).setResetPassword(true).setRequired(true));
    final Button updateButton = new Button("button", new Model<String>(getString("update"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.update();
      }
    };
    //    add(new SingleButtonPanel("update", updateButton));
    //    updateButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    //    setDefaultButton(updateButton);
    //    final Button cancelButton = new Button("button", new Model<String>(getString("cancel"))) {
    //      @Override
    //      public final void onSubmit()
    //      {
    //        parentPage.cancel();
    //      }
    //    };
    //    add(new SingleButtonPanel("cancel", cancelButton.setDefaultFormProcessing(false)));
  }

  public String getOldPassword()
  {
    return oldPassword;
  }

  public void setOldPassword(final String oldPassword)
  {
    this.oldPassword = oldPassword;
  }

  public String getNewPassword()
  {
    return newPassword;
  }

  public void setNewPassword(final String newPassword)
  {
    this.newPassword = newPassword;
  }

  public String getPasswordRepeat()
  {
    return passwordRepeat;
  }

  public void setPasswordRepeat(final String passwordRepeat)
  {
    this.passwordRepeat = passwordRepeat;
  }
}
