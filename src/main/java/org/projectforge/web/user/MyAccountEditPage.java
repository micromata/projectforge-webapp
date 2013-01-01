/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.UserFilter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.MessagePage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

public class MyAccountEditPage extends AbstractEditPage<PFUserDO, MyAccountEditForm, UserDao>
{
  private static final long serialVersionUID = 4636922408954211544L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyAccountEditPage.class);

  @SpringBean(name = "userDao")
  private UserDao userDao;

  public MyAccountEditPage(final PageParameters parameters)
  {
    super(parameters, "user.myAccount");
    if (Login.getInstance().isPasswordChangeSupported(getUser()) == true) {
      final BookmarkablePageLink<Void> showChangePasswordLink = new BookmarkablePageLink<Void>("link", ChangePasswordPage.class);
      final ContentMenuEntryPanel menu = new ContentMenuEntryPanel(getNewContentMenuChildId(), showChangePasswordLink,
          getString("menu.changePassword"));
      addContentMenuEntry(menu);
    }
    final PFUserDO loggedInUser = userDao.internalGetById(PFUserContext.getUserId());
    super.init(loggedInUser);
    this.showHistory = false;
  }

  @Override
  public AbstractSecuredBasePage onSaveOrUpdate()
  {
    return super.onSaveOrUpdate();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#afterSaveOrUpdate()
   */
  @Override
  public AbstractSecuredBasePage afterSaveOrUpdate()
  {
    final HttpServletRequest request = WicketUtils.getHttpServletRequest(getRequest());
    // Don't trust the form data, use logged in user from the data base instead.
    UserFilter.updateUser(request, userDao.internalGetById(getUserId()));
    return super.afterSaveOrUpdate();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#updateAll()
   */
  @Override
  protected void update()
  {
    if (PFUserContext.getUserId().equals(getData().getId()) == false) {
      throw new IllegalStateException("Oups, MyAccountEditPage is called with another than the logged in user!");
    }
    getData().setPersonalPhoneIdentifiers(userDao.getNormalizedPersonalPhoneIdentifiers(getData()));
    userDao.updateMyAccount(getData());
    ((MySession) getSession()).setLocale(getRequest());
    if (form.invalidateAllStayLoggedInSessions == true) {
      userDao.renewStayLoggedInKey(getData().getId());
    }
    setResponsePage(new MessagePage("message.successfullChanged"));
  }

  /**
   * @throws UnsupportedOperationException
   * @see org.projectforge.web.wicket.AbstractEditPage#create()
   */
  @Override
  protected void create()
  {
    throw new UnsupportedOperationException("Oups, shouldn't be called.");
  }

  /**
   * @throws UnsupportedOperationException
   * @see org.projectforge.web.wicket.AbstractEditPage#delete()
   */
  @Override
  protected void delete()
  {
    throw new UnsupportedOperationException("Oups, shouldn't be called.");
  }

  /**
   * @throws UnsupportedOperationException
   * @see org.projectforge.web.wicket.AbstractEditPage#markAsDeleted()
   */
  @Override
  protected void markAsDeleted()
  {
    throw new UnsupportedOperationException("Oups, shouldn't be called.");
  }

  /**
   * @throws UnsupportedOperationException
   * @see org.projectforge.web.wicket.AbstractEditPage#undelete()
   */
  @Override
  protected void undelete()
  {
    throw new UnsupportedOperationException("Oups, shouldn't be called.");
  }

  @Override
  protected UserDao getBaseDao()
  {
    return userDao;
  }

  @Override
  protected MyAccountEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final PFUserDO data)
  {
    return new MyAccountEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
