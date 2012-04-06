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

package org.projectforge.web;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class LoginForm extends AbstractForm<LoginForm, LoginPage>
{
  private static final long serialVersionUID = -422822736093879603L;

  private boolean stayLoggedIn;

  private String username, password;

  public LoginForm(final LoginPage parentPage)
  {
    super(parentPage);

  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    // add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    add(new CheckBox("stayLoggedIn", new PropertyModel<Boolean>(this, "stayLoggedIn")));
    final TextField<String> username = new TextField<String>("username", new PropertyModel<String>(this, "username"));
    username.setRequired(true).setMarkupId("username").setOutputMarkupId(true);
    add(username);
    // Focus is set in adminica_ui.js
    add(new PasswordTextField("password", new PropertyModel<String>(this, "password")).setResetPassword(true).setRequired(true));
    final Button loginButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("login")) {
      @Override
      public final void onSubmit()
      {
        final String result = parentPage.checkLogin();
        if (result != null) {
          parentPage.addError(getString(result));
        }
      }
    };
    setDefaultButton(loginButton);
    add(new SingleButtonPanel("loginButton", loginButton, getString("login"), SingleButtonPanel.DEFAULT_SUBMIT));
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(final String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  public boolean isStayLoggedIn()
  {
    return stayLoggedIn;
  }

  public void setStayLoggedIn(final boolean stayLoggedIn)
  {
    this.stayLoggedIn = stayLoggedIn;
  }
}
