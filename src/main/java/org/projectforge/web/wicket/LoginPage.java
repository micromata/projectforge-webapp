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

package org.projectforge.web.wicket;

import javax.servlet.http.HttpSession;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;

import de.micromata.user.LogonFilter;
import de.micromata.user.UserInfo;

/**
 * Not yet implemented (loginFilter is used instead).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LoginPage extends AbstractBasePage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoginPage.class);

  public static void login(final MySession session, final HttpSession httpSession, final PFUserDO user)
  {
    session.setUser(user);
    httpSession.setAttribute(LogonFilter.USER_ATTRIBUTE, user);
  }

  @SpringBean(name = "userDao")
  private UserDao userDao;

  @SuppressWarnings("serial")
  private class LoginForm extends Form<LoginForm>
  { // StatelessForm
    private String password;

    private String username;

    public LoginForm(final String id)
    {
      super(id);
      setModel(new CompoundPropertyModel<LoginForm>(this));
      Component username = new TextField<String>("username") //
          .setRequired(true);
      username.add(new FocusOnLoadBehavior());
      add(username);

      Component password = new PasswordTextField("password") //
          .setRequired(true);
      add(password);
    }

    @SuppressWarnings("unused")
    public String getPassword()
    {
      return password;
    }

    @SuppressWarnings("unused")
    public String getUsername()
    {
      return username;
    }

    @SuppressWarnings("unused")
    public void setPassword(String password)
    {
      this.password = password;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username)
    {
      this.username = username;
    }

    @Override
    public final void onSubmit()
    {
      if (signIn(username, password)) {
        if (continueToOriginalDestination() == false) {
          setResponsePage(WicketUtils.getDefaultPage());
        }
      } else {
        error(getString("login.error.loginFailed"));
      }
    }

    private boolean signIn(String username, String password)
    {
      if (username != null && password != null) {
        final PFUserDO user = (PFUserDO) checkLogon(username, password);
        if (user != null) {
          final MySession session = MySession.get();
          session.bind();
          if (session.isTemporary()) {
            log.error("Session temporary, whatever it means!!!");
          }
          final ServletWebRequest req = (ServletWebRequest) this.getRequest(); // "this" is a WebPage
          final HttpSession httpSession = req.getHttpServletRequest().getSession();
          login(session, httpSession, user);
          return true;
        }
      }
      return false;
    }
  }

  public LoginPage(PageParameters parameters)
  {
    super(parameters);

    setStatelessHint(true);
    setVersioned(false);

    LoginForm form = new LoginForm("form");
    add(form);
    add(new FeedbackPanel("feedback") //
        .setRenderBodyOnly(true));
    Button submit = new Button("login", new Model<String>(getString("login")));
    form.add(submit);
    // Die folgende Zeile sorgt dafür, das <ENTER> ein submit auslöst ...
    form.setDefaultButton(submit);
    throw new UnsupportedOperationException("Do not use yet! LoginFilter and Login.Action is used instead.");
  }

  @Override
  protected String getTitle()
  {
    return getString("login.title");
  }

  /**
   * Yes it is because this is the LoginPage!
   * @see org.projectforge.web.wicket.AbstractBasePage#thisIsAnUnsecuredPage()
   */
  @Override
  protected void thisIsAnUnsecuredPage()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the UserInfo if login is successful, otherwise null (e. g. for wrong username password combination and for deleted users).
   * @see de.micromata.user.PasswordChecker#checkLogon(java.lang.String, java.lang.String,
   *      de.micromata.user.PasswordChecker.AfterLoginAction)
   */
  public UserInfo checkLogon(String username, String password)
  {
    log.debug("LoginEditCheckAction.execute");
    String encryptedPassword = userDao.encryptPassword(password);

    final PFUserDO user = userDao.authenticateUser(username, encryptedPassword);
    if (user != null) {
      log.info("User with valid username/password: " + username + "/" + encryptedPassword);
      if (user.isDeleted() == true) {
        log.info("User has no system access (is deleted): " + user.getDisplayUsername());
        return null;
      } else {
        log.info("User successfully logged in: " + user.getDisplayUsername());
        return user;
      }
    } else {
      log.info("User login failed: " + username + "/" + encryptedPassword);
    }
    return null;
  }

}
