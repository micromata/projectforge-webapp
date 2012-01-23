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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.web.wicket.NewAbstractForm;
import org.projectforge.web.wicket.WicketUtils;

public class LoginForm extends NewAbstractForm<LoginForm, LoginPage>
{
  private static final long serialVersionUID = -422822736093879603L;

  @SpringBean(name = "configuration")
  private Configuration configuration;

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
    add(WicketUtils.setFocus(new TextField<String>("username", new PropertyModel<String>(this, "username")).setRequired(true)).setMarkupId(
        "username"));
    add(new PasswordTextField("password", new PropertyModel<String>(this, "password")).setResetPassword(true).setRequired(true));
    final String messageOfTheDay = configuration.getStringValue(ConfigurationParam.MESSAGE_OF_THE_DAY);
    final Label messageOfTheDayLabel = new Label("messageOfTheDay", messageOfTheDay);
    add(messageOfTheDayLabel.setEscapeModelStrings(false).setVisible(StringUtils.isNotBlank(messageOfTheDay)));
    final Button loginButton = new Button("login", new Model<String>(getString("login"))) {
      @Override
      public final void onSubmit()
      {
        final String result = parentPage.checkLogin();
        if (result != null) {
          addError(result);
        }
      }
    };
    setDefaultButton(loginButton);
    add(loginButton);
  }

  /**
   * Need this for marking login page submit as login page submit (no redirect required). See UserFilter.redirectToLoginPage.
   * @see org.apache.wicket.markup.html.form.Form#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag tag)
  {
    super.onComponentTag(tag);
    final String action = tag.getAttribute("action");
    tag.put("action", action + "&loginpage=true");
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
